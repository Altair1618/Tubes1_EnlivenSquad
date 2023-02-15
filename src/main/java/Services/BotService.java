package Services;

import Enums.*;
import Models.*;

import java.util.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    static private int tickTimer = 0; // timer tick, > 0 jika timer hidup
    static private int playerRadarRadius = 400; // radius jarak deteksi player
    static private Double headingOffset = 1.; // offset sudut untuk mengamsumsikan arah saat ini sudah sesuai tujuan
    static private int fieldRadarRadius = 25; // radius jarak deteksi cloud dan asteroid
    static private int playerDangerRange = 20; // Range player gede dianggap berbahaya
    static private int huntingRange = 100;

    // weight untuk setiap kasus kabur/ngejar
    static private Double[] weights = {
        0.45, // menghindar dari bot besar
        0.08, // ada torpedo mengarah ke kita dan berada di danger zone kita
        0.1, // mengejar bot keci
        0.17, // masuk kembali ke map
        0.05, // menghindar dari supernova bomb yang ke arah kita
        0.1, // menghindar keluar cloud
        0.04, // menghindar keluar asteroid field
        0.01, // mengejar food jika punya super food
    };

    class EscapeInfo {
        public WorldVector escapeDirection;
        public Double weight;

        EscapeInfo(WorldVector escapeDirection, Double weight) {
            this.escapeDirection = escapeDirection;
            this.weight = weight; 

            this.escapeDirection.normalize();
        }
    }

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
    }

    public GameObject getBot() {
        return this.bot;
    }

    public void setBot(GameObject bot) {
        this.bot = bot;
    }

    public PlayerAction getPlayerAction() {
        return this.playerAction;
    }

    public void setPlayerAction(PlayerAction playerAction) {
        this.playerAction = playerAction;
    }

    public PlayerActions getAction() {
        return this.playerAction.action;
    }

    public void setAction(PlayerActions playerAction) {
        this.playerAction.action = playerAction;
    }

    public void setHeading(int heading) {
        playerAction.heading = heading;
    }

    public void computeNextPlayerAction(PlayerAction playerAction) {

        if (tickTimer > 0) tickTimer--;
        if (gameState == null || gameState.world == null || gameState.world.radius == null || gameState.world.centerPoint == null) return;
        
        List<EscapeInfo> directionVectors = new ArrayList<EscapeInfo>();
        WorldVector temp;
        EscapeInfo t;
        List<Boolean> effectList = Effects.getEffectList(bot.effectsCode);
        
        RadarService.updateAttributes(gameState, bot);

        List<GameObject> superNovaBombs = SupernovaService.getSupernovaBombs(gameState);

        if (!superNovaBombs.isEmpty() 
            && (SupernovaService.isSuperNovaOutsideMap(gameState, superNovaBombs.get(0),gameState.world.radius / 4 + superNovaBombs.get(0).size) 
                || SupernovaService.isSupernovaNearPlayer(gameState, bot)) 
            && !SupernovaService.isDetonated 
            && SupernovaService.isFired 
            && RadarService.getRealDistance(bot.size, SupernovaService.superNovaSize, RadarService.getDistanceBetween(bot, superNovaBombs.get(0))) > 0)
        /* jika kita sudah nembak supernova dan supernova bombnya dekat musuh atau akan keluar map (menghindari error) */
        {
            playerAction.action = PlayerActions.DETONATESUPERNOVA;
            this.playerAction = playerAction;
            /* set variabel sudah nembak supernova menjadi false */
            SupernovaService.isDetonated = true;
            SupernovaService.isFired = false;
            System.out.println("1");

            return;
        }

        List<GameObject> playersList = PlayerService.getOtherPlayerList(gameState, bot);

        if (!playersList.isEmpty() && RadarService.getRealDistance(bot, playersList.get(0)) <= playerRadarRadius && TorpedoService.isTorpedoAvailable(bot)) {
            playerAction.action = PlayerActions.FIRETORPEDOES;
            
            playerAction.heading = RadarService.getHeadingBetween(bot, playersList.get(0));

            this.playerAction = playerAction;

            return;
        }

        // KASUS PINDAH 1
        
        List<GameObject> biggerPlayer = PlayerService.getBiggerPlayerInRange(gameState, bot, playerDangerRange);
        if (!biggerPlayer.isEmpty())
        {
            temp = PlayerService.getEscapePlayerVector(gameState, biggerPlayer, bot); /*isi temp dengan nilai arah kabur dari bot besar */
            t = new EscapeInfo(temp, weights[0]);
            directionVectors.add(t);
            System.out.println("2");
        }

        List<GameObject> incomingTorpedo = TorpedoService.getIncomingTorpedo(gameState, bot);

        // if (!incomingTorpedo.isEmpty() && ShieldService.isShieldAvailable(bot, 30)) {
        //     playerAction.action = PlayerActions.ACTIVATESHIELD;

        //     this.playerAction = playerAction;
        //     return;
        // }

        temp = calculateResult(directionVectors);
        Double offsetAngle = Math.abs(temp.getAngleTo(RadarService.degreeToVector(bot.getHeading())));
        if ((temp.isZero() || offsetAngle < headingOffset) && !incomingTorpedo.isEmpty())
        /* jika ada torpedo yang mengarah ke kita */
        {
            /* jika torpedo (terdekat) di dalam danger zone kita */
            if (TorpedoService.fireTorpedoWhenDanger(bot, incomingTorpedo.get(0)) && TorpedoService.isTorpedoAvailable(bot, 25)) {
                playerAction.action = PlayerActions.FIRETORPEDOES;

                // playerAction.heading = titik temu torpedo kita dengan torpedo musuh
                playerAction.heading = RadarService.getHeadingBetween(bot, incomingTorpedo.get(0)); // ini belum predict
                this.playerAction = playerAction;

                System.out.println("3");
                return;
            }
        }

        // KASUS PINDAH 2

        if (!incomingTorpedo.isEmpty() && incomingTorpedo != null)
        /* jika torpedo terdetect mengarah ke kita tetapi bukan dalam danger zone */
        {
            temp = new WorldVector();// temp = nilai arah kabur dari torpedo */
            temp = TorpedoService.nextHeadingAfterProjectiles(gameState, bot, incomingTorpedo);
            t = new EscapeInfo(temp, weights[1]);
            directionVectors.add(t);
            System.out.println("4");
        }

        // KASUS PINDAH 3

        List<GameObject> preys = PlayerService.getPreys(gameState, bot, PlayerService.sizeDifferenceOffset, huntingRange);
        if (!preys.isEmpty())
        {
            temp = PlayerService.getChasePlayerVector(gameState, preys, bot);// isi temp dengan nilai arah KEJAR musuh */
            t = new EscapeInfo(temp, weights[2]);
            directionVectors.add(t);

            System.out.println("5");
        }

        // KASUS  PINDAH 4

        // jika keluar map
        if (FieldService.isOutsideMap(gameState, bot, 50))
        {
            temp = RadarService.degreeToVector(FieldService.getCenterDirection(gameState, bot));
            t = new EscapeInfo(temp, weights[4]);

            directionVectors.add(t);
            System.out.println("6");

        }

        // KASUS PINDAH 5
        
        List<GameObject> incomingSupernova = SupernovaService.getIncomingSupernova(gameState, bot);

        if (!incomingSupernova.isEmpty() && incomingSupernova != null)
        /*ada supernova bomb mengarah ke kita */
        {
            temp = new WorldVector(); // isi dengan nilai arah kabur dari supernova bomb */
            temp = SupernovaService.nextHeadingAfterProjectile(gameState, bot, incomingSupernova.get(0));
            t = new EscapeInfo(temp, weights[5]);

            directionVectors.add(t);
            System.out.println("7");
        }
        
        // KASUS PINDAH 6
        // jika masuk cloud

        List<GameObject> collapsingClouds = FieldService.getCollapsingClouds(bot, fieldRadarRadius);

        if (!collapsingClouds.isEmpty())
        {
            
            temp = FieldService.getHeadingEscape(gameState, bot, collapsingClouds);

            t = new EscapeInfo(temp, weights[6]);
            directionVectors.add(t);
            System.out.println("8");
            
        }

        // KASUS PINDAH 7
        // jika masuk asteroid
        List<GameObject> collapsingAsteroids = FieldService.getCollapsingAsteroids(bot, fieldRadarRadius);
        if (!collapsingAsteroids.isEmpty())
        {

            temp = FieldService.getHeadingEscape(gameState, bot, collapsingAsteroids);

            t = new EscapeInfo(temp, weights[6]);
            directionVectors.add(t);
            System.out.println("9");
            
        }

        // KASUS PINDAH 8   
        // jika punya superfood
        if (effectList.get(3))
        {
            var foods = FoodServices.getNearestFoods(bot);

            if (foods.size() > 0)
            {
                temp = RadarService.degreeToVector(RadarService.getHeadingBetween(bot, foods.get(0))); // isi dengan nilai arah kabur dari supernova bomb */
                t = new EscapeInfo(temp, weights[7]);
                directionVectors.add(t);
                System.out.println("10");

            }
        }

        // PERHITUNGAN PERPINDAHAN BERDASARKAN TIAP WEIGHT
        if (!directionVectors.isEmpty())
        {
            WorldVector res = calculateResult(directionVectors);

            if (!res.isZero())
            {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = RadarService.roundToEven(RadarService.vectorToDegree(res));
                this.playerAction = playerAction;
                System.out.println("11");

                return;
            }
        }

        // KASUS SELANJUTNYA ADALAH KASUS TIDAK ADA YANG PERLU DIKEJAR ATAU DIHINDARI

        if (!TeleportService.isFired && TeleportService.isTeleportAvailable(bot))
        {
            temp = TeleportService.getAttackDirection(bot);

            if (!temp.isZero())
            {
                playerAction.action = PlayerActions.FIRETELEPORT;
                playerAction.heading = RadarService.roundToEven(RadarService.vectorToDegree(temp));
                this.playerAction = playerAction;

                TeleportService.shoot(playerAction.heading);
                return;
            }
        }

        if (TeleportService.isFired)
        {
            GameObject teleporter = TeleportService.getFiredTeleport(gameState, bot);

            if (teleporter != null)
            {
                List<GameObject> collapsingObjects = TeleportService.getCollapsingObjectsAfterTeleport(gameState, bot, teleporter, playerRadarRadius);

                boolean isSafe = TeleportService.isTeleportSafe(bot, collapsingObjects, true);

                if (isSafe)
                {
                    playerAction.action = PlayerActions.TELEPORT;
                    this.playerAction = playerAction;

                    TeleportService.teleport();
                    return;
                }
            }
        }

        // // CARI SUPER FOOD
        // List<GameObject> superFoods = FoodServices.getSuperFoods(gameState, bot);
        // if (!superFoods.isEmpty())
        // {
        //     playerAction.action = PlayerActions.FORWARD;
        //     playerAction.heading = RadarService.getHeadingBetween(bot, superFoods.get(0));
        //     this.playerAction = playerAction;
        //     System.out.println("12");

        //     return;
        // }

   
        if (SupernovaService.isSupernovaPickupExist(gameState) && SupernovaService.isBotNearestfromPickup(gameState, bot))
        {
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = RadarService.getHeadingBetween(bot, SupernovaService.getSupernovaPickupObject(gameState));
            this.playerAction = playerAction;
            System.out.println("13");

            tickTimer = 10;

            return;
        }

    
        if (SupernovaService.isSupernovaAvailable(bot) && !SupernovaService.isFired && tickTimer <= 0)
        /*punya supernova pickup */
        {
            playerAction.action = PlayerActions.FIRESUPERNOVA;

            // players sudah terurut dari terkecil
            List<GameObject> players = PlayerService.getOtherPlayerList(gameState, bot);

            for (int i = 0; i < players.size(); i++) {
                if (RadarService.isCollapsing(players.get(i), bot, 50)) {
                    // playerAction.heading = arah ke TARGET
                    playerAction.heading = RadarService.getHeadingBetween(bot, players.get(i));
                    System.out.println("14");
                    break;
                }
            }
            
            SupernovaService.isFired = true;

            this.playerAction = playerAction;
            return;
        }

        var foods = FoodServices.getAllFoods(gameState, bot);

        // playerAction.heading = arah ke TARGET

        if (foods.size() > 0 && foods != null)
        {
            System.out.println("15");
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = RadarService.getHeadingBetween(bot, foods.get(0));
        }

        else
        {
            playerAction.action = PlayerActions.STOP;
        }

        this.playerAction = playerAction;
    }



    public GameState getGameState() {
        return this.gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateSelfState();
    }

    private void updateSelfState() {
        Optional<GameObject> optionalBot = gameState.getPlayerGameObjects().stream().filter(gameObject -> gameObject.id.equals(bot.id)).findAny();
        optionalBot.ifPresent(bot -> this.bot = bot);
    }

    private WorldVector calculateResult(List<EscapeInfo> directionVectors)
    {
        WorldVector res = new WorldVector();

        for (EscapeInfo v : directionVectors)
        {
            res.add(v.escapeDirection.mult(v.weight));
        }

        return res;
    }


}
