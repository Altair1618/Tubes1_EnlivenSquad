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
        var objectList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD && RadarService.isInWorld(item, gameState, bot))
                .sorted(Comparator
                        .comparing(item -> RadarService.getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        return objectList;
    }

    static public List<GameObject> getSuperFoods(GameState gameState, GameObject bot) {
        var objectList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.SUPERFOOD && RadarService.isInWorld(item, gameState, bot))
                .sorted(Comparator
                        .comparing(item -> RadarService.getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        return objectList;
    }

    static public List<GameObject> getNearestFoods(GameState gameState, GameObject bot)
    {
        List<GameObject> foods = getFoods(gameState, bot);
        List<GameObject> res = new ArrayList<GameObject>();

        if (foods.isEmpty()) return res;

        res.add(foods.get(0));

        int i = 1;
        double distance = RadarService.getRealDistance(bot, foods.get(0));

        while (i < foods.size() && Math.abs(RadarService.getRealDistance(bot, foods.get(i)) - distance) < Double.MIN_VALUE)
        {
            res.add(foods.get(i));
        }

        return res;

    }
}
