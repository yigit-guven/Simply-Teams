# Simply Teams (100% Server-Sided)

A feature-rich, **100% server-sided** Minecraft mod for **NeoForge 1.21.1** that replaces Minecraft's vanilla `/team` command with an intuitive, extensible team management system.

> **Zero Client Requirements**: Clients do **NOT** need the mod installed. Vanilla Minecraft clients, Bedrock (via Geyser), Lunar, Badlion, etc. can all connect and use all features seamlessly!

---

## 🌟 Key Features

1. **100% Server-Sided (No Client Mod Needed)**:
   - Registers no custom blocks, items, entities, or custom menu types.
   - Network channel registration is marked with `event.registrar(MODID).optional()`, so connecting clients are never checked or required to have the mod installed.
   - All UIs, commands, scoreboard tags, and tab list visuals use standard vanilla Minecraft network packets.
2. **Replaces Vanilla `/team` Commands**:
   - The vanilla `/team` command tree is completely unhooked and eradicated from the server dispatcher.
   - Replaced by Simply Teams' custom command tree.
3. **Context-Aware Dynamic Visibility**:
   - Players without a team only see `/team create`, `/team accept`, `/team info`, and `/team list`.
   - Players with a team see team-specific commands (`/team members`, `/team invite`, `/team leave`, `/team chat`, `/team icon`, `/team prefix`, `/team suffix`, `/team color`, `/team rank`, `/team sethome`, `/team home`, `/team friendlyfire`, `/team motd`, etc.).
   - As soon as a team is created, joined, or left, the server updates the client's command tree in real time (`sendCommands(player)`).
4. **Spaced Display Names & Configurable Limits**:
   - Team display names can contain spaces (e.g. `/team create The Diamond Legion`).
   - Maximum character length is configurable in `simply_teams-server.toml` (defaults to `-1` for unlimited).
   - Team ID is permanently and uniquely tied to the founder/leader's username.
5. **Interactive Invitations & Expiration**:
   - `/team invite <player>` sends an interactive, clickable invite in chat with real-time expiration countdown.
   - `/team accept [player|teamId]` validates invite lifetime (defaults to 60s, configurable) before letting players join.
6. **Native 6-Row Chest UI (`/team members`)**:
   - Uses Minecraft's native `MenuType.GENERIC_9x6` (`ChestMenu`), opening seamlessly on vanilla clients.
   - Displays real member skins on player heads for both online and offline members (`ResolvableProfile`).
   - The team leader is always displayed first in slot 0 with the Leader rank.
   - Tooltips show member rank, online/offline status, join date, and founder notes.
   - Includes pagination controls and team statistics summary.
   - **Strictly Read-Only**: Clicks are intercepted on the server so items cannot be taken, moved, or dropped.
7. **Cosmetics & Tab List (Player List) Integration**:
   - **Team Icon (`/team icon`)**: Takes whatever item the leader or authorized member is holding in their main hand and assigns it as the team icon.
   - **Server Tab List ("TAB")**: Formats player names in the TAB overlay:  
     `[Prefix] PlayerName [Icon] [Suffix]`  
     Hovering over the icon in chat or TAB shows the full item tooltip.
   - **Prefix & Suffix**: `/team prefix <tag> <color> <bold>` and `/team suffix <tag> <color> <bold>`.
   - **Team Color**: `/team color <color>` (supports all standard Minecraft colors).
8. **Comprehensive Ranks & Permissions System**:
   - Default ranks: `Leader` (priority 100), `Officer` (priority 50), `Member` (priority 10).
   - Create custom ranks: `/team rank create <id> <displayName> [priority]`.
   - Assign ranks: `/team rank set <player> <rankId>`.
   - Customize appearance: `/team rank color <rankId> <color>` and `/team rank bold <rankId> <true|false>`.
   - Granular permissions: `INVITE`, `KICK`, `CHANGE_ICON`, `CHANGE_COLOR`, `CHANGE_PREFIX`, `CHANGE_SUFFIX`, `MANAGE_RANKS`, `SET_RANK`, `RENAME`, `CHAT`, `TOGGLE_FRIENDLY_FIRE`, `SET_HOME`.
9. **Team Features**:
   - **Friendly Fire Toggle**: `/team friendlyfire [true|false]` (prevents teammates from damaging each other).
   - **Team Chat Mode**: `/team chat` with no args toggles team chat mode, automatically routing normal chat messages to teammates!
   - **Team Home Base**: `/team sethome` and `/team home` for shared team bases.
   - **Team MOTD**: `/team motd <message>` greets members when they log into the server.
   - **Server Directory**: `/team list [page]` for browsing public teams.
10. **World Persistence & Scoreboard Synchronization**:
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
