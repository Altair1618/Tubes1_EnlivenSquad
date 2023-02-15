package Services;

import Models.*;

import java.util.*;
import java.util.stream.*;

public class PlayerService {

    static public int sizeDifferenceOffset = 10; // Minimal selisih size player yang dikejar

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

    static public List<GameObject> getPreys(GameState gameState, GameObject bot, int offset, int range) {
        // Mengembalikan List Player yang lebih besar dari bot

        return RadarService.players.stream().filter(item -> isBotBigger(bot, item, offset) && RadarService.getRealDistance(bot, item) <= range).collect(Collectors.toList());
    }

    static public WorldVector getEscapePlayerVector(List<GameObject> others, GameObject bot) {
        // Mengembalikan Vektor arah player bila ingin kabur
        // Others diisi dengan bigger players bukan other players

        return FieldService.getHeadingEscape(bot, others);
    }

    static public WorldVector getEscapePlayerVector(GameObject bot, List<GameObject> others)
    {
        // mengembalikan arah terbaik player untuk keluar dari cloud (PENDEKATAN PENJUMLAHAN VECTOR DENGAN BOBOT 1 / (jarak yang dibutuhkan untuk escape))

        WorldVector total = new WorldVector();

        if (others.size() == 0) {
            System.out.println("WARNING in PlayerService.getEscapePlayerVector method: collapsingObjects passed has zero size!");
            return total;
        }

        for (GameObject obj : others) {


            Double distance = Math.min(0, RadarService.getRealDistance(obj, bot));
            Double weight = distance;
            
            if (weight.equals(0.0)) weight = 10e-3;
            else weight = (double) RadarService.roundToEven(weight);

            if (weight > 0) weight = 1 / weight;
            else weight = Math.abs(weight);
            total.add((new WorldVector(obj.position, bot.position)).toNormalize().mult(weight));
        }

        if (total.isZero() && others.size() == 2) 
        {
            WorldVector line = new WorldVector(others.get(0).position, others.get(1).position);
            WorldVector direction = line.getAdjacent();
            
            if (direction.dot(RadarService.degreeToVector(bot.getHeading())) > 0) total = direction;
            else total = direction.mult(-1);
            
        }

        return total;
        
    }

    static public WorldVector getChasePlayerVector(List<GameObject> preys, GameObject bot) {
        // Mengembalikan Vektor mengejar player lebih kecil

        return FieldService.getHeadingEscape(bot, preys);
        // WorldVector res = new WorldVector();

        // int count = 0;
    }
}
