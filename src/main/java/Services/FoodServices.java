package Services;

import Enums.*;
import Models.*;
import java.util.*; 

public class FoodServices {


    static public List<GameObject> getFoods(GameState gameState, GameObject bot) {

        // mengembalikan makanan terdekat dengan player
        
        return RadarService.getOtherObjects(gameState, bot, ObjectTypes.FOOD);
        
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
