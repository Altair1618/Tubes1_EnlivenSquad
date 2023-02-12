package Services;

import Enums.*;
import Models.*;

import java.util.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;

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

        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        List<GameObject> foods = FoodServices.getFoods(gameState, bot);
        List<GameObject> superfoods = FoodServices.getSuperFoods(gameState, bot);

        if (!foods.isEmpty()) {
            setHeading(RadarService.getHeadingBetween(bot, foods.get(0)));
        }
        if (!superfoods.isEmpty()) {     
            playerAction.action = PlayerActions.FORWARD;
            setHeading(RadarService.getHeadingBetween(bot, superfoods.get(0)));
            System.out.println("eat !!");
        }
        
        List<GameObject> players = PlayerService.getOtherPlayerList(gameState, bot);
        if (TorpedoService.isTorpedoAvailable(bot, 15)) {
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = RadarService.getHeadingBetween(bot, players.get(0));
            System.out.println("fire torpedo");
        }

        List<GameObject> incomingTorpedo = TorpedoService.getIncomingTorpedo(gameState, bot);
        if (incomingTorpedo.size() != 0) {
            if (TorpedoService.fireTorpedoWhenDanger(bot, incomingTorpedo.get(0)) && TorpedoService.isTorpedoAvailable(bot, 20)) {
                playerAction.action = PlayerActions.FIRETORPEDOES;
                playerAction.heading = RadarService.getHeadingBetween(bot, incomingTorpedo.get(0));
                System.out.println("shoot danger torpedo");
            } else {
                playerAction.action = PlayerActions.FORWARD;
                playerAction.heading = TorpedoService.nextHeadingAfterTorpedo(bot, incomingTorpedo);
                System.out.println("RUN RUN RUN RUN from torpedo");
            }
        }

        // if (!gameState.getGameObjects().isEmpty()) {
        //     if (supernovaService.isSupernovaPickupExist(gameState)) {
        //         setHeading(RadarService.getHeadingBetween(bot, supernovaService.getSupernovaPickupObject(gameState)));
        //         System.out.println("Mengejar supernova");
        //     }
        // //    else {
        // //        setHeading(radarService.getHeadingBetween(bot, radarService.getNearestFood(gameState, bot)));
        // //    }
        // }

        // if (Effects.getEffectList(bot.effectsCode).get(0)) System.out.println("ON!\n");
        // System.out.println("size: ");
        // System.out.println(bot.size);
        // System.out.println("\n");
        // System.out.println("speed: ");
        // System.out.println(bot.speed);
        // System.out.println("\n");

        // if (bot.size >= 20 && !Effects.getEffectList(bot.effectsCode).get(0))
        // {
        //     System.out.println("ON!\n");
        //     playerAction.action = PlayerActions.STARTAFTERBURNER;
        //     return;
            
        // }
        
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
