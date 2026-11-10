package net.yigitguven.simply_teams.team;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class TeamSavedData extends SavedData {
    public static final String DATA_NAME = "simply_teams";

    private final Map<String, Team> teamsById = new LinkedHashMap<>();
    private final Map<UUID, String> playerToTeam = new HashMap<>();

    public TeamSavedData() {
    }

    public static Factory<TeamSavedData> factory() {
        return new Factory<>(TeamSavedData::new, TeamSavedData::load);
    }

    public static TeamSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public Collection<Team> getAllTeams() {
        return Collections.unmodifiableCollection(teamsById.values());
    }

    public Team getTeamById(String teamId) {
        if (teamId == null) return null;
        return teamsById.get(teamId.toLowerCase());
    }

    public Team getTeamByPlayer(UUID playerUUID) {
        if (playerUUID == null) return null;
        String teamId = playerToTeam.get(playerUUID);
        if (teamId == null) return null;
        return teamsById.get(teamId);
    }

    public void addTeam(Team team) {
        teamsById.put(team.getId().toLowerCase(), team);
        for (UUID uuid : team.getMembers().keySet()) {
            playerToTeam.put(uuid, team.getId().toLowerCase());
        }
        setDirty();
    }

    public void removeTeam(String teamId) {
        Team removed = teamsById.remove(teamId.toLowerCase());
        if (removed != null) {
            for (UUID uuid : removed.getMembers().keySet()) {
                playerToTeam.remove(uuid);
            }
            setDirty();
        }
    }

    public void updatePlayerMembership(UUID playerUUID, String teamId) {
        if (teamId != null) {
            playerToTeam.put(playerUUID, teamId.toLowerCase());
        } else {
            playerToTeam.remove(playerUUID);
        }
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Team team : teamsById.values()) {
            list.add(team.toNBT(registries));
        }
        tag.put("teams", list);
        return tag;
    }

    public static TeamSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        TeamSavedData data = new TeamSavedData();
        if (tag.contains("teams", Tag.TAG_LIST)) {
            ListTag list = tag.getList("teams", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                Team team = Team.fromNBT(list.getCompound(i), registries);
                data.teamsById.put(team.getId().toLowerCase(), team);
                for (UUID uuid : team.getMembers().keySet()) {
                    data.playerToTeam.put(uuid, team.getId().toLowerCase());
                }
            }
        }
        return data;
    }
}

