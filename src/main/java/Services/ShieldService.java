package main.java.Services;

import Enums.*;
import Models.*;

import java.util.*;
import java.util.stream.*;

public class ShieldService {
    static public Boolean isShieldAvailable(GameObject bot) {
        // True if player can use shield
        // default bot.size >= 40
        return bot.shieldCount > 0 && bot.size >= 40;
    }
}
