package net.yigitguven.simply_teams;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_DISPLAY_NAME_LENGTH;
    public static final ModConfigSpec.BooleanValue ALLOW_TEAM_COLOR;
    public static final ModConfigSpec.BooleanValue ALLOW_TEAM_PREFIX;
    public static final ModConfigSpec.BooleanValue ALLOW_TEAM_SUFFIX;
    public static final ModConfigSpec.IntValue MAX_PREFIX_LENGTH;
    public static final ModConfigSpec.IntValue MAX_SUFFIX_LENGTH;
    public static final ModConfigSpec.IntValue INVITE_TIMEOUT_SECONDS;
    public static final ModConfigSpec.BooleanValue ALLOW_TEAM_HOME;
    public static final ModConfigSpec.BooleanValue OPS_ARE_ADMINS;

    public static final ModConfigSpec.BooleanValue TAB_LIST_SHOW_ICON;
    public static final ModConfigSpec.BooleanValue TAB_LIST_SHOW_PREFIX;
    public static final ModConfigSpec.BooleanValue TAB_LIST_SHOW_SUFFIX;
    public static final ModConfigSpec.ConfigValue<String> ICON_FORMAT;

    static {
        BUILDER.push("teams");

        MAX_DISPLAY_NAME_LENGTH = BUILDER
                .comment("Maximum character length for team display names. Use -1 for unlimited length.")
                .defineInRange("maxDisplayNameLength", -1, -1, 256);

        ALLOW_TEAM_COLOR = BUILDER
                .comment("Whether teams are allowed to have custom colors. If false, default/white is used.")
                .define("allowTeamColor", true);

        ALLOW_TEAM_PREFIX = BUILDER
                .comment("Whether teams are allowed to have custom prefixes.")
                .define("allowTeamPrefix", true);

        ALLOW_TEAM_SUFFIX = BUILDER
                .comment("Whether teams are allowed to have custom suffixes.")
                .define("allowTeamSuffix", true);

        MAX_PREFIX_LENGTH = BUILDER
                .comment("Maximum character length for team prefixes. Use -1 for unlimited.")
                .defineInRange("maxPrefixLength", 16, -1, 64);

        MAX_SUFFIX_LENGTH = BUILDER
                .comment("Maximum character length for team suffixes. Use -1 for unlimited.")
                .defineInRange("maxSuffixLength", 16, -1, 64);

        INVITE_TIMEOUT_SECONDS = BUILDER
                .comment("How many seconds a team invite remains valid before expiring.")
                .defineInRange("inviteTimeoutSeconds", 60, 5, 600);

        ALLOW_TEAM_HOME = BUILDER
                .comment("Whether teams can set and teleport to a shared team home base (/team sethome, /team home).")
                .define("allowTeamHome", true);

        BUILDER.pop();

        BUILDER.push("admin");

        OPS_ARE_ADMINS = BUILDER
                .comment(
                        "Whether server operators (OPs) have administrative privileges for Simply Teams.",
                        "When true (default), operators can manage or delete other teams (/team admin) and bypass team permissions.",
                        "When false, operators are treated strictly as regular players, allowing OPs to play normally without admin team powers."
                )
                .define("opsAreAdmins", true);

        BUILDER.pop();

        BUILDER.push("tab_list");

        TAB_LIST_SHOW_ICON = BUILDER
                .comment("Whether to show the team icon in the server player list (TAB).")
                .define("showIcon", true);

        TAB_LIST_SHOW_PREFIX = BUILDER
                .comment("Whether to show the team prefix in the server player list (TAB).")
                .define("showPrefix", true);

        TAB_LIST_SHOW_SUFFIX = BUILDER
                .comment("Whether to show the team suffix in the server player list (TAB).")
                .define("showSuffix", true);

        ICON_FORMAT = BUILDER
                .comment("Format for the team icon badge in the player list. %s will be replaced by the item display name.")
                .define("iconFormat", "[%s]");

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
