package Services;

import Enums.*;
import Models.*;

public class FoodServices {


    static public GameObject getNearestFood(GameState gameState, GameObject bot) {

        // mengembalikan makanan terdekat dengan player
        
        var foodList = RadarService.getOtherObjects(gameState, bot, ObjectTypes.FOOD);
        
        // kalau ada >= food dengan jarak yang sama?
        return foodList.get(0);
    }
}
