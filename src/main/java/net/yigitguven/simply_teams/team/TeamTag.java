package net.yigitguven.simply_teams.team;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class TeamTag {
    private String text;
    private ChatFormatting color;
    private boolean bold;
    private boolean enabled;

    public TeamTag(String text, ChatFormatting color, boolean bold, boolean enabled) {
        this.text = text != null ? text : "";
        this.color = color != null ? color : ChatFormatting.WHITE;
        this.bold = bold;
        this.enabled = enabled;
    }

    public static TeamTag empty() {
        return new TeamTag("", ChatFormatting.WHITE, false, false);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public boolean isEnabled() {
        return enabled && !text.isEmpty();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public MutableComponent toComponent() {
        if (!isEnabled()) {
            return Component.empty();
        }
        MutableComponent comp = Component.literal(text);
        Style style = Style.EMPTY.withColor(color).withBold(bold);
        return comp.withStyle(style);
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("text", text);
        tag.putString("color", color.getName());
        tag.putBoolean("bold", bold);
        tag.putBoolean("enabled", enabled);
        return tag;
    }

    public static TeamTag fromNBT(CompoundTag tag) {
        if (tag == null) return empty();
        String text = tag.getString("text");
        ChatFormatting color = ChatFormatting.getByName(tag.getString("color"));
        if (color == null) color = ChatFormatting.WHITE;
        boolean bold = tag.getBoolean("bold");
        boolean enabled = tag.getBoolean("enabled");
        return new TeamTag(text, color, bold, enabled);
    }
}

