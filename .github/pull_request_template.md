## 概要

<!-- 用一两句话说明这个 PR 做了什么 -->

## 关联文档

<!-- 引用 TDD 中的章节，例如：TDD §4.1 GoodsEntity, TDD §7 Repository -->

## 变更文件

<!-- 列出本次修改/新增的文件 -->

| 文件 | 变更类型 | 说明 |
|---|---|---|
| `path/to/File.kt` | 新增 / 修改 | ... |

## 测试计划

- [ ] 单元测试通过: `./gradlew testDebugUnitTest`
- [ ] 代码风格检查通过: `./gradlew ktlintCheck`
- [ ] 编译通过: `./gradlew assembleDebug`

### 手动验证步骤

<!-- 描述如何在真机/模拟器上验证变更 -->

1. ...
2. ...

## 截图（如有 UI 变更）

<!-- 附上 Compose Preview 截图或真机截图 -->

## 架构合规检查

- [ ] Room 为唯一数据源
- [ ] Repository 为唯一数据入口
- [ ] ViewModel 未直调 DAO
- [ ] 使用 Hilt 构造函数注入
- [ ] Flow 用于观察、suspend 用于写操作

---

🤖 Generated with [Claude Code](https://claude.com/claude-code)
