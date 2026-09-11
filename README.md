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

| 开关 | 默认 | 作用 |
| --- | --- | --- |
| `attackPlayer` | `false` | 总开关：允许女仆攻击玩家 |
| `attackOwner` | `false` | 允许女仆攻击自己的主人 |
| `respectAttackList` | `true` | 由女仆攻击列表决定打谁（未配置 = 友好，不打玩家） |

## 判定优先级

| 顺序 | 条件 | 结果 |
| --- | --- | --- |
| 1 | 目标不是玩家 | 保持车万女仆原版逻辑 |
| 2 | `attackPlayer = false` | 永不攻击玩家 |
| 3 | `respectAttackList = true` 且 列表为「友好」或未配置 | 永不攻击玩家 |
| 4 | 目标是主人 且 `attackOwner = false` | 不攻击 |
| 5 | 列表为「中立」 | 仅在被挑衅后反击 |
| 6 | 列表为「敌对」，或 `respectAttackList = false` | 攻击 |

「被挑衅」= 该玩家攻击过主人 / 主人攻击过该玩家 / 该玩家攻击过女仆。

**一句话**：女仆要攻击某个玩家，必须同时满足 —— `attackPlayer = true`；该女仆攻击列表里有
`minecraft:player` 且设为「敌对」或「中立」；若目标就是主人，还需 `attackOwner = true`。
没有单独配置的女仆，行为与车万女仆原版完全一致。

## 用法

1. 把 `maid_pvp_with_player-<版本>.jar` 和车万女仆一起放进 `mods/`
2. 开启总开关：改 `config/maid_pvp_with_player-server.toml`，把 `attackPlayer` 改成 `true`
   * 单人游戏：该文件位于 `.minecraft/config/`，也可以在「模组」→「Maid PvP with Player」→「配置」界面里改
   * 服务器：`<服务端目录>/config/`
3. 指定哪只女仆参战：女仆 GUI → 攻击任务 → 配置 → 怪物列表输入 `minecraft:player` → 添加 → 点它切成「敌对」（见到就打）或「中立」（被挑衅才还手）

不想逐只配置：把 `respectAttackList` 改成 `false`，除主人外所有玩家都会被攻击。

## 配置

| 环境 | 配置文件 |
| --- | --- |
| 专用服务端 | `<服务端目录>/config/maid_pvp_with_player-server.toml` |
| 单人游戏 | `.minecraft/config/maid_pvp_with_player-server.toml` |
| 只为某个存档覆盖 | 客户端 `.minecraft/saves/<存档名>/serverconfig/`；服务端 `<服务端目录>/world/serverconfig/` |

单人游戏也可在「模组 → 配置」界面里直接改；连他人服务器时该界面不可编辑，需服主改文件。

## 实现

在 `EntityMaid#canAttack` 处注入，覆盖车万女仆对玩家的硬编码排除；攻击列表通过反射读取，
因此编译期不需要车万女仆依赖。

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

Three toggles, all inactive by default: `attackPlayer` (master switch), `attackOwner` (protect the
owner) and `respectAttackList` (let each maid's own attack list decide; no `minecraft:player` entry
means friendly, so the maid never attacks players).

To make a maid fight, all of these must hold: `attackPlayer = true`, the maid has
`minecraft:player` in its attack list set to *hostile* or *neutral*, and for the owner
`attackOwner = true`. Maids without that entry behave exactly like vanilla Touhou Little Maid.

Touhou Little Maid is required on both sides; this mod is optional on the client because the maid
AI runs on the server.

Licensed under MIT. Unofficial addon: it modifies the maid entity at runtime via Mixin and does not
contain or redistribute any code or assets from Touhou Little Maid.
