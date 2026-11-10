package net.yigitguven.simply_teams.team;

public enum TeamPermission {
    INVITE("Invite players"),
    KICK("Kick members"),
    CHANGE_ICON("Change team icon"),
    CHANGE_COLOR("Change team color"),
    CHANGE_PREFIX("Change team prefix"),
    CHANGE_SUFFIX("Change team suffix"),
    MANAGE_RANKS("Manage ranks"),
    SET_RANK("Set member rank"),
    RENAME("Rename team"),
    CHAT("Team chat"),
    TOGGLE_FRIENDLY_FIRE("Toggle friendly fire"),
    SET_HOME("Set team home base");

    private final String description;

    TeamPermission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static TeamPermission fromString(String name) {
        for (TeamPermission perm : values()) {
            if (perm.name().equalsIgnoreCase(name)) {
                return perm;
            }
        }
        return null;
    }
}
