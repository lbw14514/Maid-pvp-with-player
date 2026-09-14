# Maid PvP with Player

让 [车万女仆 (Touhou Little Maid)](https://modrinth.com/mod/touhou-little-maid) 的女仆可以和玩家战斗。

默认不生效，需要手动开启并指定让哪只女仆参战。

## 环境要求

| 组件 | 版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.0+ |
| 车万女仆 Touhou Little Maid | 1.1.13+ |
| Java | 21 |

* **服务端**：以上全部 + 本模组 jar
* **客户端**：Minecraft / NeoForge / 车万女仆必需，本模组可选（女仆 AI 跑在服务端，客户端不装本模组时逻辑依然生效）

## 功能

三条开关有两个入口，默认全部不生效：

| 入口 | 生效时机 | 适合 |
| --- | --- | --- |
| 配置文件 | 服务端启动时读入；游戏里 `/gamerule` 改的值会回写进来 | 游戏没开的时候设定，或者模组包预设 |
| `/gamerule` | 立即生效，并回写配置文件 | 游戏内随时调整 |

| 配置文件项 | 游戏规则 | 默认 | 作用 |
| --- | --- | --- | --- |
| `attackPlayer` | `maidPvpAttackPlayer` | `false` | 总开关：允许女仆攻击玩家 |
| `attackOwner` | `maidPvpAttackOwner` | `false` | 允许女仆攻击自己的主人 |
| `respectAttackList` | `maidPvpRespectAttackList` | `true` | 由女仆攻击列表决定打谁（未配置 = 友好，不打玩家） |

## 判定优先级

| 顺序 | 条件 | 结果 |
| --- | --- | --- |
| 1 | 目标不是玩家 | 保持车万女仆原版逻辑 |
| 2 | `maidPvpAttackPlayer = false` | 永不攻击玩家 |
| 3 | `maidPvpRespectAttackList = true` 且 列表为「友好」或未配置 | 永不攻击玩家 |
| 4 | 目标是主人 且 `maidPvpAttackOwner = false` | 不攻击 |
| 5 | 列表为「中立」 | 仅在被挑衅后反击 |
| 6 | 列表为「敌对」，或 `maidPvpRespectAttackList = false` | 攻击 |

「被挑衅」= 该玩家攻击过主人 / 主人攻击过该玩家 / 该玩家攻击过女仆。

**一句话**：女仆要攻击某个玩家，必须同时满足 —— `maidPvpAttackPlayer` 为 true；该女仆攻击列表里有
`minecraft:player` 且设为「敌对」或「中立」；若目标就是主人，还需 `maidPvpAttackOwner` 为 true。
没有单独配置的女仆，行为与车万女仆原版完全一致。

## 用法

1. 把 `maid_pvp_with_player-<版本>.jar` 和车万女仆一起放进 `mods/`
2. 开启总开关：把配置文件里的 `attackPlayer` 改成 `true`（游戏没开也能改），或者进世界后执行
   `/gamerule maidPvpAttackPlayer true`（需要 OP；单人需在创建世界时开作弊）
   * 创建世界时也可以在「游戏规则」界面里勾选，这三条规则归在「生物」分类下
3. 指定哪只女仆参战：女仆 GUI → 攻击任务 → 配置 → 怪物列表输入 `minecraft:player` → 添加 → 点它切成「敌对」（见到就打）或「中立」（被挑衅才还手）

不想逐只配置：把 `maidPvpRespectAttackList` 改成 `false`，除主人外所有玩家都会被攻击。

## 配置与游戏规则

**两个方向都会同步，两边始终保持一致：**

* **配置文件 → 游戏规则**：服务端启动时，以及 NeoForge 重载这份配置时（改文件，或在「模组 → 配置」
  界面里保存，都会触发），三个值会被写进对应的游戏规则
* **游戏规则 → 配置文件**：游戏里用 `/gamerule` 改值会立刻写回配置文件

所以游戏没开的时候改文件、游戏开着的时候敲命令，改哪个都会被记下来，重启后依然生效。回写走 NeoForge
的配置系统，文件里的注释会保留。

### 配置文件

| 环境 | 路径 |
| --- | --- |
| 专用服务端 | `<服务端目录>/config/maid_pvp_with_player-server.toml` |
| 单人游戏 | `.minecraft/config/maid_pvp_with_player-server.toml` |
| 只为某个存档覆盖 | 客户端 `.minecraft/saves/<存档名>/serverconfig/`；服务端 `<服务端目录>/world/serverconfig/` |

单人游戏也可以在「模组 → Maid PvP with Player → 配置」界面里改；连他人服务器时该界面不可编辑，需服主改文件。

### 游戏规则

```mcfunction
/gamerule maidPvpAttackPlayer true
/gamerule maidPvpAttackOwner true
/gamerule maidPvpRespectAttackList false
```

* 修改需要 OP：单人需在创建世界时开启作弊，服务器需管理员权限
* 创建世界时也能在「游戏规则」界面里改，规则归在「生物」分类下，中文界面显示为「女仆 PvP：…」
* 游戏规则存在存档里（`level.dat`），每个世界 / 服务器各有一套
* 客户端不装本模组也能正常游戏，只是「游戏规则」界面看不到这三条的说明文字，`/gamerule` 不受影响

## 实现

在 `EntityMaid#canAttack` 处注入，覆盖车万女仆对玩家的硬编码排除；攻击列表通过反射读取，
因此编译期不需要车万女仆依赖。

三条开关通过 `GameRules.register` 注册成游戏规则（原版该方法为 private，NeoForge 已开放为
public），注入点从女仆所在的 `Level` 读取规则值。

配置文件与游戏规则双向同步：服务端启动时、以及每次配置重载时，把文件里的值写进规则；规则被
`/gamerule` 改动时，把新值写回文件（`ModConfigSpec#save` 会重新触发一次重载，此时规则里的值已经和文件
一致，同步会直接跳过，不会自激）。同步期间挂了一个标志，避免从文件推过去的值又被回写一遍、白白重写一次
文件。另外，从文件监听线程来的重载会先跳回服务端线程再动游戏规则。

## 构建

需要 JDK 21。

```bash
gradlew.bat build      # Windows
./gradlew build        # macOS / Linux
```

产物：`build/libs/maid_pvp_with_player-1.0.0.jar`

开发环境运行需自备车万女仆，放入 `run/client/mods` 或 `run/server/mods`：

```bash
gradlew.bat runClient
gradlew.bat runServer
```

## 已验证

* `gradlew build` 编译打包通过
* 车万女仆 **1.1.13** 与 **1.5.3** 两个版本均实测通过：Mixin 注入成功、服务器正常启动
* 攻击列表读取与「敌对 / 中立 / 友好」判定逻辑已在真实女仆实体上逐条验证

## 版权与许可

* 本模组代码原创，采用 **MIT** 许可，见 [LICENSE](LICENSE)。
* 非官方附属模组，与车万女仆作者及团队无隶属、赞助或背书关系。
* 仅通过 Mixin 在运行时修改女仆的目标判定，不包含、不打包、不再分发车万女仆的任何代码或素材。

## English

**Maid PvP with Player** is a Minecraft 1.21.1 / NeoForge addon for
[Touhou Little Maid](https://modrinth.com/mod/touhou-little-maid) that lets maids fight players.

Requirements: Minecraft **1.21.1**, NeoForge **21.1.0+**, Touhou Little Maid **1.1.13+**, Java 21.

Three toggles, all inactive by default, each of them available in two places that are kept in sync:
a config file and a game rule. The config file is read into the game rules on every server start
and on every config reload, so it can be prepared while the game is closed (editing it while the
game runs works too). The game rules are what the decision logic reads, and a `/gamerule` change is
written straight back into the config file. Either way of changing a switch therefore survives a
restart, and the two never disagree. The write back goes through NeoForge's config system, which
keeps the comments in the file.

| config file | game rule | default |
| --- | --- | --- |
| `attackPlayer` | `maidPvpAttackPlayer` | `false` |
| `attackOwner` | `maidPvpAttackOwner` | `false` |
| `respectAttackList` | `maidPvpRespectAttackList` | `true` |

```mcfunction
/gamerule maidPvpAttackPlayer true
```

The config file is `config/maid_pvp_with_player-server.toml` on a dedicated server and
`.minecraft/config/maid_pvp_with_player-server.toml` in singleplayer; a `serverconfig/` directory
inside a world overrides it for that world only. Changing a game rule needs OP, so in singleplayer
enable cheats when creating the world — the same rules can also be ticked in the "Game Rules"
screen at world creation (they sit in the "Mobs" category). A client without this mod still works,
it just does not see the rule descriptions; `/gamerule` is unaffected.

To make a maid fight, all of these must hold: `attackPlayer` (or `maidPvpAttackPlayer`) is true, the
maid has `minecraft:player` in its attack list set to *hostile* or *neutral*, and for the owner
`attackOwner` (or `maidPvpAttackOwner`) is true. Maids without that entry behave exactly like
vanilla Touhou Little Maid.

Touhou Little Maid is required on both sides; this mod is optional on the client because the maid
AI runs on the server.

Licensed under MIT. Unofficial addon: it modifies the maid entity at runtime via Mixin and does not
contain or redistribute any code or assets from Touhou Little Maid.
