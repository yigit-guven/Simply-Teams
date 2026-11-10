package net.yigitguven.simply_teams.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.yigitguven.simply_teams.Config;
import net.yigitguven.simply_teams.menu.TeamMembersMenu;
import net.yigitguven.simply_teams.team.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TeamCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> teamRoot = Commands.literal("team");

        teamRoot.then(Commands.literal("create")
                .requires(TeamCommands::isPlayerWithoutTeam)
                .then(Commands.argument("displayName", StringArgumentType.greedyString())
                        .executes(context -> executeCreate(context, StringArgumentType.getString(context, "displayName"))))
                .executes(context -> {
                    context.getSource().sendFailure(Component.literal("Usage: /team create <display name>"));
                    return 0;
                }));

        teamRoot.then(Commands.literal("accept")
                .requires(TeamCommands::isPlayerWithoutTeam)
                .then(Commands.argument("target", StringArgumentType.string())
                        .executes(context -> executeAccept(context, StringArgumentType.getString(context, "target"))))
                .executes(context -> executeAccept(context, "")));

        teamRoot.then(Commands.literal("members")
                .requires(TeamCommands::isPlayerWithTeam)
                .executes(TeamCommands::executeMembers));

        teamRoot.then(Commands.literal("leave")
                .requires(TeamCommands::isPlayerWithTeam)
                .executes(TeamCommands::executeLeave));

        teamRoot.then(Commands.literal("chat")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(context -> executeChat(context, StringArgumentType.getString(context, "message"))))
                .executes(TeamCommands::executeToggleChatMode));

        teamRoot.then(Commands.literal("c")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(context -> executeChat(context, StringArgumentType.getString(context, "message"))))
                .executes(TeamCommands::executeToggleChatMode));

        teamRoot.then(Commands.literal("invite")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> executeInvite(context, EntityArgument.getPlayer(context, "player")))));

        teamRoot.then(Commands.literal("kick")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.argument("member", StringArgumentType.word())
                        .suggests(TeamCommands::suggestTeamMembers)
                        .executes(context -> executeKick(context, StringArgumentType.getString(context, "member")))));

        teamRoot.then(Commands.literal("rename")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.argument("newName", StringArgumentType.greedyString())
                        .executes(context -> executeRename(context, StringArgumentType.getString(context, "newName")))));

        teamRoot.then(Commands.literal("icon")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.literal("clear")
                        .executes(TeamCommands::executeClearIcon))
                .executes(TeamCommands::executeSetIcon));

        teamRoot.then(Commands.literal("color")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.argument("color", StringArgumentType.word())
                        .suggests(ColorArgumentHelper.SUGGESTIONS)
                        .executes(context -> executeColor(context, StringArgumentType.getString(context, "color")))));

        teamRoot.then(Commands.literal("prefix")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.literal("clear")
                        .executes(TeamCommands::executeClearPrefix))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .then(Commands.argument("color", StringArgumentType.word())
                                .suggests(ColorArgumentHelper.SUGGESTIONS)
                                .then(Commands.argument("bold", BoolArgumentType.bool())
                                        .executes(context -> executePrefix(context,
                                                StringArgumentType.getString(context, "tag"),
                                                StringArgumentType.getString(context, "color"),
                                                BoolArgumentType.getBool(context, "bold")))))));

        teamRoot.then(Commands.literal("suffix")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.literal("clear")
                        .executes(TeamCommands::executeClearSuffix))
                .then(Commands.argument("tag", StringArgumentType.word())
                        .then(Commands.argument("color", StringArgumentType.word())
                                .suggests(ColorArgumentHelper.SUGGESTIONS)
                                .then(Commands.argument("bold", BoolArgumentType.bool())
                                        .executes(context -> executeSuffix(context,
                                                StringArgumentType.getString(context, "tag"),
                                                StringArgumentType.getString(context, "color"),
                                                BoolArgumentType.getBool(context, "bold")))))));

        teamRoot.then(Commands.literal("friendlyfire")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> executeFriendlyFire(context, BoolArgumentType.getBool(context, "enabled"))))
                .executes(context -> executeFriendlyFireToggle(context)));

        teamRoot.then(Commands.literal("ff")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> executeFriendlyFire(context, BoolArgumentType.getBool(context, "enabled"))))
                .executes(context -> executeFriendlyFireToggle(context)));

        teamRoot.then(Commands.literal("motd")
                .requires(TeamCommands::isPlayerWithTeam)
                .then(Commands.literal("clear")
                        .executes(TeamCommands::executeClearMotd))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(context -> executeSetMotd(context, StringArgumentType.getString(context, "message"))))
                .executes(TeamCommands::executeShowMotd));

        teamRoot.then(Commands.literal("sethome")
                .requires(TeamCommands::isPlayerWithTeam)
                .executes(TeamCommands::executeSetHome));

        teamRoot.then(Commands.literal("home")
                .requires(TeamCommands::isPlayerWithTeam)
                .executes(TeamCommands::executeHome));

        teamRoot.then(Commands.literal("delhome")
                .requires(TeamCommands::isPlayerWithTeam)
                .executes(TeamCommands::executeDelHome));

        teamRoot.then(Commands.literal("disband")
                .requires(TeamCommands::isPlayerTeamLeader)
                .then(Commands.literal("confirm")
                        .executes(TeamCommands::executeDisbandConfirm))
                .executes(TeamCommands::executeDisbandPrompt));

        teamRoot.then(Commands.literal("transfer")
                .requires(TeamCommands::isPlayerTeamLeader)
                .then(Commands.argument("member", EntityArgument.player())
                        .executes(context -> executeTransfer(context, EntityArgument.getPlayer(context, "member")))));

        teamRoot.then(buildRankCommands());

        teamRoot.then(Commands.literal("info")
                .then(Commands.argument("teamId", StringArgumentType.word())
                        .suggests(TeamCommands::suggestAllTeamIds)
                        .executes(context -> executeInfo(context, StringArgumentType.getString(context, "teamId"))))
                .executes(context -> executeInfo(context, null)));

        teamRoot.then(Commands.literal("list")
                .then(Commands.argument("page", IntegerArgumentType.integer(1))
                        .executes(context -> executeList(context, IntegerArgumentType.getInteger(context, "page"))))
                .executes(context -> executeList(context, 1)));

        dispatcher.register(teamRoot);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildRankCommands() {
        LiteralArgumentBuilder<CommandSourceStack> rank = Commands.literal("rank")
                .requires(TeamCommands::isPlayerWithTeam);

        rank.then(Commands.literal("list")
                .executes(TeamCommands::executeRankList));

        rank.then(Commands.literal("create")
                .then(Commands.argument("id", StringArgumentType.word())
                        .then(Commands.argument("displayName", StringArgumentType.string())
                                .then(Commands.argument("priority", IntegerArgumentType.integer(1, 99))
                                        .executes(context -> executeRankCreate(context,
                                                StringArgumentType.getString(context, "id"),
                                                StringArgumentType.getString(context, "displayName"),
                                                IntegerArgumentType.getInteger(context, "priority"))))
                                .executes(context -> executeRankCreate(context,
                                        StringArgumentType.getString(context, "id"),
                                        StringArgumentType.getString(context, "displayName"),
                                        25)))));

        rank.then(Commands.literal("delete")
                .then(Commands.argument("id", StringArgumentType.word())
                        .suggests(TeamCommands::suggestCustomRanks)
                        .executes(context -> executeRankDelete(context, StringArgumentType.getString(context, "id")))));

        rank.then(Commands.literal("set")
                .then(Commands.argument("member", StringArgumentType.word())
                        .suggests(TeamCommands::suggestTeamMembers)
                        .then(Commands.argument("rankId", StringArgumentType.word())
                                .suggests(TeamCommands::suggestAllRanks)
                                .executes(context -> executeRankSet(context,
                                        StringArgumentType.getString(context, "member"),
                                        StringArgumentType.getString(context, "rankId"))))));

        rank.then(Commands.literal("color")
                .then(Commands.argument("rankId", StringArgumentType.word())
                        .suggests(TeamCommands::suggestAllRanks)
                        .then(Commands.argument("color", StringArgumentType.word())
                                .suggests(ColorArgumentHelper.SUGGESTIONS)
                                .executes(context -> executeRankColor(context,
                                        StringArgumentType.getString(context, "rankId"),
                                        StringArgumentType.getString(context, "color"))))));

        rank.then(Commands.literal("bold")
                .then(Commands.argument("rankId", StringArgumentType.word())
                        .suggests(TeamCommands::suggestAllRanks)
                        .then(Commands.argument("bold", BoolArgumentType.bool())
                                .executes(context -> executeRankBold(context,
                                        StringArgumentType.getString(context, "rankId"),
                                        BoolArgumentType.getBool(context, "bold"))))));

        rank.then(Commands.literal("permission")
                .then(Commands.argument("rankId", StringArgumentType.word())
                        .suggests(TeamCommands::suggestAllRanks)
                        .then(Commands.literal("add")
                                .then(Commands.argument("permission", StringArgumentType.word())
                                        .suggests(TeamCommands::suggestPermissions)
                                        .executes(context -> executeRankPermission(context,
                                                StringArgumentType.getString(context, "rankId"),
                                                StringArgumentType.getString(context, "permission"),
                                                true))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("permission", StringArgumentType.word())
                                        .suggests(TeamCommands::suggestPermissions)
                                        .executes(context -> executeRankPermission(context,
                                                StringArgumentType.getString(context, "rankId"),
                                                StringArgumentType.getString(context, "permission"),
                                                false))))));

        return rank;
    }

    private static boolean isPlayerWithoutTeam(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return !TeamManager.hasTeam(player.server, player.getUUID());
        }
        return false;
    }

    private static boolean isPlayerWithTeam(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return TeamManager.hasTeam(player.server, player.getUUID());
        }
        return false;
    }

    private static boolean isPlayerTeamLeader(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return TeamManager.isLeader(player.server, player.getUUID());
        }
        return false;
    }

    private static int executeCreate(CommandContext<CommandSourceStack> context, String displayName) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.createTeam(player, displayName);
        return team != null ? 1 : 0;
    }

    private static int executeAccept(CommandContext<CommandSourceStack> context, String query) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        return TeamManager.acceptInvite(player, query) ? 1 : 0;
    }

    private static int executeMembers(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) {
            context.getSource().sendFailure(Component.literal("You are not in a team!"));
            return 0;
        }

        player.openMenu(new SimpleMenuProvider(
                (containerId, inv, p) -> new TeamMembersMenu(containerId, inv, team, player),
                Component.literal("Team Members: " + team.getDisplayName())
        ));
        return 1;
    }

    private static int executeLeave(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        return TeamManager.leaveTeam(player) ? 1 : 0;
    }

    private static int executeToggleChatMode(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        boolean nowTeamChat = TeamManager.toggleTeamChatMode(player.getUUID());
        if (nowTeamChat) {
            player.sendSystemMessage(Component.literal("Team Chat Mode enabled: messages will be sent to team.")
                    .withStyle(ChatFormatting.AQUA));
        } else {
            player.sendSystemMessage(Component.literal("Team Chat Mode disabled: messages will be sent to public chat.")
                    .withStyle(ChatFormatting.YELLOW));
        }
        return 1;
    }

    private static int executeChat(CommandContext<CommandSourceStack> context, String message) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        TeamManager.sendTeamChat(player, message);
        return 1;
    }

    private static int executeInvite(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.INVITE)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to invite players!"));
            return 0;
        }

        return TeamManager.sendInvite(team, player, target) ? 1 : 0;
    }

    private static int executeKick(CommandContext<CommandSourceStack> context, String memberName) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        return TeamManager.kickMember(player, memberName) ? 1 : 0;
    }

    private static int executeDisbandPrompt(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Are you sure you want to disband your team? This cannot be undone!\nType ")
                .withStyle(ChatFormatting.RED)
                .append(Component.literal("/team disband confirm").withStyle(ChatFormatting.YELLOW)), false);
        return 1;
    }

    private static int executeDisbandConfirm(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        return TeamManager.disbandTeam(player) ? 1 : 0;
    }

    private static int executeTransfer(CommandContext<CommandSourceStack> context, ServerPlayer newLeader) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null || !team.isLeader(player.getUUID())) {
            context.getSource().sendFailure(Component.literal("Only the team leader can transfer leadership!"));
            return 0;
        }

        if (!team.isMember(newLeader.getUUID())) {
            context.getSource().sendFailure(Component.literal(newLeader.getGameProfile().getName() + " is not a member of your team!"));
            return 0;
        }

        team.setLeaderUUID(newLeader.getUUID());
        team.getMember(player.getUUID()).setRankId(TeamRank.OFFICER_ID);

        player.server.getCommands().sendCommands(player);
        player.server.getCommands().sendCommands(newLeader);
        TeamManager.refreshTabListAll(player.server);

        TeamManager.broadcastToTeam(team, player.server, Component.literal("Team leadership transferred to " + newLeader.getGameProfile().getName() + ".")
                .withStyle(ChatFormatting.GOLD));
        return 1;
    }

    private static int executeRename(CommandContext<CommandSourceStack> context, String newName) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.RENAME)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to rename the team!"));
            return 0;
        }

        int max = Config.MAX_DISPLAY_NAME_LENGTH.get();
        if (max > 0 && newName.length() > max) {
            context.getSource().sendFailure(Component.literal("Team display name cannot exceed " + max + " characters!"));
            return 0;
        }

        team.setDisplayName(newName);
        TeamSavedData.get(player.server).setDirty();
        TeamManager.syncScoreboardTeam(team, player.server);
        TeamManager.refreshTabListAll(player.server);

        TeamManager.broadcastToTeam(team, player.server, Component.literal("Team display name changed to: ")
                .withStyle(ChatFormatting.GREEN)
                .append(team.getFormattedDisplayName()));
        return 1;
    }

    private static int executeSetIcon(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.CHANGE_ICON)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to change the team icon!"));
            return 0;
        }

        net.minecraft.world.item.ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            context.getSource().sendFailure(Component.literal("Hold the item you want to set as team icon in your main hand."));
            return 0;
        }

        net.minecraft.world.item.ItemStack icon = held.copyWithCount(1);
        team.setIconItem(icon);
        TeamSavedData.get(player.server).setDirty();
        TeamManager.refreshTabListAll(player.server);

        context.getSource().sendSuccess(() -> Component.literal("Team icon set to ")
                .withStyle(ChatFormatting.GREEN)
                .append(icon.getHoverName()), false);
        return 1;
    }

    private static int executeClearIcon(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.CHANGE_ICON)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to change the team icon!"));
            return 0;
        }

        team.setIconItem(net.minecraft.world.item.ItemStack.EMPTY);
        TeamSavedData.get(player.server).setDirty();
        TeamManager.refreshTabListAll(player.server);

        context.getSource().sendSuccess(() -> Component.literal("Team icon cleared.").withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    private static int executeColor(CommandContext<CommandSourceStack> context, String colorName) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!Config.ALLOW_TEAM_COLOR.get()) {
            context.getSource().sendFailure(Component.literal("Custom team colors are disabled."));
            return 0;
        }

        if (!team.hasPermission(player.getUUID(), TeamPermission.CHANGE_COLOR)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to change the team color!"));
            return 0;
        }

        ChatFormatting color = ColorArgumentHelper.parseColor(colorName);
        if (color == null) {
            context.getSource().sendFailure(Component.literal("Unknown color: " + colorName));
            return 0;
        }

        team.setColor(color);
        TeamSavedData.get(player.server).setDirty();
        TeamManager.syncScoreboardTeam(team, player.server);
        TeamManager.refreshTabListAll(player.server);

        context.getSource().sendSuccess(() -> Component.literal("Team color updated to " + color.getName())
                .withStyle(color), false);
        return 1;
    }

    private static int executePrefix(CommandContext<CommandSourceStack> context, String tag, String colorName, boolean bold) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!Config.ALLOW_TEAM_PREFIX.get()) {
            context.getSource().sendFailure(Component.literal("Team prefixes are disabled."));
            return 0;
        }

        if (!team.hasPermission(player.getUUID(), TeamPermission.CHANGE_PREFIX)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to change the team prefix!"));
            return 0;
        }

        int max = Config.MAX_PREFIX_LENGTH.get();
        if (max > 0 && tag.length() > max) {
            context.getSource().sendFailure(Component.literal("Team prefix cannot exceed " + max + " characters."));
            return 0;
        }

        ChatFormatting color = ColorArgumentHelper.parseColor(colorName);
        if (color == null) {
            context.getSource().sendFailure(Component.literal("Unknown color: " + colorName));
            return 0;
        }

        team.setPrefix(new TeamTag(tag, color, bold, true));
        TeamSavedData.get(player.server).setDirty();
        TeamManager.syncScoreboardTeam(team, player.server);
        TeamManager.refreshTabListAll(player.server);

        context.getSource().sendSuccess(() -> Component.literal("Team prefix set to ")
                .withStyle(ChatFormatting.GREEN)
                .append(team.getPrefix().toComponent()), false);
        return 1;
    }

    private static int executeClearPrefix(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.CHANGE_PREFIX)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to change the team prefix!"));
            return 0;
        }

        team.setPrefix(TeamTag.empty());
        TeamSavedData.get(player.server).setDirty();
        TeamManager.syncScoreboardTeam(team, player.server);
        TeamManager.refreshTabListAll(player.server);

        context.getSource().sendSuccess(() -> Component.literal("Team prefix cleared.").withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    private static int executeSuffix(CommandContext<CommandSourceStack> context, String tag, String colorName, boolean bold) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!Config.ALLOW_TEAM_SUFFIX.get()) {
            context.getSource().sendFailure(Component.literal("Team suffixes are disabled."));
            return 0;
        }

        if (!team.hasPermission(player.getUUID(), TeamPermission.CHANGE_SUFFIX)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to change the team suffix!"));
            return 0;
        }

        int max = Config.MAX_SUFFIX_LENGTH.get();
        if (max > 0 && tag.length() > max) {
            context.getSource().sendFailure(Component.literal("Team suffix cannot exceed " + max + " characters."));
            return 0;
        }

        ChatFormatting color = ColorArgumentHelper.parseColor(colorName);
        if (color == null) {
            context.getSource().sendFailure(Component.literal("Unknown color: " + colorName));
            return 0;
        }

        team.setSuffix(new TeamTag(tag, color, bold, true));
        TeamSavedData.get(player.server).setDirty();
        TeamManager.syncScoreboardTeam(team, player.server);
        TeamManager.refreshTabListAll(player.server);

        context.getSource().sendSuccess(() -> Component.literal("Team suffix set to ")
                .withStyle(ChatFormatting.GREEN)
                .append(team.getSuffix().toComponent()), false);
        return 1;
    }

    private static int executeClearSuffix(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.CHANGE_SUFFIX)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to change the team suffix!"));
            return 0;
        }

        team.setSuffix(TeamTag.empty());
        TeamSavedData.get(player.server).setDirty();
        TeamManager.syncScoreboardTeam(team, player.server);
        TeamManager.refreshTabListAll(player.server);

        context.getSource().sendSuccess(() -> Component.literal("Team suffix cleared.").withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    private static int executeFriendlyFire(CommandContext<CommandSourceStack> context, boolean enabled) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.TOGGLE_FRIENDLY_FIRE)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to toggle friendly fire!"));
            return 0;
        }

        team.setFriendlyFire(enabled);
        TeamSavedData.get(player.server).setDirty();
        TeamManager.syncScoreboardTeam(team, player.server);

        TeamManager.broadcastToTeam(team, player.server, Component.literal("Friendly fire is now " + (enabled ? "enabled" : "disabled") + ".")
                .withStyle(enabled ? ChatFormatting.RED : ChatFormatting.GREEN));
        return 1;
    }

    private static int executeFriendlyFireToggle(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;
        return executeFriendlyFire(context, !team.isFriendlyFire());
    }

    private static int executeSetMotd(CommandContext<CommandSourceStack> context, String message) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.RENAME)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to change the team MOTD!"));
            return 0;
        }

        team.setMotd(message);
        TeamSavedData.get(player.server).setDirty();

        TeamManager.broadcastToTeam(team, player.server, Component.literal("Team MOTD set: " + message).withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    private static int executeClearMotd(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.RENAME)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to change the team MOTD!"));
            return 0;
        }

        team.setMotd("");
        TeamSavedData.get(player.server).setDirty();

        context.getSource().sendSuccess(() -> Component.literal("Team MOTD cleared.").withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    private static int executeShowMotd(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        String motd = team.getMotd();
        if (motd.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("No team MOTD set.").withStyle(ChatFormatting.GRAY), false);
        } else {
            context.getSource().sendSuccess(() -> Component.literal("[Team MOTD] " + motd).withStyle(ChatFormatting.YELLOW), false);
        }
        return 1;
    }

    private static int executeSetHome(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        if (!Config.ALLOW_TEAM_HOME.get()) {
            context.getSource().sendFailure(Component.literal("Team home bases are disabled."));
            return 0;
        }

        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.SET_HOME)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to set the team home!"));
            return 0;
        }

        team.setHome(TeamHome.fromPlayer(player));
        TeamSavedData.get(player.server).setDirty();

        TeamManager.broadcastToTeam(team, player.server, Component.literal("Team home base set by " + player.getGameProfile().getName() + ".")
                .withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int executeHome(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        if (!Config.ALLOW_TEAM_HOME.get()) {
            context.getSource().sendFailure(Component.literal("Team home bases are disabled."));
            return 0;
        }

        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasHome()) {
            context.getSource().sendFailure(Component.literal("Your team has not set a home base."));
            return 0;
        }

        team.getHome().teleport(player);
        player.sendSystemMessage(Component.literal("Teleported to team home base.").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private static int executeDelHome(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.SET_HOME)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to delete the team home!"));
            return 0;
        }

        team.setHome(null);
        TeamSavedData.get(player.server).setDirty();

        context.getSource().sendSuccess(() -> Component.literal("Team home base deleted.").withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    private static int executeRankList(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        MutableComponent list = Component.literal("\n=== Team Ranks (" + team.getDisplayName() + ") ===\n").withStyle(ChatFormatting.GOLD);
        for (TeamRank rank : team.getRanks().values()) {
            list.append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                    .append(rank.getFormattedName())
                    .append(Component.literal(" [ID: " + rank.getId() + ", Priority: " + rank.getPriority() + "]\n").withStyle(ChatFormatting.DARK_GRAY));
            if (!rank.getPermissions().isEmpty()) {
                String perms = rank.getPermissions().stream().map(Enum::name).collect(Collectors.joining(", "));
                list.append(Component.literal("   Permissions: " + perms + "\n").withStyle(ChatFormatting.GRAY));
            }
        }
        context.getSource().sendSuccess(() -> list, false);
        return 1;
    }

    private static int executeRankCreate(CommandContext<CommandSourceStack> context, String id, String displayName, int priority) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.MANAGE_RANKS)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to manage ranks!"));
            return 0;
        }

        if (team.getRank(id) != null) {
            context.getSource().sendFailure(Component.literal("A rank with ID '" + id + "' already exists!"));
            return 0;
        }

        TeamRank rank = new TeamRank(id, displayName, priority, ChatFormatting.GRAY, false);
        team.addRank(rank);
        TeamSavedData.get(player.server).setDirty();

        context.getSource().sendSuccess(() -> Component.literal("Created rank ")
                .withStyle(ChatFormatting.GREEN)
                .append(rank.getFormattedName()), false);
        return 1;
    }

    private static int executeRankDelete(CommandContext<CommandSourceStack> context, String id) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.MANAGE_RANKS)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to manage ranks!"));
            return 0;
        }

        if (team.removeRank(id)) {
            TeamSavedData.get(player.server).setDirty();
            context.getSource().sendSuccess(() -> Component.literal("Rank '" + id + "' deleted.").withStyle(ChatFormatting.YELLOW), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("Cannot delete default rank or rank not found!"));
            return 0;
        }
    }

    private static int executeRankSet(CommandContext<CommandSourceStack> context, String memberName, String rankId) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.SET_RANK)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to set member ranks!"));
            return 0;
        }

        TeamMember target = null;
        for (TeamMember m : team.getMembers().values()) {
            if (m.getName().equalsIgnoreCase(memberName)) {
                target = m;
                break;
            }
        }

        if (target == null) {
            context.getSource().sendFailure(Component.literal("Member '" + memberName + "' not found!"));
            return 0;
        }

        if (team.isLeader(target.getUuid())) {
            context.getSource().sendFailure(Component.literal("Cannot change the rank of the team leader!"));
            return 0;
        }

        TeamRank newRank = team.getRank(rankId);
        if (newRank == null) {
            context.getSource().sendFailure(Component.literal("Rank '" + rankId + "' does not exist!"));
            return 0;
        }

        if (TeamRank.LEADER_ID.equalsIgnoreCase(rankId)) {
            context.getSource().sendFailure(Component.literal("Use /team transfer to change the team leader!"));
            return 0;
        }

        target.setRankId(newRank.getId());
        TeamSavedData.get(player.server).setDirty();
        TeamManager.refreshTabListAll(player.server);

        TeamManager.broadcastToTeam(team, player.server, Component.literal(target.getName() + " assigned to rank ")
                .withStyle(ChatFormatting.GREEN)
                .append(newRank.getFormattedName()));
        return 1;
    }

    private static int executeRankColor(CommandContext<CommandSourceStack> context, String rankId, String colorName) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.MANAGE_RANKS)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to manage ranks!"));
            return 0;
        }

        TeamRank rank = team.getRank(rankId);
        if (rank == null) {
            context.getSource().sendFailure(Component.literal("Rank '" + rankId + "' does not exist!"));
            return 0;
        }

        ChatFormatting color = ColorArgumentHelper.parseColor(colorName);
        if (color == null) {
            context.getSource().sendFailure(Component.literal("Unknown color: " + colorName));
            return 0;
        }

        rank.setColor(color);
        TeamSavedData.get(player.server).setDirty();
        TeamManager.refreshTabListAll(player.server);

        context.getSource().sendSuccess(() -> Component.literal("Updated color for rank ")
                .withStyle(ChatFormatting.GREEN)
                .append(rank.getFormattedName()), false);
        return 1;
    }

    private static int executeRankBold(CommandContext<CommandSourceStack> context, String rankId, boolean bold) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.MANAGE_RANKS)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to manage ranks!"));
            return 0;
        }

        TeamRank rank = team.getRank(rankId);
        if (rank == null) {
            context.getSource().sendFailure(Component.literal("Rank '" + rankId + "' does not exist!"));
            return 0;
        }

        rank.setBold(bold);
        TeamSavedData.get(player.server).setDirty();
        TeamManager.refreshTabListAll(player.server);

        context.getSource().sendSuccess(() -> Component.literal("Updated bold style for rank ")
                .withStyle(ChatFormatting.GREEN)
                .append(rank.getFormattedName()), false);
        return 1;
    }

    private static int executeRankPermission(CommandContext<CommandSourceStack> context, String rankId, String permissionName, boolean add) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
        if (team == null) return 0;

        if (!team.hasPermission(player.getUUID(), TeamPermission.MANAGE_RANKS)) {
            context.getSource().sendFailure(Component.literal("You do not have permission to manage ranks!"));
            return 0;
        }

        TeamRank rank = team.getRank(rankId);
        if (rank == null) {
            context.getSource().sendFailure(Component.literal("Rank '" + rankId + "' does not exist!"));
            return 0;
        }

        TeamPermission permission = TeamPermission.fromString(permissionName);
        if (permission == null) {
            context.getSource().sendFailure(Component.literal("Unknown permission: " + permissionName));
            return 0;
        }

        if (add) {
            rank.addPermission(permission);
        } else {
            rank.removePermission(permission);
        }
        TeamSavedData.get(player.server).setDirty();

        context.getSource().sendSuccess(() -> Component.literal((add ? "Added " : "Removed ") + permission.name() + " for rank ")
                .withStyle(ChatFormatting.GREEN)
                .append(rank.getFormattedName()), false);
        return 1;
    }

    private static int executeInfo(CommandContext<CommandSourceStack> context, String teamId) {
        CommandSourceStack source = context.getSource();
        Team team;
        if (teamId != null) {
            team = TeamManager.getTeamById(source.getServer(), teamId);
            if (team == null) {
                source.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
                return 0;
            }
        } else {
            ServerPlayer player = source.getPlayer();
            if (player == null) {
                source.sendFailure(Component.literal("Specify a team ID: /team info <teamId>"));
                return 0;
            }
            team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
            if (team == null) {
                source.sendFailure(Component.literal("You are not in a team. Specify a team ID: /team info <teamId>"));
                return 0;
            }
        }

        MutableComponent info = Component.literal("\n=============================\n")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("Team: ").withStyle(ChatFormatting.YELLOW))
                .append(team.getFormattedDisplayName())
                .append(Component.literal("\nID: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(team.getId()).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nLeader: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(team.getId()).withStyle(ChatFormatting.GOLD))
                .append(Component.literal("\nMembers: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(String.valueOf(team.getMembers().size())).withStyle(ChatFormatting.WHITE))
                .append(Component.literal("\nFriendly Fire: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(team.isFriendlyFire() ? "Enabled" : "Disabled").withStyle(team.isFriendlyFire() ? ChatFormatting.RED : ChatFormatting.GREEN))
                .append(Component.literal("\nColor: ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(team.getColor().getName()).withStyle(team.getColor()));

        if (team.hasIcon()) {
            info.append(Component.literal("\nIcon: ").withStyle(ChatFormatting.GRAY))
                    .append(team.getIconItem().getHoverName());
        }
        if (team.getPrefix().isEnabled()) {
            info.append(Component.literal("\nPrefix: ").withStyle(ChatFormatting.GRAY))
                    .append(team.getPrefix().toComponent());
        }
        if (team.getSuffix().isEnabled()) {
            info.append(Component.literal("\nSuffix: ").withStyle(ChatFormatting.GRAY))
                    .append(team.getSuffix().toComponent());
        }
        if (!team.getMotd().isEmpty()) {
            info.append(Component.literal("\nMOTD: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(team.getMotd()).withStyle(ChatFormatting.YELLOW));
        }
        info.append(Component.literal("\n=============================\n").withStyle(ChatFormatting.GOLD));

        source.sendSuccess(() -> info, false);
        return 1;
    }

    private static int executeList(CommandContext<CommandSourceStack> context, int page) {
        Collection<Team> allTeams = TeamManager.getAllTeams(context.getSource().getServer());
        if (allTeams.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("No teams registered.").withStyle(ChatFormatting.GRAY), false);
            return 1;
        }

        List<Team> list = new ArrayList<>(allTeams);
        int perPage = 8;
        int maxPage = (list.size() - 1) / perPage + 1;
        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, list.size());

        MutableComponent header = Component.literal("\n=== Server Teams (Page " + page + "/" + maxPage + ", Total: " + list.size() + ") ===\n")
                .withStyle(ChatFormatting.GOLD);

        for (int i = start; i < end; i++) {
            Team t = list.get(i);
            header.append(Component.literal(" - ").withStyle(ChatFormatting.GRAY))
                    .append(t.getFormattedDisplayName())
                    .append(Component.literal(" (ID: " + t.getId() + ") - " + t.getMembers().size() + " members [Leader: " + t.getId() + "]\n").withStyle(ChatFormatting.GRAY));
        }

        context.getSource().sendSuccess(() -> header, false);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestTeamMembers(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
            if (team != null) {
                return SharedSuggestionProvider.suggest(
                        team.getMembers().values().stream().map(TeamMember::getName), builder);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestAllTeamIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Collection<Team> teams = TeamManager.getAllTeams(context.getSource().getServer());
        return SharedSuggestionProvider.suggest(
                teams.stream().map(Team::getId), builder);
    }

    private static CompletableFuture<Suggestions> suggestAllRanks(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
            if (team != null) {
                return SharedSuggestionProvider.suggest(team.getRanks().keySet(), builder);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestCustomRanks(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            Team team = TeamManager.getTeamByPlayer(player.server, player.getUUID());
            if (team != null) {
                return SharedSuggestionProvider.suggest(
                        team.getRanks().keySet().stream().filter(id -> !TeamRank.LEADER_ID.equalsIgnoreCase(id) && !TeamRank.MEMBER_ID.equalsIgnoreCase(id)),
                        builder);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestPermissions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                Arrays.stream(TeamPermission.values()).map(Enum::name), builder);
    }
}
