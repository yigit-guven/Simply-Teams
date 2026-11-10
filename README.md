# Simply Teams

<p align="center">
  <img src="logo.gif" alt="Simply Teams" width="320" />
</p>

A server-sided Minecraft mod that replaces the vanilla `/team` command with an expanded team management system.

## Project Information

- Author: Yigit_Guven
- License: MIT
- Environment: Server-side only (clients do not need the mod installed)

## Links

- GitHub Repository: https://github.com/yigit-guven/Simply-Teams
- Issue Tracker: https://github.com/yigit-guven/Simply-Teams/issues
- Documentation Wiki: https://github.com/yigit-guven/Simply-Teams/wiki
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/simply-teams/
- Modrinth: https://modrinth.com/mod/simply-teams/

---

## Overview

Simply Teams unhooks the vanilla `/team` command tree and replaces it with an independent team system. The mod runs entirely on the server. Connecting clients (vanilla, modded, or proxy connections like Geyser) do not need to install the mod to join or use any features.

### Key Capabilities

- Command replacement: The vanilla `/team` command is completely removed and replaced.
- Context-sensitive suggestions: Players without a team only see commands relevant to team creation or joining. Players in a team see operational commands. Command trees update dynamically without requiring reconnection.
- Spaced display names: Team names can contain spaces. Maximum length is configurable (default is unlimited).
- Leader ID: The internal team identifier matches the founder's username.
- Invitations: Interactive, clickable chat invitations with configurable expiration timeouts.
- Members UI: Uses the native 54-slot chest interface (`MenuType.GENERIC_9x6`) to show member player heads, skins (online and offline), rank formatting, join dates, and pagination. Slots are read-only to prevent item movement.
- Tab list formatting: Displays team prefix, player name, team icon, and team suffix in the player list.
- Team icon: Captures the item held in the player's main hand and sets it as the team icon.
- Rank management: Customizable rank hierarchy with priority numbers, colors, bold formatting, and granular permissions.
- Team home: Allows setting and teleporting to a team base.
- Team chat: Dedicated team chat command and a persistent team chat toggle mode.
- Friendly fire: Configurable per-team damage protection between teammates.
- Persistence: All data is saved to world storage (`TeamSavedData`) in NBT format and persists across server reboots.
- Scoreboard integration: Syncs with vanilla scoreboard teams for name colors and nametag visibility.

---

## Commands

### Players Without a Team

| Command | Arguments | Description |
|---|---|---|
| `/team create <name>` | Greedy string | Creates a new team with the specified display name. |
| `/team accept [target]` | Optional string | Accepts an invite from a player or team ID. |
| `/team info [teamId]` | Optional word | Displays information about a team. |
| `/team list [page]` | Optional integer | Lists all teams registered on the server. |

### Team Members

| Command | Arguments | Permission | Description |
|---|---|---|---|
| `/team members` | None | None | Opens the chest GUI displaying all team members. |
| `/team info` | None | None | Displays current team statistics and settings. |
| `/team leave` | None | None | Leaves the current team (leaders must transfer or disband). |
| `/team chat` | None | `CHAT` | Toggles persistent team chat mode on or off. |
| `/team chat <message>` | Greedy string | `CHAT` | Sends a message to team members only. |
| `/team c <message>` | Greedy string | `CHAT` | Alias for `/team chat <message>`. |
| `/team invite <player>` | Player entity | `INVITE` | Sends an invitation to an online player. |
| `/team kick <member>` | Member name | `KICK` | Removes a member of lower rank from the team. |
| `/team rename <name>` | Greedy string | `RENAME` | Changes the team display name. |
| `/team icon` | None | `CHANGE_ICON` | Sets the team icon to the held main-hand item. |
| `/team icon clear` | None | `CHANGE_ICON` | Clears the team icon. |
| `/team color <color>` | Color name | `CHANGE_COLOR` | Sets the team color. |
| `/team prefix <tag> <color> <bold>` | Word, Color, Boolean | `CHANGE_PREFIX` | Sets the team prefix text and styling. |
| `/team prefix clear` | None | `CHANGE_PREFIX` | Clears the team prefix. |
| `/team suffix <tag> <color> <bold>` | Word, Color, Boolean | `CHANGE_SUFFIX` | Sets the team suffix text and styling. |
| `/team suffix clear` | None | `CHANGE_SUFFIX` | Clears the team suffix. |
| `/team friendlyfire [enabled]` | Optional boolean | `TOGGLE_FRIENDLY_FIRE` | Toggles or sets friendly fire for the team. |
| `/team ff [enabled]` | Optional boolean | `TOGGLE_FRIENDLY_FIRE` | Alias for `/team friendlyfire`. |
| `/team motd <message>` | Greedy string | `RENAME` | Sets the team message of the day. |
| `/team motd clear` | None | `RENAME` | Clears the team message of the day. |
| `/team sethome` | None | `SET_HOME` | Sets the team home base at current position. |
| `/team home` | None | None | Teleports to the team home base. |
| `/team delhome` | None | `SET_HOME` | Deletes the team home base. |

