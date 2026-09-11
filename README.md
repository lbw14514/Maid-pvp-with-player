# Maid PvP with Player

让 [车万女仆 (Touhou Little Maid)](https://modrinth.com/mod/touhou-little-maid) 的女仆可以和玩家战斗。

一个 **Minecraft 1.21.1 / NeoForge** 的附属模组，提供「是否允许攻击玩家」总开关、「是否攻击主人」保护开关，并与女仆自带的攻击列表（敌对 / 中立 / 友好）完整联动。

默认 **不生效**（`attackPlayer = false`），需要手动开启。

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

## 用法

### 方式一：精细控制（推荐）

保持默认 `attackPlayer = true`、`respectAttackList = true`，然后对**每一只**女仆单独配置：

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

> 单人游戏该文件位于 `saves/<存档名>/serverconfig/`，专用服务器位于 `config/`。

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

## 环境要求

| 依赖 | 版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.x（开发用 21.1.21） |
| 车万女仆 Touhou Little Maid | `>= 1.5.0`（开发时用 1.5.3-neoforge+mc1.21.1 验证） |
| Java | 21 |

仅影响服务端逻辑，客户端不安装也能进服（仍建议两端都装，便于模组列表一致）。

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
* 搭配真实的 **Touhou Little Maid 1.5.3-neoforge+mc1.21.1** 启动开发服务器成功，Mixin 注入确认生效：

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

It adds a master toggle (`attackPlayer`, off by default), an owner protection toggle
(`attackOwner`), and integrates with the maid attack list: a maid whose attack list has **no**
`minecraft:player` entry is treated as **friendly** and will not attack players. Only maids that
explicitly list `minecraft:player` as *hostile* (always attack) or *neutral* (fight back when
provoked) will engage players.

Set `respectAttackList = false` to ignore the attack list and let every maid attack every player
(except the owner while `attackOwner = false`).

Licensed under MIT. This is an unofficial addon: it modifies the maid entity at runtime via Mixin
and does not contain or redistribute any code or assets from Touhou Little Maid.
