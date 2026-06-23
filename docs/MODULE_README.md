# PriceKeeper 模块结构

## 架构概览

```text
Presentation (feature/)
  ├── Compose UI       ← 单向数据流，只读 UiState
  └── ViewModel        ← 通过 UseCase 获取数据，不直调 DAO

Domain (domain/)
  ├── UseCase          ← 业务逻辑编排
  ├── Model            ← 领域模型（与 Room Entity 解耦）
  └── Repository Intf  ← 数据契约

Data (data/)
  ├── Repository Impl  ← 实现 Repository 接口
  ├── Room (Entity + DAO + Database)
  ├── Mapper           ← Entity ↔ Domain 映射
  └── DI Modules       ← Hilt 绑定

Core (core/)
  ├── Navigation       ← 路由定义 + BottomBar + NavGraph
  └── UI Theme         ← Material3 主题
```

## 包结构

```text
com.pricekeeper.app
├── PriceKeeperApp.kt           # @HiltAndroidApp
├── MainActivity.kt             # @AndroidEntryPoint
├── core/
│   ├── navigation/
│   │   ├── Route.kt            # 路由常量
│   │   ├── BottomNavBar.kt     # 底部导航
│   │   └── AppNavGraph.kt      # NavHost
│   └── ui/theme/
│       ├── Color.kt / Type.kt / Theme.kt
├── feature/
│   ├── home/     HomeScreen + HomeViewModel
│   ├── goods/    GoodsListScreen + GoodsDetailScreen
│   ├── store/    StoreScreen
│   ├── receipt/  ReceiptCapture + ReceiptRecognize
│   ├── manual/   ManualEntryScreen
│   └── main/     MainScreen (Scaffold)
├── domain/
│   ├── model/    Goods, Store, PriceRecord, GoodsPriceDetail...
│   ├── repository/  GoodsRepository, StoreRepository, PriceRecordRepository
│   └── usecase/  GetGoodsListUseCase, AddManualPriceRecordUseCase...
└── data/
    ├── local/
    │   ├── entity/  GoodsEntity, StoreEntity, PriceRecordEntity, ReceiptEntity
    │   ├── dao/     GoodsDao, StoreDao, PriceRecordDao, ReceiptDao
    │   └── database/ PriceKeeperDatabase
    ├── mapper/  GoodsMapper, StoreMapper, PriceRecordMapper
    ├── repository/ GoodsRepositoryImpl, StoreRepositoryImpl, PriceRecordRepositoryImpl
    └── di/  DatabaseModule, RepositoryModule
```

## 如何添加新功能

1. **新实体**: 在 `data/local/entity/` 添加 `@Entity`，更新 `PriceKeeperDatabase`，创建 Migration
2. **新 DAO**: 在 `data/local/dao/` 添加 `@Dao`，在 `DatabaseModule` 中提供
3. **新 Repository**: 在 `domain/repository/` 添加接口，在 `data/repository/` 实现，在 `RepositoryModule` 中 `@Binds`
4. **新 UseCase**: 在 `domain/usecase/` 添加类，`@Inject constructor`
5. **新 Screen**: 在 `feature/<name>/` 添加 UiState + ViewModel + Screen，在 `AppNavGraph` 注册路由

## 数据流

```text
UI Event → ViewModel → UseCase → Repository → DAO → Room
                                                      ↓
UI ← Compose ← StateFlow ← Flow.map ← Flow<List<Entity>>
```

## 运行命令

```bash
# 编译
./gradlew assembleDebug

# 单元测试
./gradlew testDebugUnitTest

# 安装到设备
./gradlew installDebug

# 代码检查
./gradlew ktlintCheck
```
