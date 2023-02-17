package Services;

import Enums.*;
import Models.*;

import java.util.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

    static private int tickTimer = 0; // timer tick, > 0 jika timer hidup, solve bug engine pada supernova
    static private int tpTimer = 0; // solve bug engine pada teleport
    static private int playerRadarRadius = 400; // radius jarak deteksi player
    static private Double headingOffset = 5.; // offset sudut untuk mengamsumsikan arah saat ini sudah sesuai tujuan
    static private int fieldRadarRadius = 60; // radius jarak deteksi cloud dan asteroid
    static private int huntingRange = 200;
    static private boolean isAfterburner = false;
    static private int afterBurnerSizeLimit = 40;

    // weight untuk setiap kasus kabur/ngejar
    static private Double[] weights = {
        0.45, // menghindar dari bot besar
        0.08, // ada torpedo mengarah ke kita dan berada di danger zone kita
        0.1, // mengejar bot kecil
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
        if (tpTimer > 0) tpTimer--;
        if (gameState == null || gameState.world == null || gameState.world.radius == null || gameState.world.centerPoint == null) return;

        if (tpTimer == 0 && TeleportService.isFired) TeleportService.teleport();

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
            // System.out.println("1");

            return;
        }

        if (TeleportService.isFired)
        {
            GameObject teleporter = TeleportService.getFiredTeleport(gameState, bot);

            if (teleporter != null)
            {
                List<GameObject> collapsingObjects = TeleportService.getCollapsingObjectsAfterTeleport(gameState, bot, teleporter, 200);
                List<GameObject> collapsingPlayers = TeleportService.getCollapsingPlayersAfterTeleport(gameState, bot, teleporter, 200);
                boolean isSafe = TeleportService.isTeleportSafe(gameState, bot, teleporter, collapsingObjects, collapsingPlayers, TeleportService.isAttacking);

                System.out.println(isSafe);
                if (isSafe)
                {
                    System.out.println("TELEPORTING!!!!!!!!!!!!");
                    playerAction.action = PlayerActions.TELEPORT;
                    this.playerAction = playerAction;

                    TeleportService.teleport();
                    return;
                }
            }
        }

        if (!TeleportService.isFired && TeleportService.isTeleportAvailable(bot))
        {
            
            temp = TeleportService.getAttackDirection(bot);

            if (!temp.isZero())
            {
                playerAction.action = PlayerActions.FIRETELEPORT;
                playerAction.heading = RadarService.roundToEven(RadarService.vectorToDegree(temp));
                this.playerAction = playerAction;
                TeleportService.isAttacking = true;
                TeleportService.shoot(playerAction.heading);
                tpTimer = gameState.world.radius * 3 / TeleportService.teleporterSpeed;
                return;
            }
        }

        if (isAfterburner && bot.size <= afterBurnerSizeLimit)
        {
            playerAction.action = PlayerActions.STOPAFTERBURNER;
            this.playerAction = playerAction;

            System.out.println("STOP AFTERBURNER");
            isAfterburner = false;

            return;
        }
        
        List<GameObject> playersList = PlayerService.getOtherPlayerList(gameState, bot);
        int maxEnemySize = PlayerService.getBiggestEnemySize();
        List<GameObject> incomingTeleports = TeleportService.getIncomingTeleporter(bot, maxEnemySize + 20);

        if (!playersList.isEmpty()
            && RadarService.getRealDistance(bot, playersList.get(0)) <= playerRadarRadius 
            && TorpedoService.isTorpedoAvailable(bot, 40)
            && ProjectileService.isPriorHit(bot, playersList.get(0), TorpedoService.missilesSpeed, TorpedoService.missilesSize)
            && !ShieldService.isPlayerShielded(playersList.get(0))
            ) {
            /* menembak */

            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = RadarService.getHeadingBetween(bot, playersList.get(0));

            this.playerAction = playerAction;

            return;
        }

        // KASUS PINDAH 1
        
        List<GameObject> biggerPlayer = PlayerService.getBiggerPlayerInRange(gameState, bot, Math.min(PlayerService.playerDangerRange, gameState.world.radius / 5));
        if (!biggerPlayer.isEmpty() || (!incomingTeleports.isEmpty() && maxEnemySize + PlayerService.sizeDifferenceOffset > bot.size))
        {
            temp = PlayerService.getEscapePlayerVector(gameState, biggerPlayer, incomingTeleports, bot); /*isi temp dengan nilai arah kabur dari bot besar */
            t = new EscapeInfo(temp, weights[0]);
            directionVectors.add(t);
            // System.out.println("2");
        }

        /* AFTERBURNER */
        /* OFFENSIVE */

        List<GameObject> preys = PlayerService.getPreys(gameState, bot, PlayerService.sizeDifferenceOffset, huntingRange - 100);

        /* Kalau belum nyala */
        /*
         * Kalau ada preys dan
         * gada player lebih gede di sekitar dan
         * sedang tidak afterburner
         */
        if (!preys.isEmpty() && directionVectors.isEmpty() && !isAfterburner) {

            double distance = RadarService.getRealDistance(bot, preys.get(0));
            double tick = distance / ((bot.getSpeed() * 2) - preys.get(0).getSpeed());

            if ((bot.getSize() - 2 * tick) > (preys.get(0).getSize() + PlayerService.sizeDifferenceOffset)) {
                playerAction.action = PlayerActions.STARTAFTERBURNER;
                playerAction.heading = RadarService.getHeadingBetween(bot, preys.get(0));

                this.playerAction = playerAction;

                isAfterburner = true;
                System.out.println("AFTERBURNER");
                return;
            }
        }

        /* Kalau sedang nyala */
        /*
         * (Kalau ada player lebih gede di sekitar dan
         * sedang afterburner) atau
         * (gaada preys di sekitar dan sedang afterburner)
         */
        if ((!directionVectors.isEmpty() || preys.isEmpty()) && isAfterburner) {

            playerAction.action = PlayerActions.STOPAFTERBURNER;
            this.playerAction = playerAction;
            System.out.println("STOP AFTERBURNER");

            isAfterburner = false;

            return;
        }

        /*
         * Kalau ga ada player lebih gede di sekitar dan
         * lagi ngejar preys dan
         * sedang afterburner
         */

        if (isAfterburner) {
            double distance = RadarService.getRealDistance(bot, preys.get(0));
            double tick = distance / ((bot.getSpeed() * 2) - preys.get(0).getSpeed());

            /* Kalau ternyata malah bahaya bisa mati */
            if ((bot.getSize() - 2 * tick) <= (preys.get(0).getSize() + PlayerService.sizeDifferenceOffset)) {
                playerAction.action = PlayerActions.STOPAFTERBURNER;
                this.playerAction = playerAction;

                System.out.println("STOP AFTERBURNER");
                isAfterburner = false;

                return;
            } else { /* Kalau aman */
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = RadarService.getHeadingBetween(bot, preys.get(0));

                this.playerAction = playerAction;

                System.out.println("LANJUT AFTERBURNER");

                return;
            }
        }


        List<GameObject> incomingTorpedo = TorpedoService.getIncomingTorpedo(gameState, bot);

        temp = calculateResult(directionVectors);
        Double offsetAngle = Math.abs(temp.getAngleTo(RadarService.degreeToVector(bot.getHeading())));
        if ((temp.isZero() || offsetAngle < headingOffset) && !incomingTorpedo.isEmpty())
        /* jika ada torpedo yang mengarah ke kita */
        {
            /* jika torpedo (terdekat) di dalam danger zone kita */
            if (TorpedoService.fireTorpedoWhenDanger(bot, incomingTorpedo.get(0))) {
                if (ShieldService.isShieldAvailable(bot, 60)
                    && RadarService.getRealDistance(bot, incomingTorpedo.get(0)) <= 60) {
                    
                    playerAction.action = PlayerActions.ACTIVATESHIELD;

                    this.playerAction = playerAction;
                    return;
                }
                
                if (TorpedoService.isTorpedoAvailable(bot, 25)) {
                    /* defend with shooting */
                    playerAction.action = PlayerActions.FIRETORPEDOES;

                    // playerAction.heading = titik temu torpedo kita dengan torpedo musuh
                    playerAction.heading = RadarService.getHeadingBetween(bot, incomingTorpedo.get(0));
                    this.playerAction = playerAction;

                    // System.out.println("3");
                    return;
                } else {
                    /* 
                    SHIELD USE
                    2 torpedo = 20, shield = 20
                    jika ada >= 2 torpedo datang && jaraknya sudah lumayan dekat
                    saat di state ini prioritas lebih rendah dari defend with shooting
                    */

                    if (
                        incomingTorpedo.size() >= 2 
                        && ShieldService.isShieldAvailable(bot, 40) 
                        && RadarService.isCollapsing(bot, incomingTorpedo.get(0), 60)) {
                        playerAction.action = PlayerActions.ACTIVATESHIELD;

                        System.out.println("SHIELD SHIELD " + incomingTorpedo.size());

                        this.playerAction = playerAction;
                        return;
                    }
                }
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
            // System.out.println("4");
        }

        // KASUS PINDAH 3

        // List<GameObject> preys = PlayerService.getPreys(gameState, bot, PlayerService.sizeDifferenceOffset, huntingRange);
        if (!preys.isEmpty() || (!incomingTeleports.isEmpty() && maxEnemySize + PlayerService.sizeDifferenceOffset < bot.size))
        {
            temp = PlayerService.getChasePlayerVector(preys, incomingTeleports, bot, maxEnemySize);// isi temp dengan nilai arah KEJAR musuh */
            t = new EscapeInfo(temp, weights[2]);
            directionVectors.add(t);

            // System.out.println("5");
        }

        // KASUS  PINDAH 4

        // jika keluar map
        if (FieldService.isOutsideMap(gameState, bot, RadarService.worldRadiusOffset))
        {
            temp = RadarService.degreeToVector(FieldService.getCenterDirection(gameState, bot));
            t = new EscapeInfo(temp, weights[3]);

            directionVectors.add(t);
            // System.out.println("6");

        }

        // KASUS PINDAH 5
        
        List<GameObject> incomingSupernova = SupernovaService.getIncomingSupernova(gameState, bot);

        if (!incomingSupernova.isEmpty() && incomingSupernova != null)
        /*ada supernova bomb mengarah ke kita */
        {
            temp = new WorldVector(); // isi dengan nilai arah kabur dari supernova bomb */
            temp = SupernovaService.nextHeadingAfterProjectile(gameState, bot, incomingSupernova.get(0));
            t = new EscapeInfo(temp, weights[4]);

            directionVectors.add(t);
            // System.out.println("7");
        }
        
        // KASUS PINDAH 6
        // jika masuk cloud

        List<GameObject> collapsingClouds = FieldService.getCollapsingClouds(bot, fieldRadarRadius);

        if (!collapsingClouds.isEmpty())
        {
            
            temp = FieldService.getHeadingEscape(gameState, bot, collapsingClouds);

            t = new EscapeInfo(temp, weights[5]);
            directionVectors.add(t);
            // System.out.println("8");
            
        }

        // KASUS PINDAH 7
        // jika masuk asteroid
        List<GameObject> collapsingAsteroids = FieldService.getCollapsingAsteroids(bot, fieldRadarRadius);
        if (!collapsingAsteroids.isEmpty())
        {

            temp = FieldService.getHeadingEscape(gameState, bot, collapsingAsteroids);

            t = new EscapeInfo(temp, weights[6]);
            directionVectors.add(t);
            // System.out.println("9");
            
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
                // System.out.println("10");

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
                // System.out.println("11");

                return;
            }
        }

        // KASUS SELANJUTNYA ADALAH KASUS TIDAK ADA YANG PERLU DIKEJAR ATAU DIHINDARI

        if (SupernovaService.isSupernovaPickupExist(gameState, bot, fieldRadarRadius) && SupernovaService.isBotNearestfromPickup(gameState, bot))
        {
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = RadarService.getHeadingBetween(bot, SupernovaService.getSupernovaPickupObject(gameState));
            this.playerAction = playerAction;
            // System.out.println("13");

            tickTimer = 10;

            return;
        }

        if (SupernovaService.isSupernovaAvailable(bot) && !SupernovaService.isFired && tickTimer <= 0)
        /*punya supernova pickup */
        {

            // players sudah terurut dari terdekat
            List<GameObject> players = PlayerService.getOtherPlayerList(gameState, bot);

            int maxWeight = 0;
            GameObject tempTarget = null;
            
            for (GameObject player : players)
            {
                if (player.size > maxWeight)
                {
                    maxWeight = player.size;
                    tempTarget = player;
                }
            }

            if (maxWeight > 0)
            {   
                playerAction.action = PlayerActions.FIRESUPERNOVA;
                playerAction.heading = RadarService.getHeadingBetween(bot, tempTarget);
                SupernovaService.isFired = true;

                this.playerAction = playerAction;
                return;
            }
        }

        List<GameObject> foods;
        
        if (!TeleportService.isFired && TeleportService.isTeleportAvailable(bot))
        {
            foods = FoodServices.getAllFoods(bot, fieldRadarRadius);
        }
        
        else
        {
            foods = FoodServices.getAllFoods(gameState, bot);
        }

        // playerAction.heading = arah ke TARGET

        if (foods.size() > 0 && foods != null)
        {
            // System.out.println("15");
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = RadarService.getHeadingBetween(bot, foods.get(0));
        }

        else if (!TeleportService.isFired && TeleportService.isTeleportAvailable(bot))
        {

            foods = FoodServices.getAllFoods(gameState, bot);

            if (!foods.isEmpty())
            {
                playerAction.action = PlayerActions.TELEPORT;
                playerAction.heading = RadarService.roundToEven(RadarService.vectorToDegree(TeleportService.escapeDirection(gameState, bot)));
                tpTimer = gameState.world.radius * 3 / TeleportService.teleporterSpeed;

                TeleportService.isAttacking = false;
            }
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