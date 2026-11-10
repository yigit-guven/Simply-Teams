package net.yigitguven.simply_teams.team;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.simply_teams.Config;

import java.util.*;

public class Team {
    private final String id; // Leader's player nick
    private String displayName;
    private UUID leaderUUID;
    private ChatFormatting color;
    private TeamTag prefix;
    private TeamTag suffix;
    private ItemStack iconItem;
    private boolean friendlyFire = false;
    private String motd = "";
    private TeamHome home = null;
    private final Map<UUID, TeamMember> members = new LinkedHashMap<>();
    private final Map<String, TeamRank> ranks = new LinkedHashMap<>();
    private final long createdAt;

    public Team(String id, String displayName, UUID leaderUUID, String leaderName) {
        this.id = id;
        this.displayName = displayName;
        this.leaderUUID = leaderUUID;
        this.color = ChatFormatting.WHITE;
        this.prefix = TeamTag.empty();
        this.suffix = TeamTag.empty();
        this.iconItem = ItemStack.EMPTY;
        this.friendlyFire = false;
        this.motd = "";
        this.home = null;
        this.createdAt = System.currentTimeMillis();

        // Initialize default ranks
        TeamRank leaderRank = TeamRank.createDefaultLeader();
        TeamRank officerRank = TeamRank.createDefaultOfficer();
        TeamRank memberRank = TeamRank.createDefaultMember();
        ranks.put(leaderRank.getId(), leaderRank);
        ranks.put(officerRank.getId(), officerRank);
        ranks.put(memberRank.getId(), memberRank);

        // Add leader as first member
        TeamMember leaderMember = new TeamMember(leaderUUID, leaderName, TeamRank.LEADER_ID, this.createdAt);
        members.put(leaderUUID, leaderMember);
    }

