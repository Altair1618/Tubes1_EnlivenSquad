package Services;

import Enums.*;
import Models.*;

import java.util.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

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

        if (false /* jika kita sudah nembak supernova dan supernova bombnya dekat musuh */)
        {
            playerAction.action = PlayerActions.DETONATESUPERNOVA;
            /* set variabel sudah nembak supernova menjadi false */

            return;
        }


        // KASUS PINDAH 1
        if (false /* jika ada bot lebih besar yang berada di rentang radar imaginary kita */)
        {
            temp = new WorldVector();/*isi temp dengan nilai arah kabur dari bot besar */
            t = new EscapeInfo(temp, weights[0]);
            directionVectors.add(t);
        }

        if (directionVectors.isEmpty() /* && jika torpedo di dalam danger zone kita */)
        {
            playerAction.action = PlayerActions.FIRETORPEDOES;
            // playerAction.heading = titik temu torpedo kita dengan torpedo musuh
            this.playerAction = playerAction;
            return;
        }

        // KASUS PINDAH 2

        if (false /* jika torpedo terdetect mengarah ke kita tetapi bukan dalam danger zone */)
        {
            temp = new WorldVector();// isi temp dengan nilai arah kabur dari torpedo */
            t = new EscapeInfo(temp, weights[1]);
            directionVectors.add(t);
        }

        // KASUS PINDAH 3

        if (false /* jika ada player kecil yang dekat utk dimakan */)
        {
            temp = new WorldVector();// isi temp dengan nilai arah KEJAR musuh */
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
        if (false /*ada superfood */)
        {
            
            playerAction.action = PlayerActions.FORWARD;
            // playerAction.heading = arah ke superfood
            this.playerAction = playerAction;
            return;
        }

   
        if (false /* ada supernova pickup*/)
        {
            playerAction.action = PlayerActions.FORWARD;
            // playerAction.heading = arah ke supernova pickup
            this.playerAction = playerAction;
            return;
        }

    
        if (false /*punya supernova pickup */)
        {
           playerAction.action = PlayerActions.FIRESUPERNOVA;
            // playerAction.heading = arah ke TARGET
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
