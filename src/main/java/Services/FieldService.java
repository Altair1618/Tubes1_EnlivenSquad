package Services;

import Enums.*;
import Models.*;
import java.util.*;

public class FieldService {

    static public List<GameObject> getCollapsingClouds(GameState gameState, GameObject bot, int radarRadius)
    {
        // mengembalikan semua cloud yang collapse dengan player
        return RadarService.getOtherObjects(gameState, bot, ObjectTypes.GASCLOUD, radarRadius);
    }

    static public List<GameObject> getCollapsingAsteroids(GameState gameState, GameObject bot, int radarRadius)
    {
        // mengembalikan semua asteroid field yang collapse dengan player

        return RadarService.getOtherObjects(gameState, bot, ObjectTypes.ASTEROIDFIELD, radarRadius);
    }


    static public List<GameObject> getWormHoles(GameState gameState, GameObject bot)
    {
        // mengembalikan semua wormhole di map
        return RadarService.getOtherObjects(gameState, bot, ObjectTypes.WORMHOLE);
    }

    static public List<GameObject> getCollapsingObjectsAfterWormHole(GameState gameState, GameObject bot, List<GameObject> otherWormHoles, int sizeOffset)
    {
        // mengembalikan semua objek yang akan collapse dengan player setelah memasuki wormhole (pair wormhole tidak diketahui sehingga perlu ditinjau semua untuk kasus terburuk)
        List<GameObject> res = new ArrayList<GameObject>();
        for(GameObject wh : otherWormHoles)
        {
            res.addAll(RadarService.getCollapsingObjects(gameState, wh.position, bot.size + sizeOffset));
        }

        return res;
    }

    static public List<Integer> getHeadingEscape(GameObject bot, List<GameObject> collapsingObject)
    {
        // mengembalikan arah terbaik player untuk keluar dari cloud (PENDEKATAN PENJUMLAHAN VECTOR DENGAN BOBOT 1 / (jarak yang dibutuhkan untuk escape))

        ArrayList<Integer> res = new ArrayList<Integer>();
        WorldVector total = new WorldVector();
        
        if (collapsingObject.size() == 0) {
            System.out.println("WARNING in FieldService.getHeadingEscape method: collapsingObjects passed has zero size!");
            return res;
        }

        for (GameObject obj : collapsingObject) {

            double weight = 0.;
            double distance = RadarService.getDistanceBetween(bot, obj);
            weight = RadarService.roundToEven(obj.size - distance + bot.size); // dipastikan weight >= 1 karena collapsing dan menggunakan perhitungan integer
            
            if (Math.abs(RadarService.roundToEven(weight)) < Double.MIN_VALUE) weight = 10e-3 * (weight < 0? -1 : 1);
            else weight = RadarService.roundToEven(weight);

            if (weight > 0) weight = 1 / weight;
            else weight = Math.abs(weight);
            total.add((new WorldVector(obj.position, bot.position)).toNormalize().mult(weight));
        }

        if (total.isZero()) 
        {
            if (collapsingObject.size() > 2) res = new ArrayList<Integer>(bot.getHeading());

            // if size == 2
            else
            {
                WorldVector line = new WorldVector(collapsingObject.get(0).position, collapsingObject.get(1).position);
                WorldVector direction = line.getAdjacent();
        
                res.add(RadarService.roundToEven(RadarService.vectorToDegree(direction)));
                res.add(RadarService.roundToEven(RadarService.vectorToDegree(direction.mult(-1))));
        
            }
        }

        else res.add(RadarService.roundToEven(RadarService.vectorToDegree(total)));

        if (res.size() == 0)
        {
            System.out.println("BUG in FieldService.getHeadingEscape method: ArrayList<Integer> res size is zero!");
        }
        return res;
        
    }

    static public Boolean isWormHoleAvailable(GameObject bot, GameObject wormHole)
    {
        // mengembalikan true jika player dapat teleport dengan worm hole tertentu
        return wormHole.size > bot.size;
    }

    static public Boolean isOutsideMap(GameState gameState, GameObject bot)
    {
        Position center = gameState.world.centerPoint;

        return (gameState.world.radius < RadarService.roundToEven(RadarService.getDistanceBetween(bot, center)) + bot.size);
    }

    static public Boolean isOutsideMap(GameState gameState, GameObject bot, int offset)
    {
        // making world radius smaller if offset
        Position center = gameState.world.centerPoint;

        return (gameState.world.radius - offset < RadarService.roundToEven(RadarService.getDistanceBetween(bot, center)) + bot.size);
    }

    static public Boolean isOutsideMap(GameState gameState, Position p, int size)
    {
        Position center = gameState.world.centerPoint;

        return (gameState.world.radius < RadarService.roundToEven(RadarService.getDistanceBetween(p, center)) + size);
    }

    static public Boolean isOutsideMap(GameState gameState, Position p, int size, int offset)
    {
        Position center = gameState.world.centerPoint;

        return (gameState.world.radius - offset < RadarService.roundToEven(RadarService.getDistanceBetween(p, center)) + size);
    }
    
    static public int getCenterDirection(GameState gameState, GameObject bot)
    {
        return RadarService.getHeadingBetween(bot, gameState.world.centerPoint);
    }

}