    public Team(String id, String displayName, UUID leaderUUID, long createdAt) {
        this.id = id;
        this.displayName = displayName;
        this.leaderUUID = leaderUUID;
        this.color = ChatFormatting.WHITE;
        this.prefix = TeamTag.empty();
        this.suffix = TeamTag.empty();
        this.iconItem = ItemStack.EMPTY;
        this.friendlyFire = false;
        this.motd = "";
        this.home = null;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public UUID getLeaderUUID() {
        return leaderUUID;
    }

    public void setLeaderUUID(UUID leaderUUID) {
        this.leaderUUID = leaderUUID;
        TeamMember member = members.get(leaderUUID);
        if (member != null) {
            member.setRankId(TeamRank.LEADER_ID);
        }
    }

    public ChatFormatting getColor() {
        if (!Config.ALLOW_TEAM_COLOR.get()) {
            return ChatFormatting.WHITE;
        }
        return color != null ? color : ChatFormatting.WHITE;
    }

    public void setColor(ChatFormatting color) {
        this.color = color != null ? color : ChatFormatting.WHITE;
    }

    public TeamTag getPrefix() {
        return prefix;
    }

    public void setPrefix(TeamTag prefix) {
        this.prefix = prefix != null ? prefix : TeamTag.empty();
    }

    public TeamTag getSuffix() {
        return suffix;
    }

    public void setSuffix(TeamTag suffix) {
        this.suffix = suffix != null ? suffix : TeamTag.empty();
    }

    public ItemStack getIconItem() {
        return iconItem != null ? iconItem : ItemStack.EMPTY;
    }

    public void setIconItem(ItemStack iconItem) {
        this.iconItem = iconItem != null ? iconItem.copy() : ItemStack.EMPTY;
    }

    public boolean hasIcon() {
        return iconItem != null && !iconItem.isEmpty();
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public String getMotd() {
        return motd != null ? motd : "";
    }

    public void setMotd(String motd) {
        this.motd = motd != null ? motd : "";
    }

    public TeamHome getHome() {
        return home;
    }

    public void setHome(TeamHome home) {
        this.home = home;
    }

    public boolean hasHome() {
        return home != null;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Map<UUID, TeamMember> getMembers() {
        return Collections.unmodifiableMap(members);
    }

    public TeamMember getMember(UUID uuid) {
        return members.get(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public boolean isLeader(UUID uuid) {
        return leaderUUID != null && leaderUUID.equals(uuid);
    }

    public void addMember(UUID uuid, String name, String rankId) {
        if (rankId == null || !ranks.containsKey(rankId)) {
            rankId = TeamRank.MEMBER_ID;
        }
        members.put(uuid, new TeamMember(uuid, name, rankId, System.currentTimeMillis()));
    }

    public boolean removeMember(UUID uuid) {
        if (isLeader(uuid)) {
            return false;
        }
        return members.remove(uuid) != null;
    }

    public Map<String, TeamRank> getRanks() {
        return Collections.unmodifiableMap(ranks);
    }

    public TeamRank getRank(String rankId) {
        if (rankId == null) return null;
        return ranks.get(rankId.toLowerCase());
    }

    public TeamRank getMemberRank(UUID uuid) {
        TeamMember member = members.get(uuid);
        if (member == null) return null;
        if (isLeader(uuid)) {
            TeamRank leaderRank = ranks.get(TeamRank.LEADER_ID);
            return leaderRank != null ? leaderRank : TeamRank.createDefaultLeader();
        }
        TeamRank rank = ranks.get(member.getRankId());
        return rank != null ? rank : ranks.get(TeamRank.MEMBER_ID);
    }

    public void addRank(TeamRank rank) {
        ranks.put(rank.getId(), rank);
    }

    public boolean removeRank(String rankId) {
        if (TeamRank.LEADER_ID.equalsIgnoreCase(rankId) || TeamRank.MEMBER_ID.equalsIgnoreCase(rankId)) {
            return false; // Cannot remove default leader or member ranks
        }
        TeamRank removed = ranks.remove(rankId.toLowerCase());
        if (removed != null) {
            // Demote any members with this rank to member
            for (TeamMember member : members.values()) {
                if (rankId.equalsIgnoreCase(member.getRankId())) {
                    member.setRankId(TeamRank.MEMBER_ID);
                }
            }
            return true;
        }
        return false;
    }

    public boolean hasPermission(UUID uuid, TeamPermission permission) {
        if (isLeader(uuid)) {
            return true;
        }
        TeamRank rank = getMemberRank(uuid);
        return rank != null && rank.hasPermission(permission);
    }

    public List<TeamMember> getSortedMembers() {
        List<TeamMember> list = new ArrayList<>(members.values());
        list.sort((m1, m2) -> {
            boolean l1 = isLeader(m1.getUuid());
            boolean l2 = isLeader(m2.getUuid());
            if (l1 && !l2) return -1;
            if (!l1 && l2) return 1;

            TeamRank r1 = getRank(m1.getRankId());
            TeamRank r2 = getRank(m2.getRankId());
            int p1 = r1 != null ? r1.getPriority() : 0;
            int p2 = r2 != null ? r2.getPriority() : 0;

            if (p1 != p2) {
                return Integer.compare(p2, p1); // Higher priority first
            }
            return m1.getName().compareToIgnoreCase(m2.getName());
        });
        return list;
    }

    public MutableComponent getFormattedDisplayName() {
        return Component.literal(displayName).withStyle(Style.EMPTY.withColor(getColor()).withBold(true));
    }

    public CompoundTag toNBT(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("displayName", displayName);
        tag.putUUID("leaderUUID", leaderUUID);
        tag.putString("color", color.getName());
        tag.putLong("createdAt", createdAt);
        tag.putBoolean("friendlyFire", friendlyFire);
        tag.putString("motd", motd);
        tag.put("prefix", prefix.toNBT());
        tag.put("suffix", suffix.toNBT());

        if (home != null) {
            tag.put("home", home.toNBT());
        }

        if (hasIcon()) {
            Tag itemTag = iconItem.saveOptional(registries);
            if (itemTag instanceof CompoundTag compound) {
                tag.put("iconItem", compound);
            }
        }

        ListTag memberList = new ListTag();
        for (TeamMember member : members.values()) {
            memberList.add(member.toNBT());
        }
        tag.put("members", memberList);

        ListTag rankList = new ListTag();
        for (TeamRank rank : ranks.values()) {
            rankList.add(rank.toNBT());
        }
        tag.put("ranks", rankList);

        return tag;
    }

    public static Team fromNBT(CompoundTag tag, HolderLookup.Provider registries) {
        String id = tag.getString("id");
        String displayName = tag.getString("displayName");
        UUID leaderUUID = tag.getUUID("leaderUUID");
        long createdAt = tag.getLong("createdAt");

        Team team = new Team(id, displayName, leaderUUID, createdAt);
        ChatFormatting color = ChatFormatting.getByName(tag.getString("color"));
        team.setColor(color != null ? color : ChatFormatting.WHITE);

        if (tag.contains("friendlyFire")) {
            team.setFriendlyFire(tag.getBoolean("friendlyFire"));
        }
        if (tag.contains("motd")) {
            team.setMotd(tag.getString("motd"));
        }
        if (tag.contains("home", Tag.TAG_COMPOUND)) {
            team.setHome(TeamHome.fromNBT(tag.getCompound("home")));
        }
        if (tag.contains("prefix", Tag.TAG_COMPOUND)) {
            team.setPrefix(TeamTag.fromNBT(tag.getCompound("prefix")));
        }
        if (tag.contains("suffix", Tag.TAG_COMPOUND)) {
            team.setSuffix(TeamTag.fromNBT(tag.getCompound("suffix")));
        }
        if (tag.contains("iconItem", Tag.TAG_COMPOUND)) {
            ItemStack stack = ItemStack.parseOptional(registries, tag.getCompound("iconItem"));
            team.setIconItem(stack);
        }

        if (tag.contains("ranks", Tag.TAG_LIST)) {
            ListTag rankList = tag.getList("ranks", Tag.TAG_COMPOUND);
            for (int i = 0; i < rankList.size(); i++) {
                TeamRank rank = TeamRank.fromNBT(rankList.getCompound(i));
                team.ranks.put(rank.getId(), rank);
            }
        }

        // Ensure default ranks exist if missing
        if (!team.ranks.containsKey(TeamRank.LEADER_ID)) team.ranks.put(TeamRank.LEADER_ID, TeamRank.createDefaultLeader());
        if (!team.ranks.containsKey(TeamRank.OFFICER_ID)) team.ranks.put(TeamRank.OFFICER_ID, TeamRank.createDefaultOfficer());
        if (!team.ranks.containsKey(TeamRank.MEMBER_ID)) team.ranks.put(TeamRank.MEMBER_ID, TeamRank.createDefaultMember());

        if (tag.contains("members", Tag.TAG_LIST)) {
            ListTag memberList = tag.getList("members", Tag.TAG_COMPOUND);
            for (int i = 0; i < memberList.size(); i++) {
                TeamMember member = TeamMember.fromNBT(memberList.getCompound(i));
                team.members.put(member.getUuid(), member);
            }
        }

        return team;
    }
}
