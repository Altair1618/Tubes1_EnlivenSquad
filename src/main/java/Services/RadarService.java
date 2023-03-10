package Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class RadarService {

    static public int worldRadiusOffset = 50; // batas jarak kita ke pinggir world
    static public List<GameObject> allObjects = new ArrayList<GameObject>();
    static public HashMap<ObjectTypes, List<GameObject>> objects = new HashMap<ObjectTypes, List<GameObject>>();
    static public List<GameObject> players = new ArrayList<GameObject>();
    static public List<GameObject> allFoods = new ArrayList<GameObject>();

    static public void updateAttributes(GameState gameState, GameObject bot)
    {
        allObjects.clear();
        players.clear();
        allFoods.clear();

        for (int i = 1; i <= 11; i++)
        {
            if (!objects.containsKey(ObjectTypes.valueOf(i)))
            {
                objects.put(ObjectTypes.valueOf(i), new ArrayList<GameObject>());
            }

            else
            {
                objects.get(ObjectTypes.valueOf(i)).clear();
            }
        }

        

        allObjects = gameState.getGameObjects()
            .stream()
            .sorted(Comparator
                    .comparing(item -> getRealDistance(bot, item)))
            .collect(Collectors.toList());

        for (GameObject obj : allObjects)
        {
            if (FieldService.isOutsideMap(gameState, obj, worldRadiusOffset - 2 * obj.size))
            {
                continue;
            }

            objects.get(obj.getGameObjectType()).add(obj);

            if (obj.getGameObjectType() == ObjectTypes.FOOD || obj.getGameObjectType() == ObjectTypes.SUPERFOOD)
            {
                allFoods.add(obj);
            }
            
        }

        players = gameState.getPlayerGameObjects()
        .stream()
        .filter(item -> item.getId() != bot.getId())
        .sorted(Comparator
                .comparing(item -> getRealDistance(bot, item)))
        .collect(Collectors.toList());
    }

    static public List<GameObject> getOtherObjects(ObjectTypes objectType)
    {
        // mengembalikan objek-objek lain bertipe tertentu dan diurutkan berdasarkan jarak terhadap bot

        return objects.get(objectType);
    }

    static public List<GameObject> getOtherObjects(ObjectTypes objectType, GameObject bot, int radarRadius)
    {
        // mengembalikan objek-objek lain bertipe tertentu yang berada di dalam rentang radar dan diurutkan berdasarkan jarak terhadap bot 
        
        return objects.get(objectType)
                .stream().filter(item -> RadarService.getRealDistance(bot, item) < radarRadius)
                .collect(Collectors.toList());
    }

    static public List<GameObject> getOtherObjects(Position position) {
        // mengembalikan objek-objek lain dan diurutkan berdasarkan jarak terhadap position 
        return allObjects
                .stream()
                .sorted(Comparator
                        .comparing(item -> getRealDistance(item.size, 0, getDistanceBetween(item, position))))
                .collect(Collectors.toList());
    }

    static public Double getRealDistance(int radius1, int radius2, double distance)
    {
        // can return negative distance if collapsing
        // smaller means the center is closer when collapsing
        return distance - radius1 - radius2;

    }

    static public Double getRealDistance(GameObject object1, GameObject object2)
    {
        // can return negative distance if collapsing
        // smaller means the center is closer when collapsing
        return getRealDistance(object1.size, object2.size, getDistanceBetween(object1, object2));

    }

    static public Double getDistanceBetween(GameObject object1, GameObject object2) {
        // mengembalikan jarak dua objek
        var triangleX = Math.abs(object1.getPosition().x - object2.getPosition().x);
        var triangleY = Math.abs(object1.getPosition().y - object2.getPosition().y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    static public Double getDistanceBetween(GameObject object, Position p) {
        // mengembalikan jarak dua objek
        var triangleX = Math.abs(object.getPosition().x - p.x);
        var triangleY = Math.abs(object.getPosition().y - p.y);
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    static public Double getDistanceBetween(Position p1, Position p2) {
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

    static public List<GameObject> getCollapsingPlayers(GameState gameState, Position position, Integer size)
    {
        // mengembalikan objek-objek bertipe tertentu yang sedang collapse dengan bot pada posisi dan size tertentu

        return players
                .stream()
                .filter(item -> isCollapsing(item, position, size)).collect(Collectors.toList());

    }
    static public List<GameObject> getCollapsingObjects(GameState gameState, Position position, Integer size)
    {
        // mengembalikan objek-objek bertipe tertentu yang sedang collapse dengan bot pada posisi dan size tertentu

        return getOtherObjects(position).stream().filter(item -> isCollapsing(item, position, size)).collect(Collectors.toList());

    }

    static public Double getDistanceFromZero(GameObject object, GameState gameState) {
        // Mengembalikan Jarak Objek dari Center
        Position center = gameState.getWorld().getCenterPoint();
        var triangleX = Math.abs(object.getPosition().getX() - center.getX());
        var triangleY = Math.abs(object.getPosition().getY() - center.getY());
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    static public Double getDistanceFromZero(Position p, GameState gameState) {
        // Mengembalikan Jarak Objek dari Center
        Position center = gameState.getWorld().getCenterPoint();
        var triangleX = Math.abs(p.getX() - center.getX());
        var triangleY = Math.abs(p.getY() - center.getY());
        return Math.sqrt(triangleX * triangleX + triangleY * triangleY);
    }

    static public Double vectorToDegree(WorldVector v)
    {
        var direction = Math.atan2(v.y, v.x) * 180 / Math.PI;
        return (direction + 360) % 360;
    }

    static public WorldVector degreeToVector(int heading)
    {
        Double rad = toRadians((double) heading);
        return new WorldVector(Math.cos(rad), Math.sin(rad));
    }

    static public int roundToEven(Double v) {

        // standar pembulatan engine
        // contoh : 24.5 dibulatin ke 24, 25.5 dibulatin ke 26, sedangkan yang bukan desimal 0.5 akan dibulatin seperti biasa
        long res = Math.round(v);

        Double des = res - v;

        if (des.equals(0.5) && res % 2 == 1)
        {
            res--;
        }

        return (int) res;
    }
    static public int toDegrees(Double v) {
        // radiant to degree
        return (int) (v * (180 / Math.PI));
    }

    static public Double toRadians(Double v) {
        // radiant to degree
        return (v * (Math.PI / 180));
    }

}
