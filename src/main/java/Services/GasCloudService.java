package Services;

import Enums.*;
import Models.*;
import java.util.*;

public class GasCloudService {

    static public List<GameObject> collapsingClouds;

    GasCloudService()
    {
        collapsingClouds = new ArrayList<GameObject>();
    }

    static public List<GameObject> getCollapsingClouds()
    {
        return collapsingClouds;
    }

    static public List<GameObject> updateCollapsingClouds(GameState gameState, GameObject bot)
    {
        // update atribut collapsingClouds berisi cloud yang sedang collapse saat ini dengan bot
        collapsingClouds = RadarService.getCollapsingObjects(gameState, bot, ObjectTypes.GASCLOUD);

        return collapsingClouds;
    }

    static public boolean isCloudCollapsing(GameObject bot)
    {
        // memeriksa apakah bot sedang di dalam cloud
        return Effects.getEffectList(bot.effectsCode).get(0);
    }

    static public int getHeadingEscape(GameObject bot)
    {
        // mengembalikan arah terbaik player untuk keluar dari cloud (PENDEKATAN RATA-RATA)

        long total = 0;

        for (GameObject cloud : collapsingClouds) {
            total += RadarService.getHeadingBetween(cloud, bot);
        }

        return (int) (total / collapsingClouds.size());

    }
    
    

}
