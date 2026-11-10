package net.yigitguven.simply_teams.team;

import java.util.UUID;

public class TeamInvite {
    private final String teamId;
    private final String teamDisplayName;
    private final UUID inviterUUID;
    private final String inviterName;
    private final UUID targetUUID;
    private final long createdAt;
    private final long expiresAt;

    public TeamInvite(String teamId, String teamDisplayName, UUID inviterUUID, String inviterName, UUID targetUUID, long timeoutMillis) {
        this.teamId = teamId;
        this.teamDisplayName = teamDisplayName;
        this.inviterUUID = inviterUUID;
        this.inviterName = inviterName;
        this.targetUUID = targetUUID;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = this.createdAt + timeoutMillis;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getTeamDisplayName() {
        return teamDisplayName;
    }

    public UUID getInviterUUID() {
        return inviterUUID;
    }

    public String getInviterName() {
        return inviterName;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public long getRemainingSeconds() {
        long remaining = (expiresAt - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
}

