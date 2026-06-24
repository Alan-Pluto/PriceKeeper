# 阶段 1 — MVP（手动记录核心）

目标（时长：4 周）

- 交付一个可稳定记录商品价格、查看历史价格和管理商店的最小可用版本。

周计划

- 周 0（准备）：搭建工程骨架、代码规范、CI 初始检查、创建 Clean Architecture 模块目录。
- 周 1（模型与持久层）：实现 `GoodsEntity`、`StoreEntity`、`PriceRecordEntity`、DAO、Room Database 与索引。
- 周 2（Repository 与 UseCase）：实现 `GoodsRepository`、`StoreRepository`、`PriceRecordRepository` 及本地实现；实现 `GetGoodsListUseCase`、`GetGoodsDetailUseCase`、`AddManualPriceRecordUseCase`。
- 周 3（ViewModel 与 UI）：实现 `HomeScreen`、`ManualEntryScreen`、`GoodsListScreen`、`GoodsDetailScreen`、`StoreScreen`、`ProfileScreen`。
- 周 4（测试与收尾）：补充 UseCase、Repository、关键 UI 测试，修复阻断问题并准备合并。

交付物

- 商品、商店、价格记录三张核心表。
- 手动新增价格记录流程。
- 商品价格趋势与商店比价。
- 商店列表与商店详情。
- 基础导入导出能力。

验收标准

- 用户可以手动完成一条价格记录的新增、查看和删除。
- 商品详情能展示最低价、最高价、最近价、趋势和各店价格。
- 商店详情能展示该店关联商品和最近购买信息。
- ViewModel 不直接访问 DAO，所有数据访问通过 Repository。
- `assembleDebug` 与 `testDebugUnitTest` 通过。

AI 生成提示范例

- “生成 `data/local/dao/GoodsDao.kt`，包含增删改查、按名称查询、按分类观察列表，并提供对应 Repository 调用方式。”
