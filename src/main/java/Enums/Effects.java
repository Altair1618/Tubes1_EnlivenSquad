package Enums;
import java.util.*;

public enum Effects {
    AFTER_BURNER(1),
    ASTEROID_FIELD(2),
    GAS_CLOUD(4),
    SUPERFOOD(8),
    SHIELD(16);

    public final Integer value;

    Effects(Integer value) {
        this.value = value;
    }

    public static List<Boolean> getEffectList(Integer value) {
        
        // mengembalikan list status efek yang ada pada player 
        List<Boolean> res = new ArrayList<Boolean>();
        Integer temp = value;
        Integer ctr = 16;

        while(ctr > 0)
        {
            if (temp >= ctr)
            {
                temp -= ctr;
                res.add(true);
            }

            else
            {
                res.add(false);
            }

            ctr /= 2;
        }

        return res;


      }
}
