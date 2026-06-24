# PriceKeeper API 使用示例

本文展示当前手动录入版本的核心 API：商品、商店、价格记录、地图路线规划和本地导入导出。

## 1. 观察商品列表

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val getGoodsListUseCase: GetGoodsListUseCase
) : ViewModel() {

    val goods: StateFlow<List<Goods>> = getGoodsListUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

## 2. 添加手动价格记录

`AddManualPriceRecordUseCase` 会通过 Repository 完成商品与商店的 find-or-create，再写入价格记录。

```kotlin
class MyViewModel @Inject constructor(
    private val addRecord: AddManualPriceRecordUseCase
) : ViewModel() {

    fun save() {
        viewModelScope.launch {
            val result = addRecord(
                goodsName = "牛奶",
                storeName = "永辉商店",
                price = 12.5,
                goodsCategory = "饮料",
                storeRegion = "北京朝阳区",
                storeAddress = "北京市朝阳区某购物中心",
                storeLatitude = 39.9123,
                storeLongitude = 116.4567,
                storeMapUrl = "https://uri.amap.com/marker?position=116.4567,39.9123"
            )

            result.onSuccess { recordId ->
                // 保存成功，recordId 为新价格记录 ID
            }.onFailure { error ->
                // 展示错误提示
            }
        }
    }
}
```

## 3. 获取商品详情

商品详情聚合价格区间、最近价格、趋势点和各商店比价信息。

```kotlin
class GoodsDetailViewModel @Inject constructor(
    private val getGoodsDetail: GetGoodsDetailUseCase
) : ViewModel() {

    fun load(goodsId: Long) {
        viewModelScope.launch {
            val detail = getGoodsDetail(goodsId)
            // detail.lowestPrice  - 最低价
            // detail.highestPrice - 最高价
            // detail.latestPrice  - 最新价
            // detail.trend        - 价格趋势
            // detail.storePrices  - 各店比价
        }
    }
}
```

## 4. 观察商店列表

```kotlin
@HiltViewModel
class StoreViewModel @Inject constructor(
    private val getStoreList: GetStoreListUseCase
) : ViewModel() {

    val stores: StateFlow<List<Store>> = getStoreList()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

## 5. 打开地图路线规划

商品详情和商店详情应复用同一套导航逻辑，优先使用经纬度拉起高德路线规划页；没有经纬度时使用保存的地图链接或通用 `geo:` URI 兜底。

```kotlin
openMapRoutePlan(
    context = context,
    destination = MapDestination(
        name = store.name,
        address = store.address,
        latitude = store.latitude,
        longitude = store.longitude,
        mapUrl = store.mapUrl
    )
)
```

## 6. 导出与导入

`.mypd` 文件为 GZIP 压缩后的 JSON，当前结构包含商品、商店和价格记录。

```kotlin
class BackupViewModel @Inject constructor(
    private val exportRepository: ExportRepository,
    private val importRepository: ImportRepository
) : ViewModel() {

    suspend fun exportAll(uri: Uri) {
        exportRepository.exportAll(uri)
    }

    suspend fun importAll(uri: Uri) {
        importRepository.importData(uri, ImportConflictStrategy.MERGE)
    }
}
```

## 架构约束速查

| 规则 | 正确 | 错误 |
|---|---|---|
| 数据源 | `repository.getGoodsDetail(id)` | ViewModel 直接调用 `dao.getById(id)` |
| 依赖注入 | `class Foo @Inject constructor(repo: Repo)` | 手动 new Repository |
| 持续观察 | `fun observeX(): Flow<T>` | 循环调用一次性查询刷新 UI |
| 异步写入 | `suspend fun saveX()` | 主线程直接写数据库 |
| UI 状态 | `StateFlow<UiState>` 单向暴露 | Compose 直接修改 Repository 状态 |
