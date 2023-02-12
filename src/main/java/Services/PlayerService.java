package Services;

import Models.*;

import java.util.*;
import java.util.stream.*;

public class PlayerService {
    static public List<GameObject> getOtherPlayerList(GameState gameState, GameObject bot) {
        // Mengembalikan List Player Lainnya
        return gameState.getPlayerGameObjects()
                .stream().filter(item -> item.getId() != bot.getId())
                .sorted(Comparator
                        .comparing(item -> RadarService.getRealDistance(bot, item)))
                .collect(Collectors.toList());
    }

    static public int getOtherPlayerHeading(GameObject other) {
        // Mengembalikan Heading Player Lain
        return other.getHeading();
    }

    static public boolean isBotBigger(GameObject bot, GameObject other) {
        // Mengembalikan Apakah Bot Lebih Besar Daripada Player other
        return bot.getSize() > other.getSize();
    }

    static public boolean isBotBigger(GameObject bot, GameObject other, int offset) {
        // Mengembalikan Apakah Bot Lebih Besar Daripada Player other dengan offset
        return bot.getSize() > other.getSize() + offset;
    }

    static public List<GameObject> getBiggerPlayerInRange(GameState gameState, GameObject bot, int range) {
        // Mengembalikan List Player yang lebih besar dari bot
        return gameState.getPlayerGameObjects()
                .stream().filter(item -> item.getId() != bot.getId() && !isBotBigger(bot, item) && RadarService.getRealDistance(bot, item) <= range)
                .sorted(Comparator
                        .comparing(item -> RadarService.getRealDistance(bot, item)))
                .collect(Collectors.toList());
    }

    static public List<GameObject> getPreys(GameState gameState, GameObject bot, int offset) {
        // Mengembalikan List Player yang lebih besar dari bot
        return gameState.getPlayerGameObjects()
                .stream().filter(item -> item.getId() != bot.getId() && isBotBigger(bot, item, offset))
                .sorted(Comparator
                        .comparing(item -> RadarService.getRealDistance(bot, item)))
                .collect(Collectors.toList());
    }

    static public WorldVector getEscapePlayerVector(List<GameObject> others, GameObject bot) {
        // Mengembalikan Vektor arah player bila ingin kabur
        // Others diisi dengan bigger players bukan other players
        WorldVector res = new WorldVector();

        others.forEach((player) -> {
            WorldVector temp = RadarService.degreeToVector(RadarService.getHeadingBetween(player, bot));
            double distance = RadarService.getRealDistance(player, bot);

            // Menghitung Rata-Rata Vektor dari Semua Arah Kemungkinan Kabur
            res.add(temp.div(distance));
        });

        return res.toNormalize();
    }

    static public WorldVector getChasePlayerVector(List<GameObject> preys, GameObject bot) {
        // Mengembalikan Vektor mengejar player lebih kecil
        WorldVector res = new WorldVector();

        preys.forEach((prey) -> {
            WorldVector temp = RadarService.degreeToVector(RadarService.getHeadingBetween(bot, prey));
            double distance = RadarService.getRealDistance(prey, bot);

            // Menghitung Rata-Rata Vektor dari Semua Arah Kemungkinan Kabur
            res.add(temp.div(distance));
        });

        return res.toNormalize();
    }
}
