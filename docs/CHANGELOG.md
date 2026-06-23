# Changelog

## v1.0.0 — Phase 2 (功能完备)

### 新增
- **OCR 识别流水线**：端侧 ML Kit 中文识别 → Parser 多策略解析 → 可编辑商品列表 → 写入数据库
- **小票解析多策略**：DefaultParser + YonghuiParser + HemaParser + 策略自动选择器
- **相机/相册采集**：CameraX 预览 + 系统相册选择 + 图片压缩（1080px）
- **数据导入导出**：`.mypd` 格式（JSON + GZIP），支持 OVERWRITE/MERGE/SKIP 三种冲突策略
- **价格趋势图**：Compose Canvas 折线图，支持空状态和数据点渲染
- **商店比价图**：横向柱状图，最便宜/最贵着色
- **仪表盘统计**：商品数/商店数/总消费/小票数实时聚合
- **Profile 屏幕**：仪表盘卡片 + 导出/导入按钮 + 深色模式开关 + 分类管理入口
- **Reusable UI 组件**：EmptyStateView + ErrorStateView

### 依赖新增
- `com.google.mlkit:text-recognition-chinese:16.0.1`
- `androidx.camera:camera-*:1.3.4`
- `org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3`
- `kotlin("plugin.serialization")`

### 数据库
- **无 Schema 迁移** — v1 已包含 Phase 2 所需全部列

---

## v1.0.0 — Phase 1 (MVP 内核)

### 新增
- 4 个 Room Entities + DAOs + TDD §5.1-5.5 聚合查询
- 4 个 Repository 接口 + 实现 + Mappers
- 5 个 UseCases
- 6 个 Compose 屏幕：Home, GoodsList, GoodsDetail, Store, ManualEntry, Profile(placeholder)
- 4-tab Bottom Navigation
- CI workflow + PR template + .editorconfig
