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

    static public int getCloudHeadingEscape(GameObject bot, List<GameObject> collapsingClouds)
    {
        // mengembalikan arah terbaik player untuk keluar dari cloud (PENDEKATAN RATA-RATA)

        long total = 0;

        for (GameObject cloud : collapsingClouds) {
            total += RadarService.getHeadingBetween(cloud, bot);
        }

        return (int) (total / collapsingClouds.size());

    }

    static public int getAsteroidHeadingEscape(GameObject bot, List<GameObject> collapsingAsteroids)
    {
        // mengembalikan arah terbaik player untuk keluar dari asteroid field (PENDEKATAN RATA-RATA)

        long total = 0;

        for (GameObject ast : collapsingAsteroids) {
            total += RadarService.getHeadingBetween(ast, bot);
        }

        return (int) (total / collapsingAsteroids.size());

    }

    static public Boolean isWormHoleAvailable(GameObject bot, GameObject wormHole)
    {
        // mengembalikan true jika player dapat teleport dengan worm hole tertentu
        return wormHole.size > bot.size;
    }
    
    

}
