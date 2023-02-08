package Services;

import Enums.*;
import Models.*;
import java.util.*;

public class GasCloudService {

    static private RadarService radarService = new RadarService();

    public List<GameObject> collapsingClouds;

    GasCloudService()
    {
        collapsingClouds = new ArrayList<GameObject>();
    }

    public List<GameObject> getCollapsingClouds()
    {
        return collapsingClouds;
    }

    public List<GameObject> updateCollapsingClouds(GameState gameState, GameObject bot)
    {
        // update atribut collapsingClouds berisi cloud yang sedang collapse saat ini dengan bot
        collapsingClouds = radarService.getCollapsingObjects(gameState, bot, ObjectTypes.GAS_CLOUD);

        return collapsingClouds;
    }

    public boolean isCloudCollapsing(GameObject bot)
    {
        // memeriksa apakah bot sedang di dalam cloud
        return Effects.getEffectList(bot.effectsCode).get(0);
    }

    public int getHeadingEscape(GameObject bot)
    {
        // mengembalikan arah terbaik player untuk keluar dari cloud (PENDEKATAN RATA-RATA)

        long total = 0;

        for (GameObject cloud : collapsingClouds) {
            total += radarService.getHeadingBetween(cloud, bot);
        }

        return (int) (total / collapsingClouds.size());

    }
    
    

}
