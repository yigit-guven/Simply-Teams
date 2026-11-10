# Simply Teams

[![GitHub](https://img.shields.io/badge/GitHub-Simply--Teams-blue?logo=github)](https://github.com/yigit-guven/Simply-Teams)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1-orange)](https://neoforged.net/)
[![Side](https://img.shields.io/badge/Side-100%25%20Server--Sided-brightgreen)](#-zero-client-requirements)

A high-performance, **100% server-sided** Minecraft mod for **NeoForge 1.21.1** that replaces Minecraft's vanilla `/team` command with an intuitive, extensible team management system.

* **Author:** Yigit_Guven
* **Repository:** [https://github.com/yigit-guven/Simply-Teams](https://github.com/yigit-guven/Simply-Teams)
* **Issues & Bug Reports:** [https://github.com/yigit-guven/Simply-Teams/issues](https://github.com/yigit-guven/Simply-Teams/issues)

---

## 🛡️ Zero Client Requirements

Simply Teams runs **purely on the server**.
* Clients do **NOT** need the mod installed.
* Vanilla Minecraft clients, Lunar Client, Badlion, Bedrock Edition (via Geyser), and all custom clients can connect and use every feature without warnings or handshake mismatches.
* Network channels are marked as optional (`event.registrar(MODID).optional()`).
* All user interfaces use native Minecraft packets (`MenuType.GENERIC_9x6`), standard chat components, and scoreboard synchronizations.

---

## 🌟 Key Features

1. **Replaces Vanilla `/team` Commands**:
   - The vanilla `/team` command tree is completely unhooked and eradicated from the server dispatcher.
   - Replaced by Simply Teams' custom command tree.
2. **Context-Aware Dynamic Visibility**:
   - Players without a team only see `/team create`, `/team accept`, `/team info`, and `/team list`.
   - Players with a team see team-specific commands (`/team members`, `/team invite`, `/team leave`, `/team chat`, `/team icon`, `/team prefix`, `/team suffix`, `/team color`, `/team rank`, `/team sethome`, `/team home`, `/team friendlyfire`, `/team motd`, etc.).
   - As soon as a team is created, joined, or left, the server updates the client's command suggestions in real time (`sendCommands(player)`).
3. **Spaced Display Names & Configurable Limits**:
   - Team display names can contain spaces (e.g. `/team create The Dragon Slayers`).
   - Maximum character length is configurable in `simply_teams-server.toml` (defaults to `-1` for unlimited).
   - Team ID is permanently and uniquely tied to the founder/leader's username.
4. **Interactive Invitations & Expiration**:
   - `/team invite <player>` sends an interactive, clickable invite in chat with a countdown timer.
   - `/team accept [player|teamId]` validates invite lifetime (defaults to 60s, configurable) before letting players join.
5. **Native 6-Row Chest UI (`/team members`)**:
   - Uses Minecraft's native `MenuType.GENERIC_9x6` (`ChestMenu`), opening natively on any client.
   - Displays real member skins on player heads for both online and offline members (`ResolvableProfile`).
   - The team leader is always displayed first in slot 0 with the Leader rank.
   - Tooltips show member rank, online/offline status, join date, and founder notes.
   - Includes pagination controls and team statistics summary.
   - **Strictly Read-Only**: Server cancels all item pickups, drops, or moves.
6. **Cosmetics & Tab List (Player List) Integration**:
   - **Team Icon (`/team icon`)**: Takes whatever item the leader or authorized member is holding in their main hand and assigns it as the team icon.
   - **Server Tab List ("TAB")**: Formats player names in the TAB overlay:  
     `[Prefix] PlayerName [Icon] [Suffix]`  
     Hovering over the icon in chat or TAB displays the full item tooltip.
   - **Prefix & Suffix**: `/team prefix <tag> <color> <bold>` and `/team suffix <tag> <color> <bold>`.
   - **Team Color**: `/team color <color>` (supports all standard Minecraft colors).
7. **Comprehensive Ranks & Permissions System**:
   - Default ranks: `Leader` (priority 100), `Officer` (priority 50), `Member` (priority 10).
   - Create custom ranks: `/team rank create <id> <displayName> [priority]`.
   - Assign ranks: `/team rank set <player> <rankId>`.
   - Customize appearance: `/team rank color <rankId> <color>` and `/team rank bold <rankId> <true|false>`.
   - Granular permissions: `INVITE`, `KICK`, `CHANGE_ICON`, `CHANGE_COLOR`, `CHANGE_PREFIX`, `CHANGE_SUFFIX`, `MANAGE_RANKS`, `SET_RANK`, `RENAME`, `CHAT`, `TOGGLE_FRIENDLY_FIRE`, `SET_HOME`.
8. **Team Features**:
   - **Friendly Fire Toggle**: `/team friendlyfire [true|false]` (or `/team ff`). Automatically cancels damage between teammates when friendly fire is disabled.
   - **Team Chat Mode**: `/team chat` with no args toggles team chat mode, automatically routing normal chat messages to teammates!
   - **Team Home Base**: `/team sethome` and `/team home` for shared team bases.
   - **Team MOTD**: `/team motd <message>` greets members when they log into the server.
   - **Server Directory**: `/team list [page]` for browsing public teams.
9. **World Persistence & Scoreboard Synchronization**:
   - Data is saved directly to world storage (`TeamSavedData`) and persists across server reboots.
   - Synchronizes with Minecraft's vanilla `Scoreboard` so player nametags and glowing effects respect team colors and formatting.

---

## 📜 Command Reference

### For Players Without a Team:
| Command | Description |
|---|---|
| `/team create <display name>` | Create a team with your username as unique ID |
| `/team accept [player or team]` | Accept a pending team invitation |
| `/team info [teamId]` | View information about a team |
| `/team list [page]` | Browse all server teams |

### For Players In a Team:
| Command | Permission Required | Description |
|---|---|---|
| `/team members` | Any member | Open the chest UI showing all members and ranks |
| `/team chat` | `CHAT` | Toggle Team Chat mode on/off |
| `/team chat <message>` | `CHAT` | Send a private message to team members |
| `/team c <message>` | `CHAT` | Alias for `/team chat <message>` |
| `/team invite <player>` | `INVITE` | Invite an online player |
| `/team kick <player>` | `KICK` | Kick a member with lower rank |
| `/team leave` | Any member | Leave the team (leader must transfer or disband) |
| `/team icon` | `CHANGE_ICON` | Set team icon to held main-hand item |
| `/team icon clear` | `CHANGE_ICON` | Clear the team icon |
| `/team color <color>` | `CHANGE_COLOR` | Change team color |
| `/team prefix <tag> <color> <bold>` | `CHANGE_PREFIX` | Set custom prefix |
| `/team prefix clear` | `CHANGE_PREFIX` | Clear prefix |
| `/team suffix <tag> <color> <bold>` | `CHANGE_SUFFIX` | Set custom suffix |
| `/team suffix clear` | `CHANGE_SUFFIX` | Clear suffix |
| `/team friendlyfire [true\|false]` | `TOGGLE_FRIENDLY_FIRE` | Toggle or set friendly fire |
| `/team motd <message>` | `RENAME` | Set team message of the day |
| `/team sethome` | `SET_HOME` | Set team home base at current location |
| `/team home` | Any member | Teleport to team home base |
| `/team delhome` | `SET_HOME` | Delete team home base |
| `/team rename <new name>` | `RENAME` | Change team display name |
| `/team transfer <player>` | Leader only | Transfer team leadership |
| `/team disband` | Leader only | Disband team (with confirmation prompt) |

### Rank Management:
| Command | Description |
|---|---|
| `/team rank list` | List all ranks, priorities, colors, and permissions |
| `/team rank create <id> <name> [priority]` | Create a new custom rank |
| `/team rank delete <id>` | Delete a custom rank |
| `/team rank set <player> <rankId>` | Assign a rank to a player |
| `/team rank color <rankId> <color>` | Change the display color of a rank |
| `/team rank bold <rankId> <true\|false>` | Toggle bold formatting for a rank |
| `/team rank permission <rankId> add <perm>` | Grant a permission to a rank |
| `/team rank permission <rankId> remove <perm>` | Revoke a permission from a rank |

---

## ⚙️ Server Configuration (`simply_teams-server.toml`)

Stored in the server `world/serverconfig/simply_teams-server.toml` or `config/` directory:

```toml
[teams]
  # Maximum character length for team display names. Use -1 for unlimited length.
  maxDisplayNameLength = -1

  # Whether teams are allowed to have custom colors.
  allowTeamColor = true

  # Whether teams are allowed to have custom prefixes.
  allowTeamPrefix = true

  # Whether teams are allowed to have custom suffixes.
  allowTeamSuffix = true

  # Maximum character length for team prefixes. Use -1 for unlimited.
  maxPrefixLength = 16

  # Maximum character length for team suffixes. Use -1 for unlimited.
  maxSuffixLength = 16

  # How many seconds a team invite remains valid before expiring.
  inviteTimeoutSeconds = 60

  # Whether teams can set and teleport to a shared team home base.
  allowTeamHome = true

[tab_list]
  # Whether to show the team icon in the server player list (TAB).
  showIcon = true

  # Whether to show the team prefix in the server player list (TAB).
  showPrefix = true

  # Whether to show the team suffix in the server player list (TAB).
  showSuffix = true

  # Format for the team icon badge in the player list. %s is replaced by the item name.
  iconFormat = "[%s]"
```

---

## 📦 Building From Source

To build the mod JAR from source:

```bash
./gradlew build
```

The output JAR will be generated in `build/libs/simply_teams-1.0.0.jar`.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
Author: **Yigit_Guven** (2026).
Repository: [https://github.com/yigit-guven/Simply-Teams](https://github.com/yigit-guven/Simply-Teams)
