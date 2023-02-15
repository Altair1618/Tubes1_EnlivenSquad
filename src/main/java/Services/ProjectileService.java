package Services;

import Models.*;
import java.util.*;

public class ProjectileService {

    static public boolean isIncoming(GameObject bot, GameObject projectile) {
        // Mengembalikan true jika torpedo mengarah ke bot
        // perhitungan dengan konsep segitiga

        return isIncoming(bot, projectile, projectile.size);
    }

    static public boolean isIncoming(GameObject bot, GameObject projectile, int avoidableSize) {
        // Mengembalikan true jika torpedo mengarah ke bot
        // perhitungan dengan konsep segitiga

        int torpedoHeading = projectile.getHeading();
        int headingBetween = RadarService.getHeadingBetween(projectile, bot);
        double distance = RadarService.getRealDistance(projectile, bot);
        int radius = avoidableSize + projectile.getSize();

        // offset = asin(radius / jarak torpedo ke bot)
        double offSet = RadarService.toDegrees(Math.asin(radius / distance));

        if (((angleBetween(torpedoHeading, headingBetween + offSet)
                + angleBetween(torpedoHeading, headingBetween - offSet)) <= 2 * offSet)) {
            return true;
        }

        return false;
    }

    static public WorldVector nextHeadingAfterProjectile(GameObject bot, GameObject incomingProjectile) {
        // Mendapat angle heading terbaik mempertimbangkan
        // supernova yang menuju ke bot dengan menggunakan vector

        WorldVector temp = RadarService.degreeToVector(incomingProjectile.getHeading());

        // random
        WorldVector tmp = temp.getAdjacent();

        // random untuk arah tegak lurus dari projectile datangnya torpedo untuk kabur
        if (tmp.dot(RadarService.degreeToVector(bot.getHeading())) > 0) {
            temp = tmp;
        } else {
            temp = tmp.mult(-1);
        }

        return temp;
    }

    static public WorldVector nextHeadingAfterProjectiles(GameObject bot, List<GameObject> incomingProjectiles) {
        // Mendapat angle heading terbaik mempertimbangkan
        // torpedo yang menuju ke bot dengan menggunakan vector
        
        WorldVector res = new WorldVector();

        incomingProjectiles.forEach((projectile) -> {
           
            WorldVector temp = nextHeadingAfterProjectile(bot, projectile);

            Double distance = Math.min(0, RadarService.getRealDistance(projectile, bot));
            Double weight = distance;

            if (distance.equals(0.0)) weight = 0.001;
            else weight = (double) RadarService.roundToEven(weight);

            // menghitung rata-rata vektor dari semua kemungkinan arah kabur
            res.add(temp.div(distance));
        });
        
        return res.toNormalize();
    }

    static protected double angleBetween(double angle1, double angle2) {
        // Menghitung angle dari 2 sudut

        return Math.abs((angle1 - angle2 + 180 + 360) % 360 - 180);
    }
}