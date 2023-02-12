package Services;

import Enums.*;
import Models.*;

import java.util.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    static private boolean isDetonated = false;

    class EscapeInfo {
        public WorldVector escapeDirection;
        public Double weight;

        EscapeInfo(WorldVector escapeDirection, Double weight) {
            this.escapeDirection = escapeDirection;
            this.weight = weight; 
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

        List<EscapeInfo> directionVectors = new ArrayList<EscapeInfo>();
        WorldVector temp;
        EscapeInfo t;
        List<Boolean> effectList = Effects.getEffectList(bot.effectsCode);

        // weight untuk setiap kasus kabur/ngejar
        Double[] weights = {
            0.0, 
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0,
            0.0
        };

        if (isSupernovaNearPlayer(gameState, bot) && isDetonated)
        /* jika kita sudah nembak supernova dan supernova bombnya dekat musuh */
        {
            playerAction.action = PlayerActions.DETONATESUPERNOVA;

            /* set variabel sudah nembak supernova menjadi false */
            isDetonated = false;

            return;
        }

        // KASUS PINDAH 1
        int dangerRange = 20; // Range player gede dianggap berbahaya, ganti kalau perlu
        List<GameObject> biggerPlayer = PlayerService.getBiggerPlayerInRange(gameState, bot, dangerRange)
        if (!biggerPlayer.isEmpty())
        {
            temp = PlayerService.getEscapePlayerVector(biggerPlayer, bot); /*isi temp dengan nilai arah kabur dari bot besar */
            t = new EscapeInfo(temp, weights[0]);
            directionVectors.add(t);
        }

        List<GameObject> incomingTorpedo = TorpedoService.getIncomingTorpedo(gameState, bot);
        if (directionVectors.isEmpty() && !incomingTorpedo.isEmpty())
        /* jika ada torpedo yang mengarah ke kita */
        {
            /* jika torpedo (terdekat) di dalam danger zone kita */
            if (TorpedoService.fireTorpedoWhenDanger(bot, incomingTorpedo.get(0)) && TorpedoService.isTorpedoAvailable(bot, 20)) {
                playerAction.action = PlayerActions.FIRETORPEDOES;

                // playerAction.heading = titik temu torpedo kita dengan torpedo musuh
                playerAction.heading = RadarService.getHeadingBetween(bot, incomingTorpedo.get(0)); // ini belum predict
                this.playerAction = playerAction;
                return;
            }
        }

        // KASUS PINDAH 2

        if (!incomingTorpedo.isEmpty())
        /* jika torpedo terdetect mengarah ke kita tetapi bukan dalam danger zone */
        {
            temp = new WorldVector();// temp = nilai arah kabur dari torpedo */
            temp = TorpedoService.nextHeadingAfterTorpedo(bot, incomingTorpedo);
            t = new EscapeInfo(temp, weights[1]);
            directionVectors.add(t);
        }

        // KASUS PINDAH 3

        int offset = 10; // Minimal selisih size player
        List<GameObject> preys = PlayerService.getPreys(gameState, bot, offset);
        if (!preys.isEmpty())
        {
            temp = PlayerService.getChasePlayerVector(preys, bot);// isi temp dengan nilai arah KEJAR musuh */
            t = new EscapeInfo(temp, weights[2]);
            directionVectors.add(t);
        }

        // KASUS PINDAH 4

        if (false /* jika berada di luar radius world */)
        {
            temp = new WorldVector(); // isi dengan nilai arah KEJAR musuh */
            t = new EscapeInfo(temp, weights[3]);
            directionVectors.add(t);
        }

        // KASUS  PINDAH 5

        // jika keluar map
        if (FieldService.isOutsideMap(gameState, bot, 5))
        {
            temp = RadarService.degreeToVector(FieldService.getCenterDirection(gameState, bot));
            t = new EscapeInfo(temp, weights[4]);

            directionVectors.add(t);
        }

        // KASUS PINDAH 6
        if (false /*ada supernova bomb mengarah ke kita */)
        {
            temp = new WorldVector(); // isi dengan nilai arah kabur dari supernova bomb */
            t = new EscapeInfo(temp, weights[5]);

            directionVectors.add(t);
        }

        
        if (false /* tembak gascloud kalo kita kena gascloud dan size gascloud <= limit ???*/)
        {
            // pilih gascloud yg mau ditembak
            if (false /*cek apakah nembak tidak bakal bunuh diri serta cloud yg dipilih emang bisa ditembak (tidak terhalang*/)
            {
                playerAction.action = PlayerActions.FIRETORPEDOES;
                // PlayerAction.heading = arah ke cloud yg mw ditembak
            }
        }

        // KASUS PINDAH 7
        // jika masuk cloud
        if (FieldService.isCloudCollapsing(bot))
        {
            List<GameObject> collapsingClouds = FieldService.getCollapsingClouds(gameState, bot);

            List<Integer> tempDirection = FieldService.getHeadingEscape(bot, collapsingClouds);

            if (tempDirection.size() > 0)
            {
                temp = RadarService.degreeToVector(RadarService.roundToEven(FieldService.getHeadingEscape(bot, collapsingClouds).get(0))); // isi dengan nilai arah kabur dari supernova bomb */
                t = new EscapeInfo(temp, weights[6]);
                directionVectors.add(t);
            }
        }

        // KASUS PINDAH 8
        // jika masuk asteroid
        if (FieldService.isAsteroidCollapsing(bot))
        {
            List<GameObject> collapsingAsteroids = FieldService.getCollapsingAsteroids(gameState, bot);

            List<Integer> tempDirection = FieldService.getHeadingEscape(bot, collapsingAsteroids);

            if (tempDirection.size() > 0)
            {
                temp = RadarService.degreeToVector(RadarService.roundToEven(FieldService.getHeadingEscape(bot, collapsingAsteroids).get(0))); // isi dengan nilai arah kabur dari supernova bomb */
                t = new EscapeInfo(temp, weights[6]);
                directionVectors.add(t);
            }
        }

        // KASUS PINDAH 9   
        // jika punya superfood
        if (effectList.get(3))
        {
            var foods = FoodServices.getNearestFoods(gameState, bot);

            if (foods.size() > 0)
            {
                temp = RadarService.degreeToVector(RadarService.getHeadingBetween(bot, foods.get(0))); // isi dengan nilai arah kabur dari supernova bomb */
                t = new EscapeInfo(temp, weights[8]);
                directionVectors.add(t);
            }
        }

        // PERHITUNGAN PERPINDAHAN BERDASARKAN TIAP WEIGHT
        if (directionVectors.size() > 0)
        {
            WorldVector res = new WorldVector();

            for (EscapeInfo v : directionVectors)
            {
                res.add(v.escapeDirection.mult(v.weight));
            }

            if (!res.isZero())
            {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = RadarService.roundToEven(RadarService.vectorToDegree(res));
                this.playerAction = playerAction;
                return;
            }
        }

        // KASUS SELANJUTNYA ADALAH KASUS TIDAK ADA YANG PERLU DIKEJAR ATAU DIHINDARI

        // CARI SUPER FOOD
        List<GameObject> superFoods = FoodServices.getSuperFoods(gameState, bot);
        if (!superFoods.isEmpty())
        {
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = RadarService.getHeadingBetween(bot, superFoods.get(0));
            this.playerAction = playerAction;
            return;
        }

   
        if (SupernovaService.isSupernovaPickupExist(gameState) && SupernovaService.isBotNearestfromPickup(gameState, bot))
        {
            playerAction.action = PlayerActions.FORWARD;
            playerAction.heading = RadarService.getHeadingBetween(bot, SupernovaService.getSupernovaPickupObject(gameState));
            this.playerAction = playerAction;
            return;
        }

    
        if (isSupernovaAvailable(bot))
        /*punya supernova pickup */
        {
            playerAction.action = PlayerActions.FIRESUPERNOVA;

            // players sudah terurut dari terkecil
            List<GameObject> players = getOtherPlayerList(gameState, bot);

            for (int i = 0; i < players.size(); i ++) {
                if (RadarService.isCollapsing(players.get(i), bot, 50)) {
                    // playerAction.heading = arah ke TARGET
                    playerAction.heading = RadarService.getHeadingBetween(bot, players.get(i));
                    break;
                }
            }
            
            isDetonated = true;

            this.playerAction = playerAction;
            return;
        }

        var foods = FoodServices.getNearestFoods(gameState, bot);
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = bot.getHeading();
        // playerAction.heading = arah ke TARGET

        if (foods.size() > 0)
        {
            playerAction.heading = RadarService.getHeadingBetween(bot, foods.get(0));
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


}
