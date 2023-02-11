package Services;

import Enums.*;
import Models.*;
import java.util.*;

public class FieldService {

    static public List<GameObject> getCollapsingClouds(GameState gameState, GameObject bot)
    {
        // mengembalikan semua cloud yang collapse dengan player
        return RadarService.getCollapsingObjects(gameState, bot, ObjectTypes.GASCLOUD);
    }

    static public List<GameObject> getCollapsingAsteroids(GameState gameState, GameObject bot)
    {
        // mengembalikan semua asteroid field yang collapse dengan player
        return RadarService.getCollapsingObjects(gameState, bot, ObjectTypes.ASTEROIDFIELD);
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
        return Effects.getEffectList(bot.effectsCode).get(0);
    }

    static public boolean isAsteroidCollapsing(GameObject bot)
    {
        // memerika apakah bot sedang di dalam asteroid field
        return Effects.getEffectList(bot.effectsCode).get(1);
    }

    static public List<Integer> getHeadingEscape(GameObject bot, List<GameObject> collapsingObject)
    {
        // mengembalikan arah terbaik player untuk keluar dari cloud (PENDEKATAN PENJUMLAHAN VECTOR DENGAN BOBOT 1 / (jarak yang dibutuhkan untuk escape))

        WorldVector total = new WorldVector();

        for (GameObject obj : collapsingObject) {

            double weight = obj.size - RadarService.getDistanceBetween(bot, obj) + bot.size; // dipastikan weight >= 1 karena collapsing dan menggunakan perhitungan integer
            total.add((new WorldVector(obj.position, bot.position)).toNormalize().div(weight));
        }

        if (total.isZero()) 
        {
            if (collapsingObject.size() > 2) return new ArrayList<Integer>(bot.getHeading());

            // if size == 2

            WorldVector line = new WorldVector(collapsingObject.get(0).position, collapsingObject.get(1).position);
            WorldVector direction = line.getAdjacent();
            return new ArrayList<Integer>(){
                {
                    add((int) RadarService.vectorToDegree(direction));
                    add((int) RadarService.vectorToDegree(direction.mult(-1)));
            
                }
            };



        }

        return new ArrayList<Integer>((int) RadarService.vectorToDegree(total));
    }

    static public Boolean isWormHoleAvailable(GameObject bot, GameObject wormHole)
    {
        // mengembalikan true jika player dapat teleport dengan worm hole tertentu
        return wormHole.size > bot.size;
    }

    static public Boolean isOutsideMap(GameState gameState, GameObject bot)
    {
        Position center = gameState.world.centerPoint;

        return (gameState.world.radius < RadarService.getDistanceBetween(bot, center) + bot.size);
    }
    
    static public int getCenterDirection(GameState gameState, GameObject bot)
    {
        return RadarService.getHeadingBetween(bot, gameState.world.centerPoint);
    }

}
