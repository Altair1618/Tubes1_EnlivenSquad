package Services;

import Enums.*;
import Models.*;
import java.util.*;
import java.util.stream.*;

public class TorpedoService extends ProjectileService {

    static public int missilesSpeed = 20;  // torpedo or supernova
    static public int missilesSize = 10; // torpedo or supernova
    static public Double dangerZonePercentage = 0.8;
    static public Double dangerZoneRadiusFactor = 5.0;

    static public int torpedoSizeLimit = 50;

    static public boolean isTorpedoAvailable(GameObject bot) {
        // True if player can fire torpedo
        // default bot.size >= 50
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
        double offSet = RadarService.toDegrees(Math.asin(radius / distance)) * dangerZonePercentage;

        // angle 80% from original incoming zone
        // distance torpedo to bot = 2 * bot.size()
        if (((angleBetween(torpedoHeading, headingBetween + offSet)
                + angleBetween(torpedoHeading, headingBetween - offSet)) <= 2 * offSet) && (distance <= dangerZoneRadiusFactor * bot.getSize())) {
            return true;
        }

        return false;
    }

    static public WorldVector getInverseDirection(GameObject torpedo)
    {
        WorldVector temp = RadarService.degreeToVector(torpedo.getHeading());
        
        return temp;
    }


}
