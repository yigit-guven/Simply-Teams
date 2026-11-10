package net.yigitguven.simply_teams.team;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.yigitguven.simply_teams.Config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {
    private static final Map<UUID, Map<String, TeamInvite>> pendingInvites = new ConcurrentHashMap<>();
    private static final Set<UUID> teamChatModePlayers = ConcurrentHashMap.newKeySet();

    public static boolean isTeamChatMode(UUID playerUUID) {
        return teamChatModePlayers.contains(playerUUID);
    }

    public static boolean toggleTeamChatMode(UUID playerUUID) {
        if (teamChatModePlayers.contains(playerUUID)) {
            teamChatModePlayers.remove(playerUUID);
            return false;
        } else {
            teamChatModePlayers.add(playerUUID);
            return true;
        }
    }

    public static TeamSavedData getData(MinecraftServer server) {
        return TeamSavedData.get(server);
    }

    public static Team getTeamByPlayer(MinecraftServer server, UUID playerUUID) {
        return getData(server).getTeamByPlayer(playerUUID);
    }

    public static Team getTeamById(MinecraftServer server, String teamId) {
        return getData(server).getTeamById(teamId);
    }

    public static Collection<Team> getAllTeams(MinecraftServer server) {
        return getData(server).getAllTeams();
    }

    public static boolean hasTeam(MinecraftServer server, UUID playerUUID) {
        return getTeamByPlayer(server, playerUUID) != null;
    }

    public static boolean isLeader(MinecraftServer server, UUID playerUUID) {
        Team team = getTeamByPlayer(server, playerUUID);
        return team != null && team.isLeader(playerUUID);
    }

    public static boolean hasAdminPrivilege(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return Config.OPS_ARE_ADMINS.get() && player.hasPermissions(2);
        }
        return source.hasPermission(2);
    }

    public static boolean hasAdminPrivilege(ServerPlayer player) {
        return Config.OPS_ARE_ADMINS.get() && player.hasPermissions(2);
    }

    public static Team createTeam(ServerPlayer leader, String displayName) {
        MinecraftServer server = leader.server;
        UUID leaderUUID = leader.getUUID();
        String leaderNick = leader.getGameProfile().getName();

        if (hasTeam(server, leaderUUID)) {
            leader.sendSystemMessage(Component.literal("You already belong to a team!").withStyle(ChatFormatting.RED));
            return null;
        }

        int maxLength = Config.MAX_DISPLAY_NAME_LENGTH.get();
        if (maxLength > 0 && displayName.length() > maxLength) {
            leader.sendSystemMessage(Component.literal("Team display name cannot exceed " + maxLength + " characters!").withStyle(ChatFormatting.RED));
            return null;
        }

        TeamSavedData data = getData(server);
        if (data.getTeamById(leaderNick) != null) {
            leader.sendSystemMessage(Component.literal("A team with ID '" + leaderNick + "' already exists!").withStyle(ChatFormatting.RED));
            return null;
        }

        Team team = new Team(leaderNick, displayName, leaderUUID, leaderNick);
        data.addTeam(team);
        syncScoreboardTeam(team, server);

        server.getCommands().sendCommands(leader);
        refreshTabList(server, leader);

        leader.sendSystemMessage(Component.literal("Successfully created team ")
                .withStyle(ChatFormatting.GREEN)
                .append(team.getFormattedDisplayName())
                .append(Component.literal(" (ID: " + team.getId() + ")!").withStyle(ChatFormatting.GRAY)));

        return team;
    }

    public static boolean disbandTeam(ServerPlayer leader) {
        MinecraftServer server = leader.server;
        Team team = getTeamByPlayer(server, leader.getUUID());
        if (team == null) {
            leader.sendSystemMessage(Component.literal("You are not in a team!").withStyle(ChatFormatting.RED));
            return false;
        }

        if (!team.isLeader(leader.getUUID()) && !hasAdminPrivilege(leader)) {
            leader.sendSystemMessage(Component.literal("Only the team leader can disband the team!").withStyle(ChatFormatting.RED));
            return false;
        }

        for (UUID memberUUID : team.getMembers().keySet()) {
            teamChatModePlayers.remove(memberUUID);
            ServerPlayer member = server.getPlayerList().getPlayer(memberUUID);
            if (member != null) {
                member.sendSystemMessage(Component.literal("Team ")
                        .withStyle(ChatFormatting.RED)
                        .append(team.getFormattedDisplayName())
                        .append(Component.literal(" has been disbanded by the leader.").withStyle(ChatFormatting.RED)));
                server.getCommands().sendCommands(member);
                refreshTabList(server, member);
            }
        }

        removeScoreboardTeam(team, server);
        getData(server).removeTeam(team.getId());
        return true;
    }

    public static boolean leaveTeam(ServerPlayer player) {
        MinecraftServer server = player.server;
        Team team = getTeamByPlayer(server, player.getUUID());
        if (team == null) {
            player.sendSystemMessage(Component.literal("You are not in a team!").withStyle(ChatFormatting.RED));
            return false;
        }

        if (team.isLeader(player.getUUID())) {
            player.sendSystemMessage(Component.literal("As the leader, you cannot leave the team! Transfer leadership first or use /team disband.").withStyle(ChatFormatting.RED));
            return false;
        }

        teamChatModePlayers.remove(player.getUUID());
        team.removeMember(player.getUUID());
        getData(server).updatePlayerMembership(player.getUUID(), null);
        removePlayerFromScoreboardTeam(team, player.getGameProfile().getName(), server);

        server.getCommands().sendCommands(player);
        refreshTabList(server, player);

        player.sendSystemMessage(Component.literal("You left the team ")
                .withStyle(ChatFormatting.YELLOW)
                .append(team.getFormattedDisplayName()));

        broadcastToTeam(team, server, Component.literal(player.getGameProfile().getName() + " has left the team.").withStyle(ChatFormatting.YELLOW));
        return true;
    }

    public static boolean kickMember(ServerPlayer actor, String targetName) {
        MinecraftServer server = actor.server;
        Team team = getTeamByPlayer(server, actor.getUUID());
        if (team == null) {
            actor.sendSystemMessage(Component.literal("You are not in a team!").withStyle(ChatFormatting.RED));
            return false;
        }

        boolean isAdmin = hasAdminPrivilege(actor);
        if (!isAdmin && !team.hasPermission(actor.getUUID(), TeamPermission.KICK)) {
            actor.sendSystemMessage(Component.literal("You do not have permission to kick team members!").withStyle(ChatFormatting.RED));
            return false;
        }

        TeamMember targetMember = null;
        for (TeamMember m : team.getMembers().values()) {
            if (m.getName().equalsIgnoreCase(targetName)) {
                targetMember = m;
                break;
            }
        }

        if (targetMember == null) {
            actor.sendSystemMessage(Component.literal("Player '" + targetName + "' is not a member of your team!").withStyle(ChatFormatting.RED));
            return false;
        }

        if (team.isLeader(targetMember.getUuid())) {
            if (!isAdmin) {
                actor.sendSystemMessage(Component.literal("You cannot kick the team leader!").withStyle(ChatFormatting.RED));
                return false;
            }
        }

        if (!isAdmin && !team.isLeader(actor.getUUID())) {
            TeamRank actorRank = team.getMemberRank(actor.getUUID());
            TeamRank targetRank = team.getMemberRank(targetMember.getUuid());
            int actorP = actorRank != null ? actorRank.getPriority() : 0;
            int targetP = targetRank != null ? targetRank.getPriority() : 0;
            if (actorP <= targetP) {
                actor.sendSystemMessage(Component.literal("You cannot kick a member with an equal or higher rank!").withStyle(ChatFormatting.RED));
                return false;
            }
        }

        UUID targetUUID = targetMember.getUuid();
        teamChatModePlayers.remove(targetUUID);
        team.removeMember(targetUUID);
        getData(server).updatePlayerMembership(targetUUID, null);
        removePlayerFromScoreboardTeam(team, targetName, server);

        ServerPlayer targetOnline = server.getPlayerList().getPlayer(targetUUID);
        if (targetOnline != null) {
            targetOnline.sendSystemMessage(Component.literal("You have been kicked from team ")
                    .withStyle(ChatFormatting.RED)
                    .append(team.getFormattedDisplayName()));
            server.getCommands().sendCommands(targetOnline);
            refreshTabList(server, targetOnline);
        }

        broadcastToTeam(team, server, Component.literal(targetName + " has been kicked from the team by " + actor.getGameProfile().getName() + ".").withStyle(ChatFormatting.YELLOW));
        return true;
    }

    public static boolean sendInvite(Team team, ServerPlayer inviter, ServerPlayer target) {
        if (target.getUUID().equals(inviter.getUUID())) {
            inviter.sendSystemMessage(Component.literal("You cannot invite yourself!").withStyle(ChatFormatting.RED));
            return false;
        }

        MinecraftServer server = inviter.server;
        if (hasTeam(server, target.getUUID())) {
            inviter.sendSystemMessage(Component.literal(target.getGameProfile().getName() + " is already in a team!").withStyle(ChatFormatting.RED));
            return false;
        }

        long timeoutMillis = Config.INVITE_TIMEOUT_SECONDS.get() * 1000L;
        TeamInvite invite = new TeamInvite(team.getId(), team.getDisplayName(), inviter.getUUID(), inviter.getGameProfile().getName(), target.getUUID(), timeoutMillis);

        pendingInvites.computeIfAbsent(target.getUUID(), k -> new ConcurrentHashMap<>()).put(team.getId().toLowerCase(), invite);

        inviter.sendSystemMessage(Component.literal("Sent team invitation to " + target.getGameProfile().getName() + " (expires in " + Config.INVITE_TIMEOUT_SECONDS.get() + "s).").withStyle(ChatFormatting.GREEN));

        MutableComponent inviteMsg = Component.literal("\n========================================\n")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("You have been invited to join team ").withStyle(ChatFormatting.YELLOW))
                .append(team.getFormattedDisplayName())
                .append(Component.literal(" by " + inviter.getGameProfile().getName() + "!\n").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("[CLICK HERE TO JOIN]")
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.GREEN)
                                .withBold(true)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/team accept " + inviter.getGameProfile().getName()))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to join team " + team.getDisplayName())))))
                .append(Component.literal(" or type ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("/team accept " + inviter.getGameProfile().getName()).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("\nThis invitation expires in " + Config.INVITE_TIMEOUT_SECONDS.get() + " seconds.\n========================================\n").withStyle(ChatFormatting.GOLD));

        target.sendSystemMessage(inviteMsg);
        return true;
    }

    public static boolean acceptInvite(ServerPlayer player, String query) {
        MinecraftServer server = player.server;
        UUID playerUUID = player.getUUID();

        if (hasTeam(server, playerUUID)) {
            player.sendSystemMessage(Component.literal("You are already in a team! Leave your current team first.").withStyle(ChatFormatting.RED));
            return false;
        }

        Map<String, TeamInvite> playerInvites = pendingInvites.get(playerUUID);
        if (playerInvites == null || playerInvites.isEmpty()) {
            player.sendSystemMessage(Component.literal("You have no pending team invitations!").withStyle(ChatFormatting.RED));
            return false;
        }

        playerInvites.values().removeIf(TeamInvite::isExpired);

        TeamInvite matchedInvite = null;
        if (query == null || query.trim().isEmpty()) {
            if (playerInvites.size() == 1) {
                matchedInvite = playerInvites.values().iterator().next();
            } else {
                player.sendSystemMessage(Component.literal("You have multiple invitations. Please specify the team ID or inviter name: /team accept <name>").withStyle(ChatFormatting.YELLOW));
                return false;
            }
        } else {
            String q = query.trim().toLowerCase();
            for (TeamInvite inv : playerInvites.values()) {
                if (inv.getTeamId().equalsIgnoreCase(q) ||
                    inv.getInviterName().equalsIgnoreCase(q) ||
                    inv.getTeamDisplayName().equalsIgnoreCase(q)) {
                    matchedInvite = inv;
                    break;
                }
            }
        }

        if (matchedInvite == null || matchedInvite.isExpired()) {
            player.sendSystemMessage(Component.literal("No active invitation found matching '" + (query != null ? query : "") + "'!").withStyle(ChatFormatting.RED));
            return false;
        }

        Team team = getTeamById(server, matchedInvite.getTeamId());
        if (team == null) {
            player.sendSystemMessage(Component.literal("That team no longer exists!").withStyle(ChatFormatting.RED));
            playerInvites.remove(matchedInvite.getTeamId().toLowerCase());
            return false;
        }

        team.addMember(playerUUID, player.getGameProfile().getName(), TeamRank.MEMBER_ID);
        getData(server).updatePlayerMembership(playerUUID, team.getId());
        playerInvites.remove(matchedInvite.getTeamId().toLowerCase());

        syncScoreboardTeam(team, server);
        server.getCommands().sendCommands(player);
        refreshTabList(server, player);

        player.sendSystemMessage(Component.literal("You have joined team ")
                .withStyle(ChatFormatting.GREEN)
                .append(team.getFormattedDisplayName())
                .append(Component.literal("!").withStyle(ChatFormatting.GREEN)));

        broadcastToTeam(team, server, Component.literal(player.getGameProfile().getName() + " has joined the team!").withStyle(ChatFormatting.GREEN));
        return true;
    }

    public static void broadcastToTeam(Team team, MinecraftServer server, Component message) {
        for (UUID memberUUID : team.getMembers().keySet()) {
            ServerPlayer member = server.getPlayerList().getPlayer(memberUUID);
            if (member != null) {
                member.sendSystemMessage(message);
            }
        }
    }

    public static void sendTeamChat(ServerPlayer sender, String message) {
        MinecraftServer server = sender.server;
        Team team = getTeamByPlayer(server, sender.getUUID());
        if (team == null) {
            sender.sendSystemMessage(Component.literal("You are not in a team!").withStyle(ChatFormatting.RED));
            return;
        }

        TeamRank rank = team.getMemberRank(sender.getUUID());
        MutableComponent formatted = Component.literal("[TeamChat] ")
                .withStyle(ChatFormatting.AQUA)
                .append(rank != null ? rank.getFormattedName() : Component.empty())
                .append(Component.literal(" " + sender.getGameProfile().getName() + ": ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(message).withStyle(ChatFormatting.GRAY));

        broadcastToTeam(team, server, formatted);
    }

    public static void syncScoreboardTeam(Team team, MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        String teamName = "st_" + team.getId().toLowerCase();
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }

        PlayerTeam pTeam = scoreboard.getPlayerTeam(teamName);
        if (pTeam == null) {
            pTeam = scoreboard.addPlayerTeam(teamName);
        }

        pTeam.setDisplayName(Component.literal(team.getDisplayName()));
        pTeam.setColor(team.getColor());
        pTeam.setAllowFriendlyFire(team.isFriendlyFire());

        if (Config.ALLOW_TEAM_PREFIX.get() && team.getPrefix().isEnabled()) {
            pTeam.setPlayerPrefix(Component.literal("").append(team.getPrefix().toComponent()).append(" "));
        } else {
            pTeam.setPlayerPrefix(Component.empty());
        }

        if (Config.ALLOW_TEAM_SUFFIX.get() && team.getSuffix().isEnabled()) {
            pTeam.setPlayerSuffix(Component.literal(" ").append(team.getSuffix().toComponent()));
        } else {
            pTeam.setPlayerSuffix(Component.empty());
        }

        for (TeamMember member : team.getMembers().values()) {
            if (!pTeam.getPlayers().contains(member.getName())) {
                scoreboard.addPlayerToTeam(member.getName(), pTeam);
            }
        }
    }

    public static void removeScoreboardTeam(Team team, MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        String teamName = "st_" + team.getId().toLowerCase();
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }
        PlayerTeam pTeam = scoreboard.getPlayerTeam(teamName);
        if (pTeam != null) {
            scoreboard.removePlayerTeam(pTeam);
        }
    }

    public static void removePlayerFromScoreboardTeam(Team team, String playerName, MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        String teamName = "st_" + team.getId().toLowerCase();
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }
        PlayerTeam pTeam = scoreboard.getPlayerTeam(teamName);
        if (pTeam != null) {
            scoreboard.removePlayerFromTeam(playerName, pTeam);
        }
    }

    public static void refreshTabList(MinecraftServer server, ServerPlayer player) {
        server.getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, player));
    }

    public static void refreshTabListAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            refreshTabList(server, player);
        }
    }

    public static boolean adminDisbandTeam(MinecraftServer server, String teamId, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        for (UUID memberUUID : team.getMembers().keySet()) {
            teamChatModePlayers.remove(memberUUID);
            ServerPlayer member = server.getPlayerList().getPlayer(memberUUID);
            if (member != null) {
                member.sendSystemMessage(Component.literal("Team ")
                        .withStyle(ChatFormatting.RED)
                        .append(team.getFormattedDisplayName())
                        .append(Component.literal(" has been disbanded by an administrator.").withStyle(ChatFormatting.RED)));
                server.getCommands().sendCommands(member);
                refreshTabList(server, member);
            }
        }

        removeScoreboardTeam(team, server);
        getData(server).removeTeam(team.getId());

        actorSource.sendSuccess(() -> Component.literal("Team '" + team.getDisplayName() + "' (ID: " + team.getId() + ") has been deleted.")
                .withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    public static boolean adminKickMember(MinecraftServer server, String teamId, String memberName, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        TeamMember targetMember = null;
        for (TeamMember m : team.getMembers().values()) {
            if (m.getName().equalsIgnoreCase(memberName)) {
                targetMember = m;
                break;
            }
        }

        if (targetMember == null) {
            actorSource.sendFailure(Component.literal("Member '" + memberName + "' not found in team '" + teamId + "'!"));
            return false;
        }

        if (team.isLeader(targetMember.getUuid())) {
            actorSource.sendFailure(Component.literal("Cannot kick the team leader! Transfer leadership first using /team admin setleader or delete the team using /team admin delete."));
            return false;
        }

        UUID targetUUID = targetMember.getUuid();
        teamChatModePlayers.remove(targetUUID);
        team.removeMember(targetUUID);
        getData(server).updatePlayerMembership(targetUUID, null);
        removePlayerFromScoreboardTeam(team, targetMember.getName(), server);

        ServerPlayer targetOnline = server.getPlayerList().getPlayer(targetUUID);
        if (targetOnline != null) {
            targetOnline.sendSystemMessage(Component.literal("You have been removed from team ")
                    .withStyle(ChatFormatting.RED)
                    .append(team.getFormattedDisplayName())
                    .append(Component.literal(" by an administrator.").withStyle(ChatFormatting.RED)));
            server.getCommands().sendCommands(targetOnline);
            refreshTabList(server, targetOnline);
        }

        broadcastToTeam(team, server, Component.literal(targetMember.getName() + " was removed from the team by an administrator.").withStyle(ChatFormatting.YELLOW));

        TeamMember finalTargetMember = targetMember;
        actorSource.sendSuccess(() -> Component.literal("Successfully removed " + finalTargetMember.getName() + " from team " + team.getId() + ".")
                .withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    public static boolean adminAddMember(MinecraftServer server, String teamId, ServerPlayer target, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        if (team.isMember(target.getUUID())) {
            actorSource.sendFailure(Component.literal(target.getGameProfile().getName() + " is already a member of team '" + teamId + "'!"));
            return false;
        }

        Team existingTeam = getTeamByPlayer(server, target.getUUID());
        if (existingTeam != null) {
            if (existingTeam.isLeader(target.getUUID())) {
                actorSource.sendFailure(Component.literal(target.getGameProfile().getName() + " is leader of another team (" + existingTeam.getId() + ")! Transfer leadership or disband it first."));
                return false;
            }
            existingTeam.removeMember(target.getUUID());
            removePlayerFromScoreboardTeam(existingTeam, target.getGameProfile().getName(), server);
            broadcastToTeam(existingTeam, server, Component.literal(target.getGameProfile().getName() + " was transferred to another team by an administrator.").withStyle(ChatFormatting.YELLOW));
        }

        team.addMember(target.getUUID(), target.getGameProfile().getName(), TeamRank.MEMBER_ID);
        getData(server).updatePlayerMembership(target.getUUID(), team.getId());
        syncScoreboardTeam(team, server);
        server.getCommands().sendCommands(target);
        refreshTabList(server, target);

        target.sendSystemMessage(Component.literal("You were added to team ")
                .withStyle(ChatFormatting.GREEN)
                .append(team.getFormattedDisplayName())
                .append(Component.literal(" by an administrator.").withStyle(ChatFormatting.GREEN)));

        broadcastToTeam(team, server, Component.literal(target.getGameProfile().getName() + " was added to the team by an administrator.").withStyle(ChatFormatting.GREEN));

        actorSource.sendSuccess(() -> Component.literal("Added " + target.getGameProfile().getName() + " to team " + team.getId() + ".")
                .withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    public static boolean adminSetLeader(MinecraftServer server, String teamId, ServerPlayer newLeader, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        UUID newLeaderUUID = newLeader.getUUID();
        if (!team.isMember(newLeaderUUID)) {
            if (!adminAddMember(server, teamId, newLeader, actorSource)) {
                return false;
            }
        }

        UUID oldLeaderUUID = team.getLeaderUUID();
        TeamMember oldLeaderMember = team.getMember(oldLeaderUUID);
        if (oldLeaderMember != null) {
            oldLeaderMember.setRankId(TeamRank.OFFICER_ID);
        }

        team.setLeaderUUID(newLeaderUUID);
        TeamMember newLeaderMember = team.getMember(newLeaderUUID);
        if (newLeaderMember != null) {
            newLeaderMember.setRankId(TeamRank.LEADER_ID);
        }

        getData(server).setDirty();
        syncScoreboardTeam(team, server);
        refreshTabListAll(server);

        ServerPlayer oldLeader = server.getPlayerList().getPlayer(oldLeaderUUID);
        if (oldLeader != null) {
            server.getCommands().sendCommands(oldLeader);
        }
        server.getCommands().sendCommands(newLeader);

        broadcastToTeam(team, server, Component.literal("Team leadership was set to " + newLeader.getGameProfile().getName() + " by an administrator.")
                .withStyle(ChatFormatting.GOLD));

        actorSource.sendSuccess(() -> Component.literal("Team leadership of " + team.getId() + " set to " + newLeader.getGameProfile().getName() + ".")
                .withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    public static boolean adminRename(MinecraftServer server, String teamId, String newName, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        int max = Config.MAX_DISPLAY_NAME_LENGTH.get();
        if (max > 0 && newName.length() > max) {
            actorSource.sendFailure(Component.literal("Team display name cannot exceed " + max + " characters!"));
            return false;
        }

        team.setDisplayName(newName);
        getData(server).setDirty();
        syncScoreboardTeam(team, server);
        refreshTabListAll(server);

        broadcastToTeam(team, server, Component.literal("Team display name was set to: ")
                .withStyle(ChatFormatting.GREEN)
                .append(team.getFormattedDisplayName()));

        actorSource.sendSuccess(() -> Component.literal("Renamed team " + team.getId() + " to: ")
                .withStyle(ChatFormatting.GREEN)
                .append(team.getFormattedDisplayName()), true);
        return true;
    }

    public static boolean adminColor(MinecraftServer server, String teamId, ChatFormatting color, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setColor(color);
        getData(server).setDirty();
        syncScoreboardTeam(team, server);
        refreshTabListAll(server);

        actorSource.sendSuccess(() -> Component.literal("Team color for " + team.getId() + " set to " + color.getName())
                .withStyle(color), true);
        return true;
    }

    public static boolean adminPrefix(MinecraftServer server, String teamId, String tag, ChatFormatting color, boolean bold, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setPrefix(new TeamTag(tag, color, bold, true));
        getData(server).setDirty();
        syncScoreboardTeam(team, server);
        refreshTabListAll(server);

        actorSource.sendSuccess(() -> Component.literal("Set prefix for team " + team.getId() + " to ")
                .withStyle(ChatFormatting.GREEN)
                .append(team.getPrefix().toComponent()), true);
        return true;
    }

    public static boolean adminClearPrefix(MinecraftServer server, String teamId, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setPrefix(TeamTag.empty());
        getData(server).setDirty();
        syncScoreboardTeam(team, server);
        refreshTabListAll(server);

        actorSource.sendSuccess(() -> Component.literal("Cleared prefix for team " + team.getId() + ".").withStyle(ChatFormatting.YELLOW), true);
        return true;
    }

    public static boolean adminSuffix(MinecraftServer server, String teamId, String tag, ChatFormatting color, boolean bold, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setSuffix(new TeamTag(tag, color, bold, true));
        getData(server).setDirty();
        syncScoreboardTeam(team, server);
        refreshTabListAll(server);

        actorSource.sendSuccess(() -> Component.literal("Set suffix for team " + team.getId() + " to ")
                .withStyle(ChatFormatting.GREEN)
                .append(team.getSuffix().toComponent()), true);
        return true;
    }

    public static boolean adminClearSuffix(MinecraftServer server, String teamId, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setSuffix(TeamTag.empty());
        getData(server).setDirty();
        syncScoreboardTeam(team, server);
        refreshTabListAll(server);

        actorSource.sendSuccess(() -> Component.literal("Cleared suffix for team " + team.getId() + ".").withStyle(ChatFormatting.YELLOW), true);
        return true;
    }

    public static boolean adminIcon(MinecraftServer server, String teamId, ItemStack icon, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setIconItem(icon);
        getData(server).setDirty();
        refreshTabListAll(server);

        actorSource.sendSuccess(() -> Component.literal("Set icon for team " + team.getId() + " to ")
                .withStyle(ChatFormatting.GREEN)
                .append(icon.getHoverName()), true);
        return true;
    }

    public static boolean adminClearIcon(MinecraftServer server, String teamId, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setIconItem(ItemStack.EMPTY);
        getData(server).setDirty();
        refreshTabListAll(server);

        actorSource.sendSuccess(() -> Component.literal("Cleared icon for team " + team.getId() + ".").withStyle(ChatFormatting.YELLOW), true);
        return true;
    }

    public static boolean adminFriendlyFire(MinecraftServer server, String teamId, boolean enabled, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setFriendlyFire(enabled);
        getData(server).setDirty();
        syncScoreboardTeam(team, server);

        actorSource.sendSuccess(() -> Component.literal("Friendly fire for team " + team.getId() + " set to " + (enabled ? "enabled" : "disabled") + ".")
                .withStyle(enabled ? ChatFormatting.RED : ChatFormatting.GREEN), true);
        return true;
    }

    public static boolean adminMotd(MinecraftServer server, String teamId, String motd, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setMotd(motd);
        getData(server).setDirty();

        actorSource.sendSuccess(() -> Component.literal("Set MOTD for team " + team.getId() + " to: " + motd).withStyle(ChatFormatting.YELLOW), true);
        return true;
    }

    public static boolean adminClearMotd(MinecraftServer server, String teamId, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setMotd("");
        getData(server).setDirty();

        actorSource.sendSuccess(() -> Component.literal("Cleared MOTD for team " + team.getId() + ".").withStyle(ChatFormatting.YELLOW), true);
        return true;
    }

    public static boolean adminDelHome(MinecraftServer server, String teamId, CommandSourceStack actorSource) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actorSource.sendFailure(Component.literal("No team found with ID '" + teamId + "'!"));
            return false;
        }

        team.setHome(null);
        getData(server).setDirty();

        actorSource.sendSuccess(() -> Component.literal("Deleted home base for team " + team.getId() + ".").withStyle(ChatFormatting.YELLOW), true);
        return true;
    }

    public static boolean adminSetHome(MinecraftServer server, String teamId, ServerPlayer actor) {
        Team team = getTeamById(server, teamId);
        if (team == null) {
            actor.sendSystemMessage(Component.literal("No team found with ID '" + teamId + "'!").withStyle(ChatFormatting.RED));
            return false;
        }

        team.setHome(TeamHome.fromPlayer(actor));
        getData(server).setDirty();

        actor.sendSystemMessage(Component.literal("Set home base for team " + team.getId() + " to your current location.")
                .withStyle(ChatFormatting.GREEN));
        return true;
    }

    public static boolean adminTeleportHome(ServerPlayer player, String teamId) {
        Team team = getTeamById(player.server, teamId);
        if (team == null) {
            player.sendSystemMessage(Component.literal("No team found with ID '" + teamId + "'!").withStyle(ChatFormatting.RED));
            return false;
        }

        if (!team.hasHome()) {
            player.sendSystemMessage(Component.literal("Team '" + teamId + "' does not have a home base set.").withStyle(ChatFormatting.RED));
            return false;
        }

        team.getHome().teleport(player);
        player.sendSystemMessage(Component.literal("Teleported to home base of team " + team.getDisplayName() + ".").withStyle(ChatFormatting.GREEN));
        return true;
    }
}
