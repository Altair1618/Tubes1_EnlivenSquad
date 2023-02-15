package Services;

import Models.*;

import java.util.*;
import java.util.stream.*;

public class PlayerService {
    static public List<GameObject> getOtherPlayerList(GameState gameState, GameObject bot) {
        // Mengembalikan List Player Lainnya

        return RadarService.players;
    }

    static public List<GameObject> getNearestPlayers(GameState gameState, GameObject bot)
    {
        List<GameObject> players = getOtherPlayerList(gameState, bot);
        List<GameObject> res = new ArrayList<GameObject>();

        if (players.isEmpty()) return res;

        res.add(players.get(0));

        int i = 1;
        double distance = RadarService.getRealDistance(bot, players.get(0));

        while (i < players.size() && RadarService.getRealDistance(bot, players.get(i)).equals(distance))
        {
            res.add(players.get(i));
        }

        return res;

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

        return RadarService.players.stream().filter(item -> !isBotBigger(bot, item) && RadarService.getRealDistance(bot, item) <= range).collect(Collectors.toList());

    }

    static public List<GameObject> getPreys(GameState gameState, GameObject bot, int offset) {
        // Mengembalikan List Player yang lebih besar dari bot

        return RadarService.players.stream().filter(item -> isBotBigger(bot, item, offset)).collect(Collectors.toList());
    }

    static public WorldVector getEscapePlayerVector(List<GameObject> others, GameObject bot) {
        // Mengembalikan Vektor arah player bila ingin kabur
        // Others diisi dengan bigger players bukan other players

        return FieldService.getHeadingEscape(bot, others);
    }

    static public WorldVector getChasePlayerVector(List<GameObject> preys, GameObject bot) {
        // Mengembalikan Vektor mengejar player lebih kecil

        return FieldService.getHeadingEscape(bot, preys);
        // WorldVector res = new WorldVector();

        // int count = 0;
    }
}
