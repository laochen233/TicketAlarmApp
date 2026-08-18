# 抢票闹钟 · 原生安卓 App（Kotlin）

> 锁屏到点自动弹全屏窗 + 狂震 + 一键/自动跳转购票页。
> 网页关了、手机锁屏了，照样响。

---

## 一、它是做什么的（合法边界，先看）

- ✅ **只帮你"打开购票页"**：到点弹窗 + 震动 + 跳转到大麦/猫眼等 App 的开售页。
- ❌ **不替你选座、不自动下单、不自动支付**：那类全自动脚本绕平台安全机制，违反《刑法》285 条，咱不碰。
- 本 App 是**闹钟类工具**，用的是 Android 官方 `AlarmManager` + 前台服务 + 全屏通知，行为与普通闹钟 App 一致，合规。

---

## 二、本环境交付说明（重要）

我这套运行环境**没有 Android SDK，无法直接编译出 `.apk`**。
已交付的是**完整、可直接用 Android Studio 打开编译安装的原生工程源码**（在 `TicketAlarmApp/` 目录）。

### 在你电脑上编译 / 安装（任选其一）

**方式 A：直接 Run 到手机（最快，需 USB 调试）**
1. 电脑装 [Android Studio](https://developer.android.com/studio)。
2. 手机：设置 → 关于手机 → 连点「版本号」7 次开开发者模式 → 打开「USB 调试」。
3. 数据线连电脑，Android Studio 里 `File → Open` 选 `TicketAlarmApp/` 文件夹 → 等待 Gradle Sync 完成 → 选你的手机 → 点 ▶ Run。
4. 首次会提示装「精确闹钟」和「通知」权限，全部允许。

**方式 B：Build 出 APK 自己装**
- Android Studio：`Build → Build Bundle(s) / APK(s) → Build APK(s)` → 产物在 `app/build/outputs/apk/debug/app-debug.apk`，传到手机安装即可。
- 若导入时提示缺 Gradle Wrapper，选「Use default Gradle wrapper」让 AS 自动生成即可。

> 编译要求：Android Studio 自带的 Gradle 8.9 + Android SDK Platform 34（AS 会自动下载）。minSdk 26（Android 8.0+ 手机均可）。

---

## 三、使用步骤

1. 打开 App，填**演出名称**（如「周杰伦 2026 巡回」）。
2. 选**购票平台**（内置大麦 / 猫眼 / 票星球 / 秀动；见下文自定义）。
3. 点「选择开售时间」设定**年月日 + 时分**。
4. 设**提前提醒**（分钟，到点前先震一下；0 = 到点才响）。
5. 点「保存并设置闹钟」→ 授权通知 + 精确闹钟 → 完成。
6. 到点效果：
   - 手机**已解锁** → 自动弹出红屏「开抢！」并自动跳进购票 App。
   - 手机**锁屏** → 系统强制要求至少一次点按（安全机制，绕不过）→ 自动亮屏弹全屏窗 + 狂震，你点一下「立即抢票」即跳转。

---

## 四、关于「锁屏零点击」的系统限制（必读）

Android 从 10 起**禁止后台 App 在锁屏下无交互直接拉起别家 App**——这是防滥用/防恶意软件的安全红线，任何正规 App 都绕不过。
所以现实最优解是：

- **锁屏**：自动亮屏 + 全屏弹窗 + 震动 + **一键跳转**（你点一下）。
- **已解锁**：全自动跳转，零点击。

这正是本 App 的行为。想做到"完全零点击且锁屏自动进购票页"，只有root/定制 ROM 或系统级白名单，那已属灰色地带，不在这版范围内。

---

## 五、自定义平台 / URL Scheme

平台 scheme 在 `app/src/main/java/com/laochen/ticketalarm/MainActivity.kt` 的 `platforms` 列表里：

```kotlin
private val platforms = listOf(
    "大麦" to "damai://",
    "猫眼" to "maoyan://",
    "票星球" to "piaoxingqiu://",
    "秀动" to "showstart://",
    "自定义 URL Scheme" to "__custom__"
)
```

- 想要某个平台直接打开**网页版**而非 App，把 scheme 改成 `https://...` 即可（如 `https://www.damai.cn/`）。
- 想加新平台，照格式加一行 `"平台名" to "scheme://"`。
- `JumpActivity.doJump()` 里也有 scheme 拉不起时的 https 兜底（默认回退大麦官网）。

> 注：各 App 的真实 scheme 可能随版本变化；若跳转没反应，先把对应平台 App 装好，或改用 `https://` 网页链接。

---

## 六、开机/重启后会怎样

已设置闹钟并重启手机，`BootReceiver` 会自动把闹钟重新挂上（需授予「自启动/后台运行」相关权限，部分国产 ROM 在「设置 → 应用管理 → 自启动」里允许本 App）。

---

## 七、目录结构

```
TicketAlarmApp/
├── build.gradle                      # 工程级 Gradle
├── settings.gradle
├── gradle/wrapper/gradle-wrapper.properties
├── app/
│   ├── build.gradle                  # 模块级 Gradle（依赖、SDK 版本）
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml       # 权限 + 组件注册
│       ├── java/com/laochen/ticketalarm/
│       │   ├── MainActivity.kt        # 设置界面 + 权限引导
│       │   ├── AlarmScheduler.kt      # 精确闹钟调度
│       │   ├── AlarmReceiver.kt       # 闹钟广播
│       │   ├── RingService.kt         # 前台服务：全屏通知 + 震动
│       │   ├── JumpActivity.kt        # 锁屏跳转页
│       │   ├── BootReceiver.kt        # 开机恢复
│       │   ├── PreNotify.kt           # 提前提醒
│       │   └── Prefs.kt               # 本地存储
│       └── res/                       # 布局 / 图标 / 主题（矢量，无二进制依赖）
```

---

## 八、临时兜底方案（立刻能用，不用编译）

之前已部署的**网页版**仍可用（支持 Bark / Server酱 锁屏推送、手机浏览器加到主屏、开着页面能震动+跳转）：
**https://8afcf6c1c4444723a953c3b18cbe6ad0.app.workbuddy.link**

网页版缺点是「页面关了就不响」，本安卓 App 弥补了这个短板。
