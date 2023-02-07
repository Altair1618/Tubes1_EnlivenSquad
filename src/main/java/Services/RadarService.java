package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class RadarService {

    public GameObject getNearestFood(GameState gameState, GameObject bot) {
        var foodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        return foodList.get(0);
    }

    public List<GameObject> getOtherPlayerList(GameState gameState, GameObject bot) {
        var playerList = gameState.getPlayerGameObjects()
                .stream().filter(item -> item.getId() != bot.getId())
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        return playerList;
    }

    public GameObject getNearestPlayer(GameState gameState, GameObject bot) {
        return getOtherPlayerList(gameState, bot).get(0);
    }

    public double getDistanceBetween(GameObject object1, GameObject object2) {
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public int getHeadingBetween(GameObject bot, GameObject otherObject) {
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    public int toDegrees(double v) {
        return (int) (v * (180 / Math.PI));
    }
}
