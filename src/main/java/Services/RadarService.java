package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class RadarService {

    static public List<GameObject> getOtherPlayerList(GameState gameState, GameObject bot) {
        // mengembalikan list player lainnya

        return gameState.getPlayerGameObjects()
                .stream().filter(item -> item.getId() != bot.getId())
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());
    }

    static public GameObject getNearestPlayer(GameState gameState, GameObject bot) {
        return getOtherPlayerList(gameState, bot).get(0);
    }

    static public List<GameObject> getOtherObjects(GameState gameState, GameObject bot, ObjectTypes objectType)
    {
        // mengembalikan objek-objek lain bertipe tertentu dan diurutkan berdasarkan jarak terhadap bot 
        var objectList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == objectType)
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        return objectList;
    }

    static public List<GameObject> getOtherObjects(GameState gameState, Position position)
    {
        // mengembalikan objek-objek lain dan diurutkan berdasarkan jarak terhadap position 
        var objectList = gameState.getGameObjects()
                .stream()
                .sorted(Comparator
                        .comparing(item -> getDistanceBetween(item, position)))
                .collect(Collectors.toList());

        return objectList;
    }
    
    static public int getOtherPlayerHeading(GameObject otherBot) {
        return otherBot.getHeading();
    }

    static public double getDistanceBetween(GameObject object1, GameObject object2) {
        // mengembalikan jarak dua objek
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    static public double getDistanceBetween(GameObject object, Position p) {
        // mengembalikan jarak dua objek
        var triangleX = Math.abs(object.getPosition().x - p.x);
        var triangleY = Math.abs(object.getPosition().y - p.y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    static public int getHeadingBetween(GameObject bot, GameObject otherObject) {
        // mengembalikan arah (global, bukan lokal) menuju otherObject (dalam derajat) 
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    static public long getRoundedDistance(GameObject object1, GameObject object2)
    {
        // mengembalikan jarak dua objek yang dibulatkan dengan roundToEven
        double res = getDistanceBetween(object1, object2);

        return roundToEven(res);
    }

    static public Position nextPosition(int heading, GameObject bot)
    {
        // mengembalikan prediksi posisi bot pada tik berikutnya
        int speed = (int) Math.ceil(200.0f / bot.getSize());

        if (Effects.getEffectList(bot.effectsCode).get(1)) speed /= 2;

        double rad = heading * Math.PI / 180;
        return new Position(roundToEven(bot.getPosition().x + speed * Math.cos(rad)), roundToEven(bot.getPosition().y + speed * Math.sin(rad)));
    }

    static public boolean isCollapsing(GameObject object1, GameObject object2)
    {
        // mengembalikan true jika object1 dan object2 collapse
        long distance = getRoundedDistance(object1, object2);

        return (object1.size + object2.size > distance);
    }

    static public boolean isCollapsing(GameObject object, Position p, Integer size)
    {
        // mengembalikan true jika object1 dan object2 collapse
        long distance = roundToEven(getDistanceBetween(object, p));

        return (object.size + size > distance);
    }

    static public List<GameObject> getCollapsingObjects(GameState gameState, Position position, Integer size)
    {
        // mengembalikan objek-objek bertipe tertentu yang sedang collapse dengan bot 
        List<GameObject> objectList = getOtherObjects(gameState, position);
        List<GameObject> collapsingObjects = new ArrayList<GameObject>();

        objectList.forEach((obj) -> {
            if (isCollapsing(obj, position, size)) collapsingObjects.add(obj);
        });

        return collapsingObjects;

    }

    static public List<GameObject> getCollapsingObjects(GameState gameState, GameObject bot, ObjectTypes type)
    {
        // mengembalikan objek-objek bertipe tertentu yang sedang collapse dengan bot 
        List<GameObject> objectList = getOtherObjects(gameState, bot, type);
        List<GameObject> collapsingObjects = new ArrayList<GameObject>();

        objectList.forEach((obj) -> {
            if (isCollapsing(obj, bot)) collapsingObjects.add(obj);
        });

        return collapsingObjects;

    }

    static private int roundToEven(double v) {
        
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
    static private int toDegrees(double v) {
        // radiant to degree
        return (int) (v * (180 / Math.PI));
    }
}
