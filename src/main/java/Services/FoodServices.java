package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class FoodServices {


    public GameObject getNearestFood(GameState gameState, GameObject bot) {
        var foodList = gameState.getGameObjects()
                .stream().filter(item -> item.getGameObjectType() == ObjectTypes.FOOD)
                .sorted(Comparator
                        .comparing(item -> RadarService.getDistanceBetween(bot, item)))
                .collect(Collectors.toList());

        return foodList.get(0);
    }
}
