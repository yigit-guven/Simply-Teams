package net.yigitguven.simply_teams.team;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.EnumSet;
import java.util.Set;

public class TeamRank {
    public static final String LEADER_ID = "leader";
    public static final String OFFICER_ID = "officer";
    public static final String MEMBER_ID = "member";

    private final String id;
    private String displayName;
    private int priority;
    private ChatFormatting color;
    private boolean bold;
    private final Set<TeamPermission> permissions = EnumSet.noneOf(TeamPermission.class);

    public TeamRank(String id, String displayName, int priority, ChatFormatting color, boolean bold) {
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.priority = priority;
        this.color = color != null ? color : ChatFormatting.GRAY;
        this.bold = bold;
    }

    public static TeamRank createDefaultLeader() {
        TeamRank rank = new TeamRank(LEADER_ID, "Leader", 100, ChatFormatting.GOLD, true);
        rank.permissions.addAll(EnumSet.allOf(TeamPermission.class));
        return rank;
    }

    public static TeamRank createDefaultOfficer() {
        TeamRank rank = new TeamRank(OFFICER_ID, "Officer", 50, ChatFormatting.AQUA, false);
        rank.permissions.add(TeamPermission.INVITE);
        rank.permissions.add(TeamPermission.KICK);
        rank.permissions.add(TeamPermission.CHANGE_ICON);
        rank.permissions.add(TeamPermission.CHAT);
        return rank;
    }

    public static TeamRank createDefaultMember() {
        TeamRank rank = new TeamRank(MEMBER_ID, "Member", 10, ChatFormatting.GRAY, false);
        rank.permissions.add(TeamPermission.CHAT);
        return rank;
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public void setColor(ChatFormatting color) {
        this.color = color;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public Set<TeamPermission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(TeamPermission permission) {
        if (LEADER_ID.equals(id)) {
            return true;
        }
        return permissions.contains(permission);
    }

    public void addPermission(TeamPermission permission) {
        permissions.add(permission);
    }

    public void removePermission(TeamPermission permission) {
        permissions.remove(permission);
    }

    public MutableComponent getFormattedName() {
        return Component.literal(displayName).withStyle(Style.EMPTY.withColor(color).withBold(bold));
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("displayName", displayName);
        tag.putInt("priority", priority);
        tag.putString("color", color.getName());
        tag.putBoolean("bold", bold);

        ListTag permList = new ListTag();
        for (TeamPermission perm : permissions) {
            permList.add(StringTag.valueOf(perm.name()));
        }
        tag.put("permissions", permList);
        return tag;
    }

    public static TeamRank fromNBT(CompoundTag tag) {
        String id = tag.getString("id");
        String displayName = tag.getString("displayName");
        int priority = tag.getInt("priority");
        ChatFormatting color = ChatFormatting.getByName(tag.getString("color"));
        if (color == null) color = ChatFormatting.GRAY;
        boolean bold = tag.getBoolean("bold");

        TeamRank rank = new TeamRank(id, displayName, priority, color, bold);
        if (tag.contains("permissions", Tag.TAG_LIST)) {
            ListTag permList = tag.getList("permissions", Tag.TAG_STRING);
            for (int i = 0; i < permList.size(); i++) {
                TeamPermission perm = TeamPermission.fromString(permList.getString(i));
                if (perm != null) {
                    rank.permissions.add(perm);
                }
            }
        }
        return rank;
    }
}

