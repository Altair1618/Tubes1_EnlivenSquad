package Enums;
import java.util.*;

public enum Effects {
    AFTERBURNER(1),
    ASTEROIDFIELD(2),
    GASCLOUD(4),
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
                res.add(0, true);
            }

            else
            {
                res.add(0, false);
            }

            ctr /= 2;
        }

        return res;


      }
}
