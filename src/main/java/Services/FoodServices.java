package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class FoodServices {


    static public List<GameObject> getFoods(GameState gameState, GameObject bot) {

        return getFoods(bot, gameState.world.getRadius());
    }

    static public List<GameObject> getFoods(GameObject bot, int radarRadius) {

        return RadarService.getOtherObjects(ObjectTypes.FOOD, bot, radarRadius);
    }

    static public List<GameObject> getSuperFoods(GameState gameState, GameObject bot) {
   
        return getSuperFoods(bot, gameState.world.getRadius());
        
    }

    static public List<GameObject> getSuperFoods(GameObject bot, int radarRadius) {
        
        
        return RadarService.getOtherObjects(ObjectTypes.SUPERFOOD, bot, radarRadius);
    }
    
    static public List<GameObject> getAllFoods(GameState gameState, GameObject bot)
    {
        return getAllFoods(bot, gameState.world.radius);
    }

    static public List<GameObject> getAllFoods(GameObject bot, int radarRadius)
    {
        return RadarService.allFoods.stream().filter(item -> RadarService.getRealDistance(bot, item) < radarRadius).collect(Collectors.toList());
    }

    static public List<GameObject> getNearestFoods(GameObject bot)
    {
        return RadarService.getNearestOtherObjects(bot, ObjectTypes.FOOD);
    }
}