### Leader-Only Commands

| Command | Arguments | Description |
|---|---|---|
| `/team transfer <member>` | Player entity | Transfers team leadership to another member. |
| `/team disband` | None | Initiates team disbanding (requires confirmation). |
| `/team disband confirm` | None | Confirms and permanently disbands the team. |

### Rank Management Commands

| Command | Arguments | Permission | Description |
|---|---|---|---|
| `/team rank list` | None | None | Lists all ranks, priorities, and permissions. |
| `/team rank create <id> <name> [priority]` | Word, String, Integer (1-99) | `MANAGE_RANKS` | Creates a new custom rank. |
| `/team rank delete <id>` | Word | `MANAGE_RANKS` | Deletes a custom rank. |
| `/team rank set <member> <rankId>` | Word, Word | `SET_RANK` | Assigns a member to a rank. |
| `/team rank color <rankId> <color>` | Word, Color | `MANAGE_RANKS` | Changes rank display color. |
| `/team rank bold <rankId> <boolean>` | Word, Boolean | `MANAGE_RANKS` | Toggles bold formatting for a rank. |
| `/team rank permission <rankId> add <perm>` | Word, Permission name | `MANAGE_RANKS` | Adds a permission to a rank. |
| `/team rank permission <rankId> remove <perm>` | Word, Permission name | `MANAGE_RANKS` | Removes a permission from a rank. |

### Administrator Commands (OP Level 2+ / Console)

Available when `opsAreAdmins` is enabled in configuration (`true` by default). When `opsAreAdmins` is `false`, operators are treated strictly as regular players and cannot access these commands.

| Command | Arguments | Description |
|---|---|---|
| `/team admin delete <teamId>` | Team ID | Disbands and permanently deletes any team. |
| `/team admin disband <teamId>` | Team ID | Alias for `/team admin delete`. |
| `/team admin kick <teamId> <member>` | Team ID, Member name | Removes a member from any team. |
| `/team admin add <teamId> <player>` | Team ID, Player entity | Forcefully adds a player to any team. |
| `/team admin setleader <teamId> <player>` | Team ID, Player entity | Sets the leader of any team. |
| `/team admin rename <teamId> <name>` | Team ID, Greedy string | Renames any team. |
| `/team admin color <teamId> <color>` | Team ID, Color name | Changes any team's color. |
| `/team admin prefix <teamId> <tag> <color> <bold>` | Team ID, Word, Color, Boolean | Sets any team's prefix. |
| `/team admin prefix <teamId> clear` | Team ID | Clears any team's prefix. |
| `/team admin suffix <teamId> <tag> <color> <bold>` | Team ID, Word, Color, Boolean | Sets any team's suffix. |
| `/team admin suffix <teamId> clear` | Team ID | Clears any team's suffix. |
| `/team admin icon <teamId>` | Team ID | Sets any team's icon to the held item. |
| `/team admin icon <teamId> clear` | Team ID | Clears any team's icon. |
| `/team admin friendlyfire <teamId> <enabled>` | Team ID, Boolean | Sets friendly fire for any team. |
| `/team admin motd <teamId> <message>` | Team ID, Greedy string | Sets MOTD for any team. |
| `/team admin motd <teamId> clear` | Team ID | Clears MOTD for any team. |
| `/team admin sethome <teamId>` | Team ID | Sets any team's home base at current position. |
| `/team admin delhome <teamId>` | Team ID | Deletes any team's home base. |
| `/team admin home <teamId>` | Team ID | Teleports to any team's home base. |
| `/team admin tp <teamId>` | Team ID | Alias for `/team admin home`. |
| `/team admin members <teamId>` | Team ID | Opens the 54-slot chest GUI for any team. |
| `/team admin info <teamId>` | Team ID | Displays detailed team information. |
| `/team admin list [page]` | Optional integer | Lists all registered teams with member counts. |
| `/team admin reload` | None | Refreshes command trees and tab list for all players. |

