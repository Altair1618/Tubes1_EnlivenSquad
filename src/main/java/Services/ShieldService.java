package Services;

import java.util.List;

import Enums.ObjectTypes;
import Models.*;

public class ShieldService {

    static public int shieldSizeLimit = 40;

    static public Boolean isShieldAvailable(GameObject bot, int sizeLimit) {
        // True if player can use shield
        // with bot.size >= sizeLimit
        return bot.shieldCount > 0 && bot.size >= sizeLimit;
    }
}
