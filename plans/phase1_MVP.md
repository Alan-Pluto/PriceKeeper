# 阶段 1 — MVP（核心可用）

目标（时长：4 周）
- 快速交付可用于记录与查看价格的最小可行产品。

周计划
- 周 0（准备）：搭建工程骨架、代码规范、CI 初始（lint、格式化）、创建模块目录结构。
- 周 1（模型与持久层）：实现 Room 实体与 DAO：`GoodsEntity`、`StoreEntity`、`PriceRecordEntity`、`ReceiptEntity`；编写基础 DAO 查询与索引建议。
- 周 2（Repository 与 UseCase）：实现仓库接口（`GoodsRepository`、`StoreRepository`、`ReceiptRepository`）与本地实现；实现 UseCase：`GetGoodsListUseCase`、`GetGoodsDetailUseCase`、`AddManualPriceRecordUseCase`。
- 周 3（ViewModel 与 UI）：实现对应 ViewModel 与 Compose 屏幕：`HomeScreen`、`GoodsDetailScreen`、`StoreScreen`、`ManualEntryScreen`；保证单向数据流与 UIState 模型。
- 周 4（测试与收尾）：补充单元测试、Compose UI snapshot 测试；修复关键问题并准备合并 PR。

交付物
- 源码：实体、DAO、Repository（接口+实现）、UseCase、ViewModel、Compose 屏幕。
- 测试：Repository/UseCase 单元测试、基础 UI snapshot 测试。
- 文档：模块 README、API 使用示例、PR 模板。

验收标准
- 能进行商品/门店/价格的增删改查操作。
- `GoodsDetail` 能展示价格趋势（基于 `PriceRecord` 聚合）。
- 关键 UseCase 与 Repository 有单元测试，CI 能通过基础检查（lint + 单元测试）。

开发与 PR 规范
- 单一变更原则：每个 PR 针对单一功能。
- 提交需包含：实现文件、测试、README 片段、PR 描述引用 TDD（[docs/技术设计文档.md](docs/技术设计文档.md)）。

AI 生成提示范例（用于请求 AI 生成文件）
- "生成 `data/room/GoodsDao.kt`，包含增删改查以及按名称/类别查询。返回文件内容、单元测试草案与 PR 描述。"

风险与缓解
- 数据模型迭代风险：通过迁移策略与兼容字段（非破坏性改动）缓解。