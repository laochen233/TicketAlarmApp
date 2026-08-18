package com.laochen.ticketalarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat

class JumpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 锁屏也能显示并亮屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 收掉前台通知
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        mgr.cancel(RingService.NOTIF_ID)

        setContentView(R.layout.activity_jump)

        val name = Prefs.eventName(this)
        findViewById<TextView>(R.id.tvJumpName).text = name

        val btn = findViewById<Button>(R.id.btnJump)
        btn.setOnClickListener { doJump() }

        // 已解锁则自动尝试跳转；锁屏下系统要求至少一次点击，按钮兜底
        val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val locked = km.isKeyguardLocked
        if (!locked) {
            Handler(Looper.getMainLooper()).postDelayed({ doJump() }, 600)
        }
    }

    private fun doJump() {
        val scheme = Prefs.scheme(this)
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scheme)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            // scheme 拉不起则回退官网（可在 MainActivity 里把 scheme 设为 https 链接）
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.damai.cn")))
            } catch (_: Exception) { /* ignore */ }
        }
        // 跳转后稍作延迟关闭本活动
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 1500)
    }
}
