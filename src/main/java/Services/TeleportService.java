package Services;

import Models.*;
import java.util.*;

public class TeleportService {
    
    static public Boolean isTeleportAvailable(GameObject bot)
    {
        // mengembalikan true jika player dapat menggunakan teleport
        return bot.teleporterCount > 0 && bot.size >= 25;
    }

    static public Boolean isTeleportAvailable(GameObject bot, int sizeLimit)
    {
        // mengembalikan true jika player dapat menggunakan teleport dengan tambahan batas bawah size player yang diperbolehkan
        return bot.teleporterCount > 0 && bot.size >= sizeLimit;
    }

    static public List<GameObject> getCollapsingObjectsAfterTeleport(GameState gameState, GameObject bot, GameObject teleporter)
    {
        // mengembalikan semua objek yang akan collapse dengan player setelah memasuki wormhole (pair wormhole tidak diketahui sehingga perlu ditinjau semua untuk kasus terburuk)
   
        return RadarService.getCollapsingObjects(gameState, teleporter.position, bot.size);
    }
}
