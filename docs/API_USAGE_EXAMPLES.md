# PriceKeeper API 使用示例

## 1. 观察商品列表

通过 `GetGoodsListUseCase` 获取实时更新的商品列表：

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

通过 `AddManualPriceRecordUseCase` 添加价格记录（自动 find-or-create 商品和商店）：

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
                storeRegion = "北京朝阳区"
            )
            result.onSuccess { recordId ->
                // 保存成功，recordId 为新记录 ID
            }.onFailure { error ->
                // 处理错误
            }
        }
    }
}
```

## 3. 获取商品详情（含价格趋势+比价）

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
            // detail.trend        - List<PricePoint> 价格趋势
            // detail.storePrices  - List<StorePriceInfo> 各店比价
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

## 5. 直接使用 Repository（在 UseCase 层）

```kotlin
class CustomUseCase @Inject constructor(
    private val goodsRepository: GoodsRepository,
    private val storeRepository: StoreRepository
) {
    suspend fun findCheapest(goodsName: String): Store? {
        // Repository 是最底层的数据入口
        // 所有 DAO 访问都必须通过 Repository
        return null // 实现查找逻辑
    }
}
```

## 6. 保存小票

```kotlin
class ReceiptViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    fun saveReceipt(imagePath: String, ocrText: String) {
        viewModelScope.launch {
            val receiptId = receiptRepository.saveReceipt(
                storeId = null,
                totalPrice = 45.8,
                buyDate = System.currentTimeMillis(),
                imagePath = imagePath,
                ocrRawText = ocrText
            )
        }
    }
}
```

## 架构约束速查

| 规则 | 正确 ✅ | 错误 ❌ |
|---|---|---|
| 数据源 | `repository.getGoodsDetail(id)` | `dao.getById(id)` 在 ViewModel 中 |
| 依赖注入 | `class Foo @Inject constructor(repo: Repo)` | 手动 new 或 object 单例 |
| 异步读 | `fun observeX(): Flow<T>` | `suspend fun getX(): T` 用于持续观察 |
| 异步写 | `suspend fun saveX()` | 在主线程直接调用 |
| UI 模式 | `StateFlow<UiState>` 单向 | ViewModel 持有 MutableState 暴露给 Compose |
