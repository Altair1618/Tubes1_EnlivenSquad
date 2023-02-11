package Services;

import Enums.*;
import Models.*;

import java.util.*;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    
    private GameObject firedTorpedo;

    public BotService() {
        this.playerAction = new PlayerAction();
        this.gameState = new GameState();
        this.firedTorpedo = null;
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

        List<GameObject> players = RadarService.getOtherPlayerList(gameState, bot);
        if (bot.getTorpedoSalvoCount() >= 2) {
            playerAction.action = PlayerActions.FIRETORPEDOES;
            playerAction.heading = RadarService.getHeadingBetween(bot, players.get(0));
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


        if (!foods.isEmpty())
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
