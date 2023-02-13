package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class RadarService {
    static public List<GameObject> getOtherObjects(GameState gameState, GameObject bot, ObjectTypes objectType)
    {
        // mengembalikan objek-objek lain bertipe tertentu dan diurutkan berdasarkan jarak terhadap bot
        var objectList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == objectType)
                .sorted(Comparator
                        .comparing(item -> getRealDistance(bot, item)))
                .collect(Collectors.toList());

        return objectList;
    }

    static public List<GameObject> getOtherObjects(GameState gameState, ObjectTypes objectType)
    {
        // mengembalikan objek-objek lain bertipe tertentu dan diurutkan berdasarkan jarak terhadap bot 
        var objectList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == objectType)
                .collect(Collectors.toList());

        return objectList;
    }

    static public List<GameObject> getOtherObjects(GameState gameState, Position position) {
        // mengembalikan objek-objek lain dan diurutkan berdasarkan jarak terhadap position 
        var objectList = gameState.getGameObjects()
                .stream()
                .sorted(Comparator
                        .comparing(item -> getRealDistance(item.size, 0, (getDistanceBetween(item, position)))))
                .collect(Collectors.toList());

        return objectList;
    }

    static public List<GameObject> getNearestOtherObjects(GameState gameState, GameObject bot, ObjectTypes type)
    {
        List<GameObject> objects = getOtherObjects(gameState, bot, type);
        List<GameObject> res = new ArrayList<GameObject>();

        if (objects.isEmpty()) return res;

        res.add(objects.get(0));

        int i = 1;
        double distance = RadarService.getRealDistance(bot, objects.get(0));

        while (i < objects.size() && RadarService.getRealDistance(bot, objects.get(i)) == distance)
        {
            res.add(objects.get(i));
        }

        return res.stream()
            .sorted(Comparator
                    .comparing(item -> item.id))
            .collect(Collectors.toList());
    }

    static public double getRealDistance(int radius1, int radius2, double distance)
    {
        // can return negative distance if collapsing
        // smaller means the center is closer when collapsing
        return distance - radius1 - radius2;

    }

    static public double getRealDistance(GameObject object1, GameObject object2)
    {
        // can return negative distance if collapsing
        // smaller means the center is closer when collapsing
        return getRealDistance(object1.size, object2.size, getDistanceBetween(object1, object2));

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

    static public double getDistanceBetween(Position p1, Position p2) {
        // mengembalikan jarak dua objek
        var triangleX = Math.abs(p1.x - p2.x);
        var triangleY = Math.abs(p1.y - p2.y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    static public int getHeadingBetween(GameObject bot, GameObject otherObject) {
        // mengembalikan arah (global, bukan lokal) menuju otherObject (dalam derajat) 
        var direction = toDegrees(Math.atan2(otherObject.getPosition().y - bot.getPosition().y,
                otherObject.getPosition().x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    static public int getHeadingBetween(GameObject bot, Position p) {
        // mengembalikan arah (global, bukan lokal) menuju p (dalam derajat) 
        var direction = toDegrees(Math.atan2(p.y - bot.getPosition().y,
                p.x - bot.getPosition().x));
        return (direction + 360) % 360;
    }

    static public int getHeadingBetween(Position p1, Position p2) {
        // mengembalikan arah (global, bukan lokal) menuju p2 dari p1 (dalam derajat) 
        var direction = toDegrees(Math.atan2(p2.y - p1.y,
                p2.x - p1.x));
        return (direction + 360) % 360;
    }

    static public Position nextPosition(int heading, GameObject bot)
    {
        // mengembalikan prediksi posisi bot pada tik berikutnya
        int speed = bot.speed;

        // List<Boolean> effectList = Effects.getEffectList(bot.effectsCode);

        // if (effectList.get(0)) speed *= 2;
        // if (effectList.get(1)) speed /= 2;

        double rad = heading * Math.PI / 180;
        return new Position(roundToEven(bot.getPosition().x + speed * Math.cos(rad)), roundToEven(bot.getPosition().y + speed * Math.sin(rad)));
    }

    static public boolean isCollapsing(GameObject object1, GameObject object2)
    {
        // mengembalikan true jika object1 dan object2 collapse
        long distance = roundToEven(getDistanceBetween(object1, object2));

        return (object1.size + object2.size > distance);
    }

    static public boolean isCollapsing(GameObject object1, GameObject object2, int offset)
    {
        // mengembalikan true jika object1 dan object2 collapse
        long distance = roundToEven(getDistanceBetween(object1, object2));

        return (object1.size + object2.size + offset > distance);
    }

    static public boolean isCollapsing(GameObject object, Position p, Integer size)
    {
        // mengembalikan true jika object1 dan object2 collapse
        long distance = roundToEven(getDistanceBetween(object, p));

        return (object.size + size > distance);
    }

    static public boolean isCollapsing(GameObject object, Position p, Integer size, int offset)
    {
        // mengembalikan true jika object1 dan object2 collapse
        long distance = roundToEven(getDistanceBetween(object, p));

        return (object.size + size + offset > distance);
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

    static public double getDistanceFromZero(GameObject object, GameState gameState) {
        // Mengembalikan Jarak Objek dari Center
        Position center = gameState.getWorld().getCenterPoint();
        var triangleX = Math.abs(object.getPosition().getX() - center.getX());
        var triangleY = Math.abs(object.getPosition().getY() - center.getY());
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    static public double getDistanceFromZero(Position p, GameState gameState) {
        // Mengembalikan Jarak Objek dari Center
        Position center = gameState.getWorld().getCenterPoint();
        var triangleX = Math.abs(p.getX() - center.getX());
        var triangleY = Math.abs(p.getY() - center.getY());
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    static public boolean isInWorld(Position p, GameState gameState, GameObject bot, int offset) {
        return getDistanceFromZero(p, gameState) < gameState.getWorld().getRadius() - bot.getSize() - offset;
    }

    static public boolean isInWorld(GameObject object, GameState gameState, GameObject bot, int offset) {
        return getDistanceFromZero(object, gameState) < gameState.getWorld().getRadius() - bot.getSize() - offset;
    }

    static public double vectorToDegree(WorldVector v)
    {
        var direction = Math.atan2(v.y, v.x) * 180 / Math.PI;
        return (direction + 360) % 360;
    }

    static public WorldVector degreeToVector(int heading)
    {
        return new WorldVector(Math.cos(heading), Math.sin(heading));
    }

    static public int roundToEven(double v) {

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
    static public int toDegrees(double v) {
        // radiant to degree
        return (int) (v * (180 / Math.PI));
    }
}
