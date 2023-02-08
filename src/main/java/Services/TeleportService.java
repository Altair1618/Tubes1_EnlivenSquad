package Services;

import Models.GameObject;

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
}
