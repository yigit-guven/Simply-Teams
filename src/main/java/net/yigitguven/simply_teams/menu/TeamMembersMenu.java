package net.yigitguven.simply_teams.menu;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.yigitguven.simply_teams.team.Team;
import net.yigitguven.simply_teams.team.TeamMember;
import net.yigitguven.simply_teams.team.TeamRank;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TeamMembersMenu extends ChestMenu {
    public static final int CHEST_SLOTS = 54;
    public static final int MEMBERS_PER_PAGE = 45;

    private final Container chestContainer;
    private final Team team;
    private final ServerPlayer serverPlayer;
    private int page = 0;
    private List<TeamMember> sortedMembers = new ArrayList<>();

    public TeamMembersMenu(int containerId, Inventory playerInventory, Team team, ServerPlayer player) {
        this(containerId, playerInventory, new SimpleContainer(CHEST_SLOTS), team, player);
    }

    private TeamMembersMenu(int containerId, Inventory playerInventory, Container container, Team team, ServerPlayer player) {
        super(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6);
        this.chestContainer = container;
        this.team = team;
        this.serverPlayer = player;

        if (team != null) {
            this.sortedMembers = team.getSortedMembers();
            populatePage();
        }
    }

    public void populatePage() {
        chestContainer.clearContent();

        int startIndex = page * MEMBERS_PER_PAGE;
        int endIndex = Math.min(startIndex + MEMBERS_PER_PAGE, sortedMembers.size());

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for (int i = startIndex; i < endIndex; i++) {
            int slot = i - startIndex;
            TeamMember member = sortedMembers.get(i);
            boolean isLeader = team.isLeader(member.getUuid());
            TeamRank rank = team.getMemberRank(member.getUuid());

            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponents.PROFILE, new ResolvableProfile(new GameProfile(member.getUuid(), member.getName())));

            ChatFormatting rankColor = rank != null ? rank.getColor() : ChatFormatting.WHITE;
            head.set(DataComponents.CUSTOM_NAME, Component.literal(member.getName())
                    .withStyle(Style.EMPTY.withColor(rankColor).withBold(isLeader)));

            boolean isOnline = serverPlayer.server.getPlayerList().getPlayer(member.getUuid()) != null;

            List<Component> lore = new ArrayList<>();
            lore.add(Component.literal("Rank: ").withStyle(ChatFormatting.GRAY)
                    .append(rank != null ? rank.getFormattedName() : Component.literal("Member").withStyle(ChatFormatting.WHITE)));
            lore.add(Component.literal("Status: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(isOnline ? "Online" : "Offline").withStyle(isOnline ? ChatFormatting.GREEN : ChatFormatting.RED)));
            lore.add(Component.literal("Joined: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(dateFormat.format(new Date(member.getJoinedAt()))).withStyle(ChatFormatting.YELLOW)));
            if (isLeader) {
                lore.add(Component.literal("Team Founder & Leader").withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.ITALIC));
            }

            head.set(DataComponents.LORE, new ItemLore(lore));
            chestContainer.setItem(slot, head);
        }

        // Control bar (Row 5: slots 45 to 53)
        ItemStack border = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        border.set(DataComponents.CUSTOM_NAME, Component.empty());
        for (int s = 45; s < 54; s++) {
            chestContainer.setItem(s, border.copy());
        }

        // Slot 45: Previous page
        if (page > 0) {
            ItemStack prev = new ItemStack(Items.ARROW);
            prev.set(DataComponents.CUSTOM_NAME, Component.literal("« Previous Page (" + page + ")").withStyle(ChatFormatting.YELLOW));
            chestContainer.setItem(45, prev);
        }

        // Slot 49: Team Info & Stats
        ItemStack infoItem;
        if (team.hasIcon()) {
            infoItem = team.getIconItem().copy();
        } else {
            infoItem = new ItemStack(Items.NETHER_STAR);
        }
        infoItem.set(DataComponents.CUSTOM_NAME, team.getFormattedDisplayName());

        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.literal("Team ID: ").withStyle(ChatFormatting.GRAY).append(Component.literal(team.getId()).withStyle(ChatFormatting.AQUA)));
        infoLore.add(Component.literal("Leader: ").withStyle(ChatFormatting.GRAY).append(Component.literal(team.getId()).withStyle(ChatFormatting.GOLD)));
        infoLore.add(Component.literal("Total Members: ").withStyle(ChatFormatting.GRAY).append(Component.literal(String.valueOf(sortedMembers.size())).withStyle(ChatFormatting.WHITE)));
        infoLore.add(Component.literal("Color: ").withStyle(ChatFormatting.GRAY).append(Component.literal(team.getColor().getName()).withStyle(team.getColor())));
        if (team.getPrefix().isEnabled()) {
            infoLore.add(Component.literal("Prefix: ").withStyle(ChatFormatting.GRAY).append(team.getPrefix().toComponent()));
        }
        if (team.getSuffix().isEnabled()) {
            infoLore.add(Component.literal("Suffix: ").withStyle(ChatFormatting.GRAY).append(team.getSuffix().toComponent()));
        }
        int maxPage = getMaxPage();
        infoLore.add(Component.literal("Page: ").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal((page + 1) + " / " + (maxPage + 1)).withStyle(ChatFormatting.GRAY)));
        infoItem.set(DataComponents.LORE, new ItemLore(infoLore));
        chestContainer.setItem(49, infoItem);

        // Slot 53: Next page
        if (page < getMaxPage()) {
            ItemStack next = new ItemStack(Items.ARROW);
            next.set(DataComponents.CUSTOM_NAME, Component.literal("Next Page (" + (page + 2) + ") »").withStyle(ChatFormatting.YELLOW));
            chestContainer.setItem(53, next);
        }

        broadcastChanges();
    }

    public int getMaxPage() {
        if (sortedMembers.isEmpty()) return 0;
        return (sortedMembers.size() - 1) / MEMBERS_PER_PAGE;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == 45 && page > 0) {
            page--;
            populatePage();
            if (player instanceof ServerPlayer sp) {
                sp.containerMenu.sendAllDataToRemote();
            }
            return;
        }
        if (slotId == 53 && page < getMaxPage()) {
            page++;
            populatePage();
            if (player instanceof ServerPlayer sp) {
                sp.containerMenu.sendAllDataToRemote();
            }
            return;
        }

        // View-only menu: cancel any item movement / pickup / shift-click
        if (player instanceof ServerPlayer sp) {
            sp.containerMenu.sendAllDataToRemote();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
