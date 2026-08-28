private Text getDisplayName(
        HudManager.HudId id
) {

    return switch (id) {

        case ARMOR ->
                Text.literal("Armor");

        case FPS ->
                Text.literal("FPS");

        case PING ->
                Text.literal("Ping");

        case TPS ->
                Text.literal("TPS");

        case CPS ->
                Text.literal("CPS");

        case COMBO ->
                Text.literal("Combo");

        case TOTEM ->
                Text.literal("Totems");

        case POTION ->
                Text.literal("Potions");

        case EFFECTS ->
                Text.literal("Effects");

        case GAPPLE ->
                Text.literal("Gapples");

        case WARNING ->
                Text.literal("Armor Warning");

        case ENEMY ->
                Text.literal("Enemy HP");

        case COOLDOWN ->
                Text.literal("Attack Cooldown");

        case BLOCK_OVERLAY ->
                Text.literal("Block Overlay");

        case KEYSTROKES ->
                Text.literal("Keystrokes");

        case MEMORY ->
                Text.literal("Memory");
    };
}
