# Maid PvP with Player

让 [车万女仆 (Touhou Little Maid)](https://modrinth.com/mod/touhou-little-maid) 的女仆可以和玩家战斗。

一个 **Minecraft 1.21.1 / NeoForge** 的附属模组，提供「是否允许攻击玩家」总开关、「是否攻击主人」保护开关，并与女仆自带的攻击列表（敌对 / 中立 / 友好）完整联动。

默认 **不生效**（`attackPlayer = false`），需要手动开启：单人游戏可直接在游戏内「模组 → 配置」界面打开，服务器改 `config/maid_pvp_with_player-server.toml`。

---

## 功能

| 功能 | 说明 |
| --- | --- |
| 允许女仆攻击玩家 | 处于攻击任务（近战 / 弓兵 / 弩兵 / 弹幕等）的女仆会把玩家视为合法目标 |
| 是否攻击主人 | 单独保护主人，关闭时女仆永远不会攻击自己的主人 |
| 自动友好模式 | 女仆攻击列表里没有 `minecraft:player` 这一项时，**自动按「友好」处理**，不会无差别攻击所有玩家 |
| 攻击列表联动 | 遵循女仆 GUI 中对 `minecraft:player` 的「敌对 / 中立 / 友好」设置 |

## 判定优先级

| 顺序 | 条件 | 结果 |
| --- | --- | --- |
| 1 | 目标不是玩家 | 保持车万女仆原版逻辑 |
| 2 | `attackPlayer = false` | 永不攻击玩家 |
| 3 | `respectAttackList = true` 且 列表为「友好」**或未配置** | 永不攻击玩家 |
| 4 | 目标是主人 且 `attackOwner = false` | 不攻击 |
| 5 | 列表为「中立」 | 仅在被挑衅后反击 |
| 6 | 列表为「敌对」，或 `respectAttackList = false` | 攻击 |

「被挑衅」沿用车万女仆对中立生物的定义：

* 该玩家攻击过主人
* 主人攻击过该玩家
* 该玩家攻击过女仆

### 一句话总结：怎样才会真的生效

女仆要攻击某个玩家，必须**同时**满足：

| # | 条件 | 配置位置 |
| --- | --- | --- |
| 1 | `attackPlayer = true` | 全局（服务端配置） |
| 2 | 该女仆的攻击列表里有 `minecraft:player`，且设为「敌对」或「中立」 | **每只女仆各自的 GUI** |
| 3 | 若目标就是主人，还需 `attackOwner = true` | 全局（服务端配置） |

> 未满足第 2 条的女仆，行为与**车万女仆原版完全一致**（原版本身就把玩家排除在攻击目标之外），
> 也就是「没有单独配置 = 不生效」。
>
> 这是刻意设计的**自动友好模式**：装上本模组不会让全服女仆突然围攻所有玩家。
>
> 攻击列表是**逐只女仆独立存储**的数据（挂在 `EntityMaid` 实例上），不是全局配置，
> 所以「有没有配置」是按每只女仆分别判断的，你可以只让某几只女仆参战。
>
> 想跳过第 2 条（不逐只配置）：把 `respectAttackList` 设为 `false`，
> 此时除主人外所有玩家都会被攻击。

## 用法

### 方式一：精细控制（推荐）

先把 `attackPlayer` 改成 `true`（单人游戏可直接在「模组 → 配置」界面里改），并保持 `respectAttackList = true`，然后对**每一只**女仆单独配置：

1. 打开女仆 GUI → 攻击任务 → 配置
2. 在怪物列表输入框填入 `minecraft:player`，点击「添加」
3. 点击列表中该项目，切换为「敌对」（始终攻击）或「中立」（被挑衅后攻击）
4. 不添加或设为「友好」的女仆永远不会攻击玩家

这样你可以精确控制哪几只女仆参与战斗。

### 方式二：全量开启

```toml
attackPlayer = true
respectAttackList = false   # 忽略攻击列表
```

除主人外（`attackOwner = false` 时）所有玩家都会被攻击，无需逐只配置。

### 让女仆攻击主人

额外把 `attackOwner` 设为 `true`。注意女仆主人对女仆造成的伤害会被大幅减免（这是车万女仆本体的保护机制）。

## 配置

配置文件：`config/maid_pvp_with_player-server.toml`（服务端 / 存档级别）

```toml
# 总开关：女仆是否被允许攻击玩家。
# 关闭时女仆保持车万女仆原版行为，永远不会把玩家当作攻击目标。
# 开启后具体攻击哪些玩家由女仆攻击列表决定，见 respectAttackList。
attackPlayer = false

# 女仆是否可以攻击自己的主人（仅在 attackPlayer 为 true 时生效）。
attackOwner = false

# 是否由女仆攻击列表中 minecraft:player 这一项决定该女仆攻击哪些玩家。
# true （推荐）：只有被显式设为「敌对」或「中立」的玩家会被攻击；未配置该项的女仆按「友好」处理。
# false：忽略攻击列表，除主人外（attackOwner 为 false 时）所有玩家都会被攻击。
respectAttackList = true
```

### 修改配置的两种方式

**方式 A：游戏内 GUI**

打开「模组」列表 → 选中 `Maid PvP with Player` → 点击「配置」，即可在界面里直接改动这三个开关（已附带中文名与说明）。

NeoForge 的规则是：**SERVER 类型配置只有在自己本地开世界时可编辑**。
连接他人服务器或他人开启的局域网世界时，该配置项在界面中会被禁用，只能由服主改服务端文件。

**方式 B：直接改文件**

| 环境 | 默认路径 |
| --- | --- |
| 专用服务端 | `<服务端目录>/config/maid_pvp_with_player-server.toml` |
| 单人游戏 | `.minecraft/config/maid_pvp_with_player-server.toml` |
| 只想覆盖某个存档 | 客户端 `.minecraft/saves/<存档名>/serverconfig/`；服务端 `<服务端目录>/world/serverconfig/` |

NeoForge 默认开启了配置文件监听（`disableConfigWatcher = false`），改完保存后会自动重新加载，无需重启游戏。

## 工作原理

车万女仆的目标筛选链是：

```
IAttackTask#findFirstValidAttackTarget(...)
  └─ EntityMaid#canAttack(LivingEntity)
       └─ IAttackTask#canAttack(maid, target)   // 这里硬编码排除了 Player
```

`IAttackTask#canAttack` 是接口默认方法，无法用 Mixin 注入；而 `EntityMaid#canAttack` 是所有目标搜索的唯一入口，因此本项目在它的 `HEAD` 处注入：

| 文件 | 作用 |
| --- | --- |
| `mixin/EntityMaidCanAttackMixin.java` | Mixin 注入点（只含注入器） |
| `rule/MaidAttackRules.java` | 判定逻辑 |
| `compat/MaidAttackList.java` | 反射读取女仆攻击列表（`InitTaskData.ATTACK_LIST` → `AttackListData#attackGroups()`） |
| `config/AttackPlayerConfig.java` | NeoForge `ModConfig`（SERVER 类型） |

两个刻意的设计取舍：

1. **Mixin 用字符串 `targets=` 而不是 `@Mixin(EntityMaid.class)`**
   这样编译期不需要车万女仆的 jar，任何人 clone 下来直接 `gradlew build` 就能通过。
   代价是日志里会有一条 Mixin 的 `should be specified in value` 无害告警。

2. **攻击列表用反射读取**
   车万女仆内部类若改名，只会退化成「忽略攻击列表」并打印一条 warn，而不是让游戏崩溃。

## 环境要求与支持范围

### 支持矩阵

| 组件 | 支持范围 | 判断依据 |
| --- | --- | --- |
| Minecraft | **1.21.1** | 模组 jar 按 MC 版本编译，`minecraft_version_range = [1.21.1,1.21.2)` |
| NeoForge | **21.1.x** | 与 MC 1.21.1 对应的版本线（开发用 21.1.21） |
| 车万女仆 Touhou Little Maid | **>= 1.1.13** | 1.1.13 是「攻击列表」功能落地后，TLM 在 1.21.1 上的首个发布版 |
| Java | **21** | MC 1.21.1 的硬性要求 |

### 为什么 Minecraft 版本不能放宽

* Minecraft 模组 jar 是**针对具体 MC 版本编译**的：类名、方法名、NeoForge API 在不同 MC 版本之间都会变。
* 单纯放宽 `minecraft_version_range` 只会让 NeoForge 允许它在不兼容的版本上加载，
  之后在 Mixin 注入或方法调用时崩溃 —— 比直接拒绝加载更糟。
* 要覆盖其它 MC 版本，唯一正确的做法是**为每个 MC 版本单独构建一个 jar**
  （例如再开一个 1.21.4 的构建目标）。本仓库目前只提供 1.21.1 的构建。

### 为什么车万女仆的下限是 1.1.13

本模组依赖 TLM 的这几个内部结构，在实测的 **1.1.13** 与 **1.5.3** 上完全一致：

| 依赖 | 用途 |
| --- | --- |
| `EntityMaid#canAttack(LivingEntity)` | Mixin 注入点（必须确实是 `EntityMaid` 上的覆写方法，否则注入失败会崩溃） |
| `EntityMaid#getData(TaskDataKey)` / `setData` | 读 / 写女仆的攻击列表 |
| `InitTaskData.ATTACK_LIST`（`TaskDataKey<AttackListData>`） | 攻击列表数据的键 |
| `AttackListData#attackGroups()` → `Map<ResourceLocation, MonsterType>` | 实际的敌对 / 中立 / 友好表 |
| `MonsterType.{FRIENDLY,NEUTRAL,HOSTILE}` | 枚举常量名 |

`AttackListData` 于 2024-10-28 加入 TLM，因此 1.1.13（2024-11-12 发布）是第一个可用的 1.21.1 版本；
更早的 1.1.12 系列尚无攻击列表，装了也无法工作。

### 服务端（必须安装）

1. Minecraft 1.21.1 + NeoForge 21.1.x
2. 车万女仆 Touhou Little Maid
3. **本模组** `maid_pvp_with_player-1.0.0.jar`

jar 放入 `<服务端目录>/mods/`，配置文件在 `<服务端目录>/config/maid_pvp_with_player-server.toml`。

### 客户端

**必须安装**：Minecraft 1.21.1 + NeoForge 21.1.x、车万女仆 Touhou Little Maid
（女仆的实体、模型、音效、GUI 都在车万女仆里，客户端没有它无法正常显示和交互女仆）。

**本模组在客户端是可选的**：

* 女仆的目标判定（AI）运行在**逻辑服务端**，所以服务端装了本模组，逻辑就生效，
  客户端装不装本模组，女仆一样会按配置攻击玩家。
* 本模组不注册任何网络通道 / 注册表数据 / 枚举扩展，因此不会参与 NeoForge 的连接协商，
  客户端缺失它**不会**导致「模组列表不匹配」而连不上服务器。
* 装上本模组只是让两端模组列表一致，并且能在单人游戏的配置界面里看到这几个选项。

### 单人游戏

单人存档里逻辑服务端和客户端在同一个进程内，因此**必须安装本模组**才能生效。

## 构建

```bash
# Windows
gradlew.bat build

# macOS / Linux
./gradlew build
```

产物：`build/libs/maid_pvp_with_player-1.0.0.jar`

开发环境运行（需自行准备车万女仆 jar 放入 `run/client/mods` 或 `run/server/mods`）：

```bash
gradlew.bat runClient
gradlew.bat runServer
```

## 已验证

* `gradlew build` 编译打包通过
* 兼容性边界实测：分别在支持范围**最旧**与**最新**的车万女仆版本上启动开发服务器，
  两次都成功注入且没有任何报错：

| 车万女仆版本 | 注入结果 | 服务器启动 |
| --- | --- | --- |
| **1.1.13**（最旧支持版本） | `Mixing EntityMaidCanAttackMixin from maid_pvp_with_player.mixins.json into ...EntityMaid` | `Done (5.894s)!` |
| **1.5.3**（当前最新） | 同上 | `Done (4.794s)!` |

  1.5.3 的完整日志片段：

```
[mixin/]: Selecting config maid_pvp_with_player.mixins.json
[mixin/]: Mixing EntityMaidCanAttackMixin from maid_pvp_with_player.mixins.json
          into com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid
[DedicatedServer]: Done (4.794s)! For help, type "help"
```

* 在真实 `EntityMaid` 实例上验证了攻击列表读取与判定逻辑：

| 用例 | 读到的攻击列表 | 判定结果 |
| --- | --- | --- |
| 列表未配置 | `null` | 不攻击（自动友好） |
| 列表 = 敌对 | `HOSTILE` | 攻击 |
| 列表 = 中立 | `NEUTRAL` | 不攻击（未被挑衅） |
| 列表 = 友好 | `FRIENDLY` | 不攻击 |

## 版权与许可

* 本模组代码为原创，采用 **MIT** 许可，见 [LICENSE](LICENSE)。
* [车万女仆 (Touhou Little Maid)](https://github.com/TartaricAcid/TouhouLittleMaid) 由 TartaricAcid 及贡献者开发，其**代码**采用 MIT、**素材**采用 CC BY-NC-SA 4.0。
* 本模组仅通过 Mixin 在**运行时**修改女仆实体的目标判定方法，**不包含、不打包、不再分发**车万女仆的任何代码、贴图、模型、音效或数据文件。
* 本模组不包含 Minecraft 的任何素材，也不包含 Mojang / Microsoft 的资产。
* 本模组是**非官方**附属模组，与车万女仆作者及团队**无隶属、赞助或背书关系**。
* 为避免混淆，本模组名称未使用 `Touhou Little Maid` 作为名称主体。

## English

**Maid PvP with Player** is a Minecraft 1.21.1 / NeoForge addon for
[Touhou Little Maid](https://modrinth.com/mod/touhou-little-maid) that lets maids fight players.

Requirements: Minecraft **1.21.1**, NeoForge **21.1.x**, Touhou Little Maid **1.1.13+**, Java 21.
The jar is compiled for 1.21.1 only - other Minecraft versions need their own build, since mod jars
cannot be shared across Minecraft versions.

It adds a master toggle (`attackPlayer`, off by default), an owner protection toggle
(`attackOwner`), and integrates with the maid attack list: a maid whose attack list has **no**
`minecraft:player` entry is treated as **friendly** and will not attack players. Only maids that
explicitly list `minecraft:player` as *hostile* (always attack) or *neutral* (fight back when
provoked) will engage players.

In short, all three conditions must hold: `attackPlayer = true`, the maid has `minecraft:player`
in its own attack list set to *hostile* or *neutral*, and for the owner `attackOwner = true`.
A maid without that entry behaves exactly like vanilla Touhou Little Maid.

Set `respectAttackList = false` to ignore the attack list and let every maid attack every player
(except the owner while `attackOwner = false`).

Both toggles default to `false`, so nothing changes until you turn them on. In singleplayer you can
edit them from the in-game **Mods → Config** screen; on a dedicated server edit
`config/maid_pvp_with_player-server.toml` (server configs cannot be edited from the screen while
connected to someone else's server).

The maid AI runs on the **logical server**, so a client can join without this mod installed and
maids will still attack players. Touhou Little Maid itself is required on both sides. This mod
registers no network channels, registry data or enum extensions, so it never causes a NeoForge
connection mismatch.

Licensed under MIT. This is an unofficial addon: it modifies the maid entity at runtime via Mixin
and does not contain or redistribute any code or assets from Touhou Little Maid.
