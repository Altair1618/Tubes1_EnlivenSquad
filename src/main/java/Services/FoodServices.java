package Services;

import Enums.*;
import Models.*;

public class FoodServices {


    static public GameObject getNearestFood(GameState gameState, GameObject bot) {
        var foodList = RadarService.getOtherObjects(gameState, bot, ObjectTypes.FOOD);

        return foodList.get(0);
    }
}