---

## Permissions Reference

| Permission Node | Description |
|---|---|
| `INVITE` | Allows inviting other players to the team. |
| `KICK` | Allows kicking members of lower rank from the team. |
| `CHANGE_ICON` | Allows setting or clearing the team icon item. |
| `CHANGE_COLOR` | Allows changing the team color. |
| `CHANGE_PREFIX` | Allows setting or clearing the team prefix. |
| `CHANGE_SUFFIX` | Allows setting or clearing the team suffix. |
| `MANAGE_RANKS` | Allows creating, deleting, and editing rank properties. |
| `SET_RANK` | Allows assigning ranks to members of lower rank. |
| `RENAME` | Allows changing the display name and MOTD of the team. |
| `CHAT` | Allows sending messages in team chat. |
| `TOGGLE_FRIENDLY_FIRE` | Allows toggling friendly fire for the team. |
| `SET_HOME` | Allows setting and deleting the team home base. |

Default rank structure:
- `leader`: Priority 100, Gold, Bold, contains all permissions. Cannot be demoted or deleted.
- `officer`: Priority 50, Aqua, contains `INVITE`, `KICK`, `CHANGE_ICON`, `CHAT`.
- `member`: Priority 10, Gray, contains `CHAT`.

---

## Server Configuration

Configuration file location: `world/serverconfig/simply_teams-server.toml` (or `config/simply_teams-server.toml`).

```toml
[teams]
  # Maximum character length for team display names. Use -1 for unlimited length.
  # Range: -1 ~ 256
  maxDisplayNameLength = -1

  # Whether teams are allowed to have custom colors.
  allowTeamColor = true

  # Whether teams are allowed to have custom prefixes.
  allowTeamPrefix = true

  # Whether teams are allowed to have custom suffixes.
  allowTeamSuffix = true

  # Maximum character length for team prefixes. Use -1 for unlimited.
  # Range: -1 ~ 64
  maxPrefixLength = 16

  # Maximum character length for team suffixes. Use -1 for unlimited.
  # Range: -1 ~ 64
  maxSuffixLength = 16

  # Duration in seconds before a team invitation expires.
  # Range: 5 ~ 600
  inviteTimeoutSeconds = 60

  # Whether teams can set and teleport to a shared team home base.
  allowTeamHome = true

[admin]
  # Whether server operators (OPs) have administrative privileges for Simply Teams.
  # When true (default), operators can manage or delete other teams (/team admin) and bypass team permissions.
  # When false, operators are treated strictly as regular players, allowing OPs to play normally without admin team powers.
  opsAreAdmins = true

[tab_list]
  # Whether to show the team icon in the server player list (TAB).
  showIcon = true

  # Whether to show the team prefix in the server player list (TAB).
  showPrefix = true

  # Whether to show the team suffix in the server player list (TAB).
  showSuffix = true

  # Format string for the team icon in the player list. %s represents the item name.
  iconFormat = "[%s]"
```

---

## Building

Prerequisites: JDK 21.

To build the mod jar:

```bash
./gradlew build
```

The resulting file will be located at `build/libs/simply_teams-1.0.0.jar`.
