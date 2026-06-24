# PriceKeeper 模块结构

PriceKeeper 当前版本定位为手动录入优先的个人物价记录与比价工具，核心模块围绕商品、商店、价格记录、地图路线规划和本地导入导出展开。

## 架构概览

```text
Presentation (feature/)
  ├── Compose UI       ← 单向数据流，只读 UiState
  └── ViewModel        ← 通过 UseCase 获取数据，不直调 DAO

Domain (domain/)
  ├── UseCase          ← 业务逻辑编排
  ├── Model            ← 领域模型，与 Room Entity 解耦
  └── Repository Intf  ← 数据契约

Data (data/)
  ├── Repository Impl  ← 实现 Repository 接口
  ├── Room             ← Entity + DAO + Database
  ├── Mapper           ← Entity 与 Domain 映射
  └── Export           ← .mypd 导入导出

Core (core/)
  ├── Navigation       ← 路由定义 + BottomBar + NavGraph
  └── UI Theme         ← Material3 主题
```

## 包结构

```text
com.pricekeeper.app
├── PriceKeeperApp.kt
├── MainActivity.kt
├── core/
│   ├── navigation/
│   │   ├── Route.kt
│   │   ├── BottomNavBar.kt
│   │   └── AppNavGraph.kt
│   └── ui/theme/
├── feature/
│   ├── home/       首页与手动录入入口
│   ├── manual/     新增物价记录、分类/商店弹窗、地图链接解析
│   ├── goods/      商品列表、详情、趋势、比价、导航入口
│   ├── store/      商店列表、商店详情、评价、商品价格层级展示
│   ├── navigation/ 地图路线规划 Intent
│   ├── profile/    数据仪表盘、导入导出、设置
│   └── main/       主 Scaffold
├── domain/
│   ├── model/      Goods, Store, PriceRecord, GoodsPriceDetail...
│   ├── repository/ GoodsRepository, StoreRepository, PriceRecordRepository
│   └── usecase/    GetGoodsListUseCase, AddManualPriceRecordUseCase...
└── data/
    ├── local/
    │   ├── entity/   GoodsEntity, StoreEntity, PriceRecordEntity
    │   ├── dao/      GoodsDao, StoreDao, PriceRecordDao
    │   └── database/ PriceKeeperDatabase
    ├── mapper/       GoodsMapper, StoreMapper, PriceRecordMapper
    ├── repository/   GoodsRepositoryImpl, StoreRepositoryImpl, PriceRecordRepositoryImpl
    ├── export/       ExportRepositoryImpl, ImportRepositoryImpl
    └── di/           DatabaseModule, RepositoryModule, ExportModule
```

## 数据流

```text
UI Event → ViewModel → UseCase → Repository → DAO → Room
                                                      ↓
UI ← Compose ← StateFlow ← Flow.map ← Flow<List<Entity>>
```

## 添加新功能

1. 新实体：在 `data/local/entity/` 添加 `@Entity`，更新 `PriceKeeperDatabase`，创建 Migration。
2. 新 DAO：在 `data/local/dao/` 添加 `@Dao`，在 `DatabaseModule` 中提供。
3. 新 Repository：在 `domain/repository/` 添加接口，在 `data/repository/` 实现，在 `RepositoryModule` 中绑定。
4. 新 UseCase：在 `domain/usecase/` 添加类，使用 `@Inject constructor`。
5. 新 Screen：在 `feature/<name>/` 添加 UiState、ViewModel、Screen，并在 `AppNavGraph` 注册路由。

## 架构约束

- Room 是唯一持久化数据源。
- Repository 是唯一数据入口，ViewModel 禁止直接调用 DAO。
- Compose 遵循单向数据流，事件上行，状态下行。
- 修改数据库 schema 时必须同步更新版本号、Migration 与迁移说明。
- 导入导出格式变更必须保持旧文件可降级导入，无法兼容时要给出明确错误。

## 运行命令

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedAndroidTest
```
