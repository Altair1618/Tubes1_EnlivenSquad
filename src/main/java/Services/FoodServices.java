package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class FoodServices {
    // static public List<GameObject> getFoods(GameState gameState, GameObject bot) {
    //     return RadarService.getOtherObjects(gameState, bot, ObjectTypes.FOOD);
    // }

    // static public List<GameObject> getSuperFoods(GameState gameState, GameObject bot) {
    //     return RadarService.getOtherObjects(gameState, bot, ObjectTypes.SUPERFOOD);
    // }

    static public List<GameObject> getFoods(GameState gameState, GameObject bot) {

        int radarRadius = gameState.world.getRadius();

        return getFoods(gameState, bot, radarRadius);
    }

    static public List<GameObject> getFoods(GameState gameState, GameObject bot, int radarRadius) {
        var objectList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD && RadarService.isInWorld(item, gameState, bot, 15) && RadarService.getRealDistance(bot, item) < radarRadius)
                .sorted(Comparator
                        .comparing(item -> RadarService.getRealDistance(bot, item)))
                .collect(Collectors.toList());

        return objectList;
    }

    static public List<GameObject> getSuperFoods(GameState gameState, GameObject bot) {
        int radarRadius = 50;
        return getSuperFoods(gameState, bot, radarRadius);
        
    }

    static public List<GameObject> getSuperFoods(GameState gameState, GameObject bot, int radarRadius) {
        

        var objectList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD && RadarService.isInWorld(item, gameState, bot, 15) && RadarService.getRealDistance(bot, item) < radarRadius)
                .sorted(Comparator
                        .comparing(item -> RadarService.getRealDistance(bot, item)))
                .collect(Collectors.toList());

        return objectList;
    }

    static public List<GameObject> getNearestFoods(GameState gameState, GameObject bot)
    {
        return RadarService.getNearestOtherObjects(gameState, bot, ObjectTypes.FOOD);
    }
}
