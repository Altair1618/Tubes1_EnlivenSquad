package Enums;

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
}
