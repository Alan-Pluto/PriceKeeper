# PriceKeeper

> 本地优先、隐私友好的个人物价记录与比价 Android 应用。

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose_BOM-2024.06.00-blue.svg)](https://developer.android.com/jetpack/compose)
[![Min API](https://img.shields.io/badge/minSdk-26-brightgreen.svg)](https://developer.android.com/about/versions/oreo)
[![Gradle](https://img.shields.io/badge/Gradle-8.7-02303A.svg)](https://gradle.org)

PriceKeeper 用来手动记录日常商品价格、追踪价格变化、比较不同商店价格，并保存商店位置信息用于地图路线规划。项目采用 Clean Architecture + MVVM，数据保存在本机 Room 数据库中。

## 当前能力

| 模块 | 能力 |
|---|---|
| 价格录入 | 手动录入商品、价格、分类、商店与商店位置链接 |
| 商品价格 | 商品列表、分类筛选、搜索、价格详情、趋势折线图、商店价格对比 |
| 商店管理 | 商店列表、商店详情、用户评价、已追踪商品价格范围、地图路线规划 |
| 位置解析 | 支持高德短链展开、经纬度解析、解析失败保存原始地图链接 |
| 数据管理 | `.mypd` 导出/导入、JSON + GZIP、导入冲突策略、数据校验 |
| 我的页面 | 商品/商店/消费/记录统计、数据导入导出、分类管理、关于页面 |

## 已移除能力

小票识别、OCR、相机/相册采集、小票解析与小票存档已从当前版本彻底移除，后续产品路线不再规划该能力。应用不再申请相机权限，也不再依赖 ML Kit 或 CameraX。

## 技术栈

| 分类 | 技术 |
|---|---|
| 语言与构建 | Kotlin 1.9.24, Gradle 8.7, AGP 8.4.0, KSP |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| 架构 | Clean Architecture, MVVM, StateFlow, UseCase |
| 依赖注入 | Hilt |
| 本地数据 | Room, DataStore Preferences |
| 异步 | Kotlin Coroutines, Flow |
| 序列化 | kotlinx.serialization JSON |
| 测试 | JUnit, MockK, Turbine, Room Testing, Compose UI Test |

## 项目结构

```text
app/src/main/java/com/pricekeeper/app/
├── core/          # 导航、主题、通用 UI 基础
├── data/          # Room、DAO、Repository 实现、导入导出
├── domain/        # 领域模型、Repository 接口、UseCase
└── feature/       # Home、Goods、Store、Manual、Profile 等页面
```

核心包说明：

| 包 | 说明 |
|---|---|
| `core/navigation` | 路由常量、底部导航、`NavHost` |
| `core/ui/theme` | Material 主题、颜色、字体、主题偏好 |
| `data/local` | Room Entity、DAO、Database |
| `data/repository` | Repository 实现，封装 DAO 访问 |
| `data/export` | `.mypd` 导入导出与校验 |
| `domain` | 业务模型、Repository 接口、UseCase |
| `feature/manual` | 手动记价格与商店位置链接解析 |
| `feature/goods` | 商品列表、商品详情、价格趋势与比价 |
| `feature/store` | 商店列表、商店详情、评价与路线规划 |

## 架构约束

```text
UI Event -> ViewModel -> UseCase -> Repository Interface -> Repository Impl -> DAO -> Room
```

- Room 是唯一持久化数据源。
- Repository 是唯一数据入口，ViewModel 不直接访问 DAO。
- 依赖通过 Hilt 构造函数注入。
- 读操作优先使用 `Flow` 暴露响应式数据。
- 写操作使用 `suspend` 函数。
- Compose 页面遵循单向数据流：事件上行，状态下行。
- 修改数据库 schema 时，需要同步更新版本号、Migration skeleton 与迁移说明。

## 快速开始

环境要求：Android Studio Hedgehog 或更新版本、JDK 17、Android SDK 34、Android 8.0/API 26 或以上设备/模拟器。

```bash
./gradlew assembleDebug
./gradlew installDebug
./gradlew testDebugUnitTest
./gradlew connectedAndroidTest
```

当前 CI 执行：

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

## 测试

| 类型 | 路径 | 示例 |
|---|---|---|
| UseCase 单元测试 | `app/src/test/java/.../domain/usecase` | `AddManualPriceRecordUseCaseTest` |
| Repository 单元测试 | `app/src/test/java/.../data/repository` | `GoodsRepositoryImplTest` |
| ViewModel 单元测试 | `app/src/test/java/.../feature` | `ManualEntryViewModelTest` |
| Compose UI 测试 | `app/src/androidTest/java/...` | `HomeScreenTest` |

## 数据库

当前数据库版本为 v3，定义在 `PriceKeeperDatabase`。

| 表 | 说明 |
|---|---|
| `goods` | 商品，商品名唯一，包含分类 |
| `store` | 商店，商店名唯一，包含区域、地址、经纬度、地图链接、用户评价 |
| `price_records` | 商品价格记录，关联商品与商店 |

v3 迁移移除了旧版 `receipts` 表和 `price_records.receipt_id` 字段。旧 `.mypd` 备份中的小票字段会被导入器忽略，只导入商品、商店和价格记录。

## 开发入口

| 目标 | 起点 |
|---|---|
| 新增页面 | `feature/<name>` 新建 Screen/ViewModel/UiState，然后在 `AppNavGraph` 注册路由 |
| 新增业务逻辑 | `domain/usecase` 新建 UseCase，并通过 Repository 接口访问数据 |
| 新增查询 | 先扩展 Repository 接口，再在 DAO 与 Repository 实现中落地 |
| 新增数据表 | 添加 Entity/DAO，更新 `PriceKeeperDatabase`，补 Migration |
| 修改导入导出 | `data/export` |
| 修改主题 | `core/ui/theme` |
| 修改地图路线规划 | `feature/navigation/MapNavigation.kt` |

## 文档索引

| 文档 | 内容 |
|---|---|
| [AGENTS.md](AGENTS.md) | Codex/Agent 协作约束 |
| [docs/产品需求文档.md](docs/产品需求文档.md) | 产品需求 |
| [docs/技术设计文档.md](docs/技术设计文档.md) | 技术设计、架构、SQL 与模块说明 |
| [docs/MODULE_README.md](docs/MODULE_README.md) | 模块结构与开发指南 |
| [docs/API_USAGE_EXAMPLES.md](docs/API_USAGE_EXAMPLES.md) | Repository、UseCase 使用示例 |
| [docs/CHANGELOG.md](docs/CHANGELOG.md) | 版本变更记录 |
