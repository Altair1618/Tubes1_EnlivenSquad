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
        return gameState.getPlayerGameObjects()
                .stream().filter(item -> item.getId() != bot.getId())
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
    }

    public GameObject getNearestPlayer(GameState gameState, GameObject bot) {
        return getOtherPlayerList(gameState, bot).get(0);
    }

    public int getOtherPlayerHeading(GameObject otherBot) {
        return otherBot.getHeading();
    }

    public boolean isSupernovaPickupExist(GameState gameState) {
        // Mengecek apakah terdapat supernova pickup di world
        var SupernovaPickup = gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP).collect(Collectors.toList());
        return !SupernovaPickup.isEmpty();
    }

    public GameObject getSupernovaPickupObject(GameState gameState) {
        // I.S Supernova Pickup Exist
        var SupernovaPickup = gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVAPICKUP).collect(Collectors.toList());
        return SupernovaPickup.get(0);
    }

    public GameObject getNearestPlayerFromSupernovaPickup(GameState gameState) {
        // I.S Supernova Pickup Exist
        var distanceList = gameState.getPlayerGameObjects()
                .stream()
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(getSupernovaPickupObject(gameState), item)))
                .collect(Collectors.toList());

        return distanceList.get(0);
    }

    public boolean isSupernovaBombExist(GameState gameState) {
        // Mengecek apakah terdapat bom supernova di world
        var SupernovaPickup = gameState.getGameObjects().stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB).collect(Collectors.toList());
        return !SupernovaPickup.isEmpty();
    }

    public List<GameObject> getSupernovaBombs(GameState gameState) {
        // I.S Supernova Pickup Exist
        var bombList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERNOVABOMB)
                .collect(Collectors.toList());

        return bombList;
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
