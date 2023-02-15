package Services;

import Enums.*;
import Models.*;
import java.util.*;

public class FoodServices {


    static public List<GameObject> getFoods(GameState gameState, GameObject bot) {

        return getFoods(gameState, bot, gameState.world.getRadius());
    }

    static public List<GameObject> getFoods(GameState gameState, GameObject bot, int radarRadius) {

        return RadarService.getOtherObjects(ObjectTypes.FOOD, bot, radarRadius);
    }

    static public List<GameObject> getSuperFoods(GameState gameState, GameObject bot) {
   
        return getSuperFoods(gameState, bot, gameState.world.getRadius());
        
    }

    static public List<GameObject> getSuperFoods(GameState gameState, GameObject bot, int radarRadius) {
        
        
        return RadarService.getOtherObjects(ObjectTypes.SUPERFOOD, bot, radarRadius);
    }

    static public List<GameObject> getNearestFoods(GameState gameState, GameObject bot)
    {
        return RadarService.getNearestOtherObjects(bot, ObjectTypes.FOOD);
    }
}
