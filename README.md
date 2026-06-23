# PriceKeeper

> 本地优先、隐私友好的个人物价记录与比价 Android 应用。

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose_BOM-2024.06.00-blue.svg)](https://developer.android.com/jetpack/compose)
[![Min API](https://img.shields.io/badge/minSdk-26-brightgreen.svg)](https://developer.android.com/about/versions/oreo)
[![Gradle](https://img.shields.io/badge/Gradle-8.7-02303A.svg)](https://gradle.org)

PriceKeeper 用来记录日常商品价格、追踪价格变化、比较不同商店价格，并通过端侧 OCR 辅助录入购物小票。项目采用 Clean Architecture + MVVM，数据保存在本机 Room 数据库中。

## 当前能力

| 模块 | 能力 |
|---|---|
| 商品价格 | 商品列表、分类筛选、搜索、价格详情、趋势折线图、商店价格对比 |
| 商店管理 | 商店列表、商店详情、区域信息、已追踪商品价格范围 |
| 价格录入 | 手动录入商品、价格、分类、商店与购买日期 |
| 小票识别 | 相机/相册取图、图片压缩、ML Kit 中文 OCR、多策略小票解析、识别结果编辑 |
| 数据管理 | `.mypd` 导出/导入、JSON + GZIP、导入冲突策略、数据校验 |
| 我的页面 | 商品/商店/消费/小票统计、数据导入导出、分类管理、关于页面 |

小票识别流水线：

```text
相机/相册
  -> ImageCompressor
  -> ML Kit OCR
  -> ParserStrategySelector
      -> DefaultParser
      -> YonghuiParser
      -> HemaParser
  -> 可编辑识别结果
  -> Repository
  -> Room
```

## 技术栈

| 分类 | 技术 |
|---|---|
| 语言与构建 | Kotlin 1.9.24, Gradle 8.7, AGP 8.4.0, KSP |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| 架构 | Clean Architecture, MVVM, StateFlow, UseCase |
| 依赖注入 | Hilt |
| 本地数据 | Room, DataStore Preferences |
| 异步 | Kotlin Coroutines, Flow |
| OCR 与图片 | Google ML Kit Text Recognition Chinese, CameraX, Coil |
| 序列化 | kotlinx.serialization JSON |
| 测试 | JUnit, MockK, Turbine, Room Testing, Compose UI Test |

## 项目结构

```text
.
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   └── java/com/pricekeeper/app/
│       │       ├── core/          # 导航、主题、通用 UI 基础
│       │       ├── data/          # Room、DAO、Repository 实现、OCR、Parser、导入导出
│       │       ├── domain/        # 领域模型、Repository 接口、UseCase
│       │       └── feature/       # Compose 页面、ViewModel、UiState
│       ├── test/                  # JVM 单元测试
│       └── androidTest/           # Android/Compose UI 测试
├── config/detekt/                 # detekt 配置文件，当前尚未接入 Gradle 任务
├── docs/                          # 产品、技术设计、模块说明、API 示例、变更记录
├── plans/                         # 分阶段实现计划
└── gradle/libs.versions.toml      # 依赖版本目录
```

核心包说明：

| 包 | 说明 |
|---|---|
| `core/navigation` | 路由常量、底部导航、`NavHost` |
| `core/ui/theme` | Material 主题、颜色、字体、主题偏好 |
| `data/local` | Room Entity、DAO、Database |
| `data/repository` | Repository 实现，封装 DAO 访问 |
| `data/ocr` | OCR 接口、ML Kit 实现、图片压缩 |
| `data/parser` | 小票文本解析策略 |
| `data/export` | `.mypd` 导入导出与校验 |
| `domain` | 业务模型、Repository 接口、UseCase |
| `feature` | Home、Goods、Store、Manual、Receipt、Profile 等页面 |

## 架构约束

```text
UI Event -> ViewModel -> UseCase -> Repository Interface -> Repository Impl -> DAO -> Room
             |             |                 |
             v             v                 v
          UiState       Domain Model      Local Entity
```

- Room 是唯一持久化数据源。
- Repository 是唯一数据入口，ViewModel 不直接访问 DAO。
- 依赖通过 Hilt 构造函数注入。
- 读操作优先使用 `Flow` 暴露响应式数据。
- 写操作使用 `suspend` 函数。
- Compose 页面遵循单向数据流：事件上行，状态下行。
- 修改数据库 schema 时，需要同步更新版本号、Migration skeleton 与迁移说明。

## 快速开始

环境要求：

- Android Studio Hedgehog 或更新版本
- JDK 17
- Android SDK 34
- Android 8.0/API 26 或以上设备/模拟器

常用命令：

```bash
# 编译 Debug APK
./gradlew assembleDebug

# 安装到已连接设备/模拟器
./gradlew installDebug

# 运行 JVM 单元测试
./gradlew testDebugUnitTest

# 运行 Android/Compose UI 测试，需要已连接设备/模拟器
./gradlew connectedAndroidTest

# 生成 Release APK，当前 release 开启 R8 混淆
./gradlew assembleRelease
```

当前 CI 执行：

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

注意：`config/detekt/detekt.yml` 和版本目录中的 `ktlint` 版本已经预留，但当前 `build.gradle.kts` 尚未应用 detekt/ktlint 插件，因此 `./gradlew detekt`、`./gradlew ktlintCheck` 还不是可用任务。

## 测试

| 类型 | 路径 | 示例 |
|---|---|---|
| UseCase 单元测试 | `app/src/test/java/.../domain/usecase` | `AddManualPriceRecordUseCaseTest` |
| Repository 单元测试 | `app/src/test/java/.../data/repository` | `GoodsRepositoryImplTest` |
| ViewModel 单元测试 | `app/src/test/java/.../feature` | `ManualEntryViewModelTest` |
| Compose UI 测试 | `app/src/androidTest/java/...` | `HomeScreenTest` |
| 测试工具 | `app/src/test/java/.../helpers` | `MainDispatcherRule` |

运行单个测试类：

```bash
./gradlew testDebugUnitTest --tests "com.pricekeeper.app.domain.usecase.AddManualPriceRecordUseCaseTest"
```

## 数据库

当前数据库版本为 v1，定义在 `PriceKeeperDatabase`。

| 表 | 说明 |
|---|---|
| `goods` | 商品，商品名唯一，包含分类 |
| `store` | 商店，商店名唯一，包含区域、地址、备注、评分等信息 |
| `price_records` | 商品价格记录，关联商品与商店 |
| `receipts` | 小票记录，关联商店，可为空 |

Schema 变更建议流程：

1. 修改 Entity 与 DAO。
2. 提升 `PriceKeeperDatabase` 版本号。
3. 添加 Room `Migration` skeleton。
4. 更新导入导出数据模型或兼容逻辑。
5. 增加迁移测试或 Repository 测试。

## 开发入口

| 目标 | 起点 |
|---|---|
| 新增页面 | `feature/<name>` 新建 Screen/ViewModel/UiState，然后在 `AppNavGraph` 注册路由 |
| 新增业务逻辑 | `domain/usecase` 新建 UseCase，并通过 Repository 接口访问数据 |
| 新增查询 | 先扩展 Repository 接口，再在 DAO 与 Repository 实现中落地 |
| 新增数据表 | 添加 Entity/DAO，更新 `PriceKeeperDatabase`，补 Migration |
| 修改 OCR | `data/ocr` |
| 修改小票解析 | `data/parser` |
| 修改导入导出 | `data/export` |
| 修改主题 | `core/ui/theme` |

## 文档索引

| 文档 | 内容 |
|---|---|
| [AGENTS.md](AGENTS.md) | Codex/Agent 协作约束 |
| [Claude.md](Claude.md) | Claude 协作约束 |
| [docs/产品需求文档.md](docs/产品需求文档.md) | 产品需求 |
| [docs/技术设计文档.md](docs/技术设计文档.md) | 技术设计、架构、SQL 与模块说明 |
| [docs/MODULE_README.md](docs/MODULE_README.md) | 模块结构与开发指南 |
| [docs/API_USAGE_EXAMPLES.md](docs/API_USAGE_EXAMPLES.md) | Repository、UseCase、OCR 使用示例 |
| [docs/CHANGELOG.md](docs/CHANGELOG.md) | 版本变更记录 |
| [plans/phase1_MVP.md](plans/phase1_MVP.md) | Phase 1 MVP 计划 |
| [plans/phase2_features.md](plans/phase2_features.md) | Phase 2 功能计划 |
| [plans/phase3_optimization.md](plans/phase3_optimization.md) | Phase 3 优化计划 |

## 状态说明

项目当前处于早期 Android 应用阶段，主要能力已经按本地优先架构铺开。README 中的命令以当前 Gradle 配置为准；如果后续接入 detekt、ktlint、Room schema 导出目录或发布流程，请同步更新本文件。
