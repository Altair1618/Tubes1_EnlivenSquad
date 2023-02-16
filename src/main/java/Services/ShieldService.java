package Services;

import java.util.List;

import Enums.ObjectTypes;
import Models.*;

public class ShieldService {

    static public int shieldSizeLimit = 40;
    static public Boolean isShieldAvailable(GameObject bot) {
        // True if player can use shield
        // default bot.size >= 40
        return bot.shieldCount > 0 && bot.size >= shieldSizeLimit;
    }

    static public Boolean isShieldAvailable(GameObject bot, int sizeLimit) {
        // True if player can use shield
        // with bot.size >= sizeLimit
        return bot.shieldCount > 0 && bot.size >= sizeLimit;
    }

    static public Boolean isPlayerShielded(GameObject enemy) {
        // True if player have activated shield

        List<GameObject> shieldList = RadarService.getOtherObjects(ObjectTypes.SHIELD);

        System.out.println(shieldList.size());

        for (int i = 0; i < shieldList.size(); i ++) {
            System.out.println(shieldList.get(i).getPosition().getX() + " " + shieldList.get(i).getPosition().getY());
            System.out.println(enemy.getPosition().getX() + " " + enemy.getPosition().getY());

            if (shieldList.get(i).getPosition().getX() == enemy.getPosition().getX()
            && shieldList.get(i).getPosition().getY() == enemy.getPosition().getY()) {
                return true;
            }
        }

        return false;
    }
}
