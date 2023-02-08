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

    public List<GameObject> getOtherObjects(GameState gameState, GameObject bot, ObjectTypes objectType)
    {
        var objectList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == objectType)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        return objectList;
    }

    public List<GameObject> getOtherObjects(GameState gameState, Position position)
    {
        var objectList = gameState.getGameObjects()
                .stream()
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(item, position)))
                .collect(Collectors.toList());

        return objectList;
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
        // mengembalikan jarak dua objek
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public double getDistanceBetween(GameObject object, Position p) {
        // mengembalikan jarak dua objek
        var triangleX = Math.abs(object.getPosition().x - p.x);
        var triangleY = Math.abs(object.getPosition().y - p.y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    public int getHeadingBetween(GameObject bot, GameObject otherObject) {
        // mengembalikan arah (global, bukan lokal) menuju otherObject (dalam derajat) 
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    public long getRoundedDistance(GameObject object1, GameObject object2)
    {
        // mengembalikan jarak dua objek yang dibulatkan dengan roundToEven
        double res = getDistanceBetween(object1, object2);

        return roundToEven(res);
    }

    public Position nextPosition(int heading, GameObject bot)
    {
        int speed = (int) Math.ceil(200.0f / bot.getSize());
        double rad = heading * Math.PI / 180;
        return new Position(roundToEven(bot.getPosition().x + speed * Math.cos(rad)), roundToEven(bot.getPosition().y + speed * Math.sin(rad)));
    }

    public boolean isCollapsing(GameObject object1, GameObject object2)
    {
        // mengembalikan true jika object1 dan object2 collapse
        long distance = getRoundedDistance(object1, object2);

        return (object1.size + object2.size > distance);
    }

    public boolean isCollapsing(GameObject object, Position p, Integer size)
    {
        // mengembalikan true jika object1 dan object2 collapse
        long distance = roundToEven(getDistanceBetween(object, p));

        return (object.size + size > distance);
    }

    public List<GameObject> getCollapsingObjects(GameState gameState, Position position, Integer size)
    {
        // mengembalikan objek-objek bertipe tertentu yang sedang collapse dengan bot 
        List<GameObject> objectList = getOtherObjects(gameState, position);
        List<GameObject> collapsingObjects = new ArrayList<GameObject>();

        objectList.forEach((obj) -> {
            if (isCollapsing(obj, position, size)) collapsingObjects.add(obj);
        });

        return collapsingObjects;

    }

    public List<GameObject> getCollapsingObjects(GameState gameState, GameObject bot, ObjectTypes type)
    {
        // mengembalikan objek-objek bertipe tertentu yang sedang collapse dengan bot 
        List<GameObject> objectList = getOtherObjects(gameState, bot, type);
        List<GameObject> collapsingObjects = new ArrayList<GameObject>();

        objectList.forEach((obj) -> {
            if (isCollapsing(obj, bot)) collapsingObjects.add(obj);
        });

        return collapsingObjects;

    }

    private int roundToEven(double v) {
        
        // standar pembulatan engine
        // contoh : 24.5 dibulatin ke 24, 25.5 dibulatin ke 26, sedangkan yang bukan desimal 0.5 akan dibulatin seperti biasa
        long res = Math.round(v);

        double des = res - v;

        if (Math.abs(des - 0.5) < Math.ulp(1.0) && res % 2 == 1)
        {
            res--;
        }

        return (int) res;
    }
    private int toDegrees(double v) {
        // radiant to degree
        return (int) (v * (180 / Math.PI));
    }
}
