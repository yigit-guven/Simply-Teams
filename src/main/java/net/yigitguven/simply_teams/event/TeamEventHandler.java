package net.yigitguven.simply_teams.event;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.yigitguven.simply_teams.Config;
import net.yigitguven.simply_teams.command.TeamCommands;
import net.yigitguven.simply_teams.command.VanillaTeamRemover;
import net.yigitguven.simply_teams.team.Team;
import net.yigitguven.simply_teams.team.TeamManager;

public class TeamEventHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        VanillaTeamRemover.removeVanillaTeam(event.getDispatcher());
        TeamCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onTabListNameFormat(PlayerEvent.TabListNameFormat event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        MinecraftServer server = serverPlayer.server;
        Team team = TeamManager.getTeamByPlayer(server, serverPlayer.getUUID());
        if (team == null) {
            return;
        }

        MutableComponent nameComponent = Component.empty();

        if (Config.ALLOW_TEAM_PREFIX.get() && Config.TAB_LIST_SHOW_PREFIX.get() && team.getPrefix().isEnabled()) {
            nameComponent.append(team.getPrefix().toComponent()).append(" ");
        }

        nameComponent.append(Component.literal(serverPlayer.getGameProfile().getName())
                .withStyle(Style.EMPTY.withColor(team.getColor())));

        if (team.hasIcon() && Config.TAB_LIST_SHOW_ICON.get()) {
            ItemStack stack = team.getIconItem();
            String itemName = stack.getHoverName().getString();
            String format = Config.ICON_FORMAT.get();
            String badgeText = String.format(format, itemName);

            ChatFormatting iconColor = team.getColor() != ChatFormatting.WHITE ? team.getColor() : ChatFormatting.GOLD;
            MutableComponent iconComponent = Component.literal(" " + badgeText)
                    .withStyle(Style.EMPTY
                            .withColor(iconColor)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack))));

            nameComponent.append(iconComponent);
        }

        if (Config.ALLOW_TEAM_SUFFIX.get() && Config.TAB_LIST_SHOW_SUFFIX.get() && team.getSuffix().isEnabled()) {
            nameComponent.append(" ").append(team.getSuffix().toComponent());
        }

        event.setDisplayName(nameComponent);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.server;
            Team team = TeamManager.getTeamByPlayer(server, serverPlayer.getUUID());
            if (team != null) {
                team.getMember(serverPlayer.getUUID()).setName(serverPlayer.getGameProfile().getName());
                TeamManager.syncScoreboardTeam(team, server);

                if (!team.getMotd().isEmpty()) {
                    serverPlayer.sendSystemMessage(Component.literal("\n[Team MOTD] ")
                            .withStyle(ChatFormatting.GOLD)
                            .append(Component.literal(team.getMotd()).withStyle(ChatFormatting.YELLOW)));
                }
            }
            server.getCommands().sendCommands(serverPlayer);
            TeamManager.refreshTabList(server, serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (TeamManager.isTeamChatMode(player.getUUID())) {
            Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
            if (team != null) {
                event.setCanceled(true);
                TeamManager.sendTeamChat(player, event.getMessage().getString());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer victim) {
            Entity attacker = event.getSource().getEntity();
            if (attacker instanceof ServerPlayer attackerPlayer && !victim.getUUID().equals(attackerPlayer.getUUID())) {
                Team victimTeam = TeamManager.getTeamByPlayer(victim.server, victim.getUUID());
                Team attackerTeam = TeamManager.getTeamByPlayer(attackerPlayer.server, attackerPlayer.getUUID());
                if (victimTeam != null && attackerTeam != null && victimTeam.getId().equalsIgnoreCase(attackerTeam.getId())) {
                    if (!victimTeam.isFriendlyFire()) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
