package net.yigitguven.simply_teams.team;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class TeamMember {
    private final UUID uuid;
    private String name;
    private String rankId;
    private final long joinedAt;

    public TeamMember(UUID uuid, String name, String rankId, long joinedAt) {
        this.uuid = uuid;
        this.name = name;
        this.rankId = rankId;
        this.joinedAt = joinedAt;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRankId() {
        return rankId;
    }

    public void setRankId(String rankId) {
        this.rankId = rankId;
    }

    public long getJoinedAt() {
        return joinedAt;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", uuid);
        tag.putString("name", name != null ? name : "");
        tag.putString("rankId", rankId != null ? rankId : TeamRank.MEMBER_ID);
        tag.putLong("joinedAt", joinedAt);
        return tag;
    }

    public static TeamMember fromNBT(CompoundTag tag) {
        UUID uuid = tag.getUUID("uuid");
        String name = tag.getString("name");
        String rankId = tag.getString("rankId");
        long joinedAt = tag.getLong("joinedAt");
        return new TeamMember(uuid, name, rankId, joinedAt);
    }
}

