package Services;

import Enums.*;
import Models.*;

import java.util.*;

public class TorpedoService {

    static public boolean isTorpedoAvailable(GameObject bot) {
        // True if player can fire torpedo
        // default bot.size >= 30
        return bot.torpedoSalvoCount > 0 && bot.size >= 30;
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

    static public double angleBetween(double angle1, double angle2) {
        // Menghitung angle dari 2 sudut

        return Math.abs((angle1 - angle2 + 180 + 360) % 360 - 180);
    }

    static public boolean isIncoming(GameObject bot, GameObject torpedo) {
        // Mengembalikan true jika torpedo mengarah ke bot
        // perhitungan dengan konsep segitiga

        int torpedoHeading = torpedo.getHeading();
        int headingBetween = RadarService.getHeadingBetween(torpedo, bot);
        double distance = RadarService.getDistanceBetween(torpedo, bot);
        int radius = bot.getSize() + torpedo.getSize();

        // offset = asin(radius / jarak torpedo ke bot)
        double offSet = Math.asin(radius / distance);

        if ((angleBetween(torpedoHeading, headingBetween + offSet)
                + angleBetween(torpedoHeading, headingBetween - offSet)) <= 2 * offSet) {
            return true;
        }

        return false;
    }

    static public List<GameObject> getIncomingTorpedo(GameState gameState, GameObject bot) {
        // Mendapat list torpedo yang incoming to bot
        
        List<GameObject> incomingTorpedo = new ArrayList<GameObject>();
        List<GameObject> torpedoesList = RadarService.getOtherObjects(gameState, bot, ObjectTypes.TORPEDOSALVO);

        torpedoesList.forEach((torpedo) -> {
            if (isIncoming(bot, torpedo)) {
                incomingTorpedo.add(torpedo);
            }
        });

        return incomingTorpedo;
    }

    static private int roundToEven(double v) {

        // standar pembulatan engine
        // contoh : 24.5 dibulatin ke 24, 25.5 dibulatin ke 26, sedangkan yang bukan
        // desimal 0.5 akan dibulatin seperti biasa
        long res = Math.round(v);

        double des = res - v;

        if (Math.abs(des - 0.5) < Math.ulp(1.0) && res % 2 == 1) {
            res--;
        }

        return (int) res;
    }

    static public int nextHeadingAfterTorpedo(GameObject bot, List<GameObject> incomingTorpedo) {
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
            double distance = RadarService.getDistanceBetween(torpedo, bot);

            // menghitung rata-rata vektor dari semua kemungkinan arah kabur
            res.add(temp.div(distance));
        });

        return roundToEven(RadarService.vectorToDegree(res));
    }
}
