package Services;

import Enums.*;
import Models.*;
import java.util.*;

public class FieldService {

    static public List<GameObject> getCollapsingClouds(GameState gameState, GameObject bot)
    {
        // mengembalikan semua cloud yang collapse dengan player
        List<GameObject> res = RadarService.getCollapsingObjects(gameState, bot, ObjectTypes.GASCLOUD);

        if (res.size() == 0 && isCloudCollapsing(bot)) System.out.println("BUG in FieldService.getCollapsingClouds method: GASCLOUD effect is detected but the cloud object is not!");


        return res;
    }

    static public List<GameObject> getCollapsingAsteroids(GameState gameState, GameObject bot)
    {
        // mengembalikan semua asteroid field yang collapse dengan player
        List<GameObject> res = RadarService.getCollapsingObjects(gameState, bot, ObjectTypes.ASTEROIDFIELD);

        if (res.size() == 0 && isCloudCollapsing(bot)) System.out.println("BUG in FieldService.getCollapsingAsteroids method: ASTEROIDFIELD effect is detected but the field object is not!");
        return res;
    }

    static public List<GameObject> getWormHoles(GameState gameState, GameObject bot)
    {
        // mengembalikan semua wormhole di map
        return RadarService.getOtherObjects(gameState, bot, ObjectTypes.WORMHOLE);
    }

    static public List<GameObject> getCollapsingObjectsAfterWormHole(GameState gameState, GameObject bot, List<GameObject> otherWormHoles)
    {
        // mengembalikan semua objek yang akan collapse dengan player setelah memasuki wormhole (pair wormhole tidak diketahui sehingga perlu ditinjau semua untuk kasus terburuk)
        List<GameObject> res = new ArrayList<GameObject>();
        for(GameObject wh : otherWormHoles)
        {
            res.addAll(RadarService.getCollapsingObjects(gameState, wh.position, bot.size));
        }

        return res;
    }

    static public boolean isCloudCollapsing(GameObject bot)
    {
        // memeriksa apakah bot sedang di dalam cloud
        return Effects.getEffectList(bot.effectsCode).get(2);
    }

    static public boolean isAsteroidCollapsing(GameObject bot)
    {
        // memerika apakah bot sedang di dalam asteroid field
        return Effects.getEffectList(bot.effectsCode).get(1);
    }

    static public List<Integer> getHeadingEscape(GameObject bot, List<GameObject> collapsingObject)
    {
        // mengembalikan arah terbaik player untuk keluar dari cloud (PENDEKATAN PENJUMLAHAN VECTOR DENGAN BOBOT 1 / (jarak yang dibutuhkan untuk escape))

        ArrayList<Integer> res;
        WorldVector total = new WorldVector();
        
        if (collapsingObject.size() == 0) {
            System.out.println("WARNING in FieldService.getHeadingEscape method: collapsingObjects passed has zero size!");
            return new ArrayList<Integer>();
        }

        for (GameObject obj : collapsingObject) {

            double weight = obj.size - RadarService.roundToEven(RadarService.getDistanceBetween(bot, obj)) + bot.size; // dipastikan weight >= 1 karena collapsing dan menggunakan perhitungan integer

            if (Math.abs(weight) < Double.MIN_VALUE) weight = 10e-3;
            total.add((new WorldVector(obj.position, bot.position)).toNormalize().div(weight));
        }

        if (total.isZero()) 
        {
            if (collapsingObject.size() > 2) res = new ArrayList<Integer>(bot.getHeading());

            // if size == 2
            else
            {
                WorldVector line = new WorldVector(collapsingObject.get(0).position, collapsingObject.get(1).position);
                WorldVector direction = line.getAdjacent();
                res = new ArrayList<Integer>(){
                    {
                        add((int) RadarService.vectorToDegree(direction));
                        add((int) RadarService.vectorToDegree(direction.mult(-1)));
                
                    }
                };
            }
        }

        else res = new ArrayList<Integer>((int) RadarService.vectorToDegree(total));

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
