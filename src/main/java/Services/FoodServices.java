package Services;

import Enums.*;
import Models.*;
import java.util.*; 

public class FoodServices {


    static public List<GameObject> getFoods(GameState gameState, GameObject bot) {

        // mengembalikan makanan terdekat dengan player
        
        return RadarService.getOtherObjects(gameState, bot, ObjectTypes.FOOD);
        
    }
}
