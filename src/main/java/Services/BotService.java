package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

import Services.RadarService;

public class BotService {
    private GameObject bot;
    private PlayerAction playerAction;
    private GameState gameState;
    static private RadarService radarService = new RadarService();

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

    public void setPlayerHeading(int heading) {
        playerAction.heading = heading;
    }


    public void computeNextPlayerAction(PlayerAction playerAction) {
        playerAction.action = PlayerActions.FORWARD;
        playerAction.heading = new Random().nextInt(360);

        if (!gameState.getGameObjects().isEmpty()) {
            GameObject nearestPlayer = radarService.getNearestPlayer(gameState, bot);
            int nextHeading = radarService.getHeadingBetween(bot, nearestPlayer);
            setPlayerHeading(nextHeading);
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
