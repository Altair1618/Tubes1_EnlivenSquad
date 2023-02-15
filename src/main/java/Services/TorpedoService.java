package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class TorpedoService {

    static public int torpedoSizeLimit = 30;
    static public boolean isTorpedoAvailable(GameObject bot) {
        // True if player can fire torpedo
        // default bot.size >= 30
        return bot.torpedoSalvoCount > 0 && bot.size >= torpedoSizeLimit;
    }

    static public boolean isTorpedoAvailable(GameObject bot, int sizeLimit) {
        // True if player can fire torpedo
        // with bot.size >= sizeLimit
        return bot.torpedoSalvoCount > 0 && bot.size >= sizeLimit;
    }

    static public boolean isTorpedoAvailable(GameObject bot, int sizeLimit, int salvoCountLimit) {
        // True if player can fire torpedo
        // with bot.size >= sizeLimit
        // and bot.torpedoSalvoCount >= salvoCountLimit
        return bot.torpedoSalvoCount >= salvoCountLimit && bot.size >= sizeLimit;
    }

    static public boolean isIncoming(GameObject bot, GameObject torpedo) {
        // Mengembalikan true jika torpedo mengarah ke bot
        // perhitungan dengan konsep segitiga

        int torpedoHeading = torpedo.getHeading();
        int headingBetween = RadarService.getHeadingBetween(torpedo, bot);
        double distance = RadarService.getRealDistance(torpedo, bot);
        int radius = bot.getSize() + torpedo.getSize();

        // offset = asin(radius / jarak torpedo ke bot)
        double offSet = RadarService.toDegrees(Math.asin(radius / distance));

        if (((angleBetween(torpedoHeading, headingBetween + offSet)
                + angleBetween(torpedoHeading, headingBetween - offSet)) <= 2 * offSet)) {
            return true;
        }

        return false;
    }

    static public List<GameObject> getIncomingTorpedo(GameState gameState, GameObject bot) {
        // Mendapat list torpedo yang incoming to bot
        
        return RadarService.getOtherObjects(ObjectTypes.TORPEDOSALVO).stream().filter(torpedo -> isIncoming(bot, torpedo)).collect(Collectors.toList());

    }

    static public boolean fireTorpedoWhenDanger(GameObject bot, GameObject nearestTorpedo) {
        // Mendapat torpedo terdekat dan mengecek apakah berada pada danger zone

        int torpedoHeading = nearestTorpedo.getHeading();
        int headingBetween = RadarService.getHeadingBetween(nearestTorpedo, bot);
        double distance = RadarService.getRealDistance(nearestTorpedo, bot);
        int radius = bot.getSize() + nearestTorpedo.getSize();

        // offset = asin(radius / jarak torpedo ke bot)
        double offSet = RadarService.toDegrees(Math.asin(radius / distance));

        // angle 80% from original incoming zone
        // distance torpedo to bot = 2 * bot.size()
        if (((angleBetween(torpedoHeading, headingBetween + offSet)
                + angleBetween(torpedoHeading, headingBetween - offSet)) * 0.75 <= 2 * offSet * 0.8) && (distance <= 10 * bot.getSize())) {
            return true;
        }

        return false;
    }

    static public WorldVector nextHeadingAfterTorpedo(GameObject bot, List<GameObject> incomingTorpedo) {
        // Mendapat angle heading terbaik mempertimbangkan
        // torpedo yang menuju ke bot dengan menggunakan vector
        
        WorldVector res = new WorldVector();

        incomingTorpedo.forEach((torpedo) -> {
            WorldVector temp = RadarService.degreeToVector(torpedo.getHeading());

            // random
            int tmp = (int) (Math.random() * 1) + 1;

            // random untuk arah tegak lurus dari projectile datangnya torpedo untuk kabur
            if (tmp == 1) {
                temp.getRotatedBy(90);
            } else {
                temp.getRotatedBy(-90);
            }
            double distance = RadarService.getRealDistance(torpedo, bot);

            // menghitung rata-rata vektor dari semua kemungkinan arah kabur
            res.add(temp.div(distance));
        });

        return res;
    }

    static private double angleBetween(double angle1, double angle2) {
        // Menghitung angle dari 2 sudut

        return Math.abs((angle1 - angle2 + 180 + 360) % 360 - 180);
    }
}
