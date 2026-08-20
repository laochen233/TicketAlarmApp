package com.laochen.ticketalarm

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val platforms = listOf(
        "大麦" to "damai://",
        "猫眼" to "maoyan://",
        "票星球" to "piaoxingqiu://",
        "秀动" to "showstart://",
        "自定义 URL Scheme" to "__custom__"
    )

    private var pickYear = 0
    private var pickMonth = 0
    private var pickDay = 0
    private var pickHour = 0
    private var pickMinute = 0
    private var timeChosen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sp = findViewById<Spinner>(R.id.spPlatform)
        sp.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            platforms.map { it.first }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        findViewById<Button>(R.id.btnPickTime).setOnClickListener { showPickers() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { onSave() }

        // 恢复已设置信息
        if (Prefs.isSet(this)) {
            val cal = Calendar.getInstance().apply { timeInMillis = Prefs.triggerAt(this@MainActivity) }
            pickYear = cal.get(Calendar.YEAR)
            pickMonth = cal.get(Calendar.MONTH)
            pickDay = cal.get(Calendar.DAY_OF_MONTH)
            pickHour = cal.get(Calendar.HOUR_OF_DAY)
            pickMinute = cal.get(Calendar.MINUTE)
            timeChosen = true
            updateTimeText()
            findViewById<EditText>(R.id.etName).setText(Prefs.eventName(this))
            findViewById<EditText>(R.id.etPre).setText(Prefs.preMin(this).toString())
            val idx = platforms.indexOfFirst { it.second == Prefs.scheme(this) }
            if (idx >= 0) sp.setSelection(idx)
        }
    }

    private fun showPickers() {
        val cal = Calendar.getInstance()
        if (timeChosen) cal.set(pickYear, pickMonth, pickDay, pickHour, pickMinute)
        android.app.DatePickerDialog(
            this,
            { _, y, m, d ->
                pickYear = y; pickMonth = m; pickDay = d
                android.app.TimePickerDialog(
                    this,
                    { _, h, mi ->
                        pickHour = h; pickMinute = mi; timeChosen = true
                        updateTimeText()
                    },
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
                ).show()
            },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateTimeText() {
        findViewById<TextView>(R.id.tvTime).text =
            String.format("%04d-%02d-%02d %02d:%02d", pickYear, pickMonth + 1, pickDay, pickHour, pickMinute)
    }

    private fun onSave() {
        val name = findViewById<EditText>(R.id.etName).text.toString().trim()
        val preStr = findViewById<EditText>(R.id.etPre).text.toString().trim()
        val pre = preStr.toIntOrNull() ?: 0

        if (name.isEmpty()) { toast("请填写演出名称"); return }
        if (!timeChosen) { toast("请选择开售时间"); return }

        val pos = findViewById<Spinner>(R.id.spPlatform).selectedItemPosition
        val (_, schemeRaw) = platforms[pos]
        val scheme = if (schemeRaw == "__custom__") {
            // 自定义：如需填自己的 scheme，可在源码 platforms 里直接写死，或把 scheme 设为 https 链接
            "damai://"
        } else schemeRaw

        val cal = Calendar.getInstance().apply {
            set(pickYear, pickMonth, pickDay, pickHour, pickMinute, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val trigger = cal.timeInMillis
        if (trigger <= System.currentTimeMillis()) { toast("开售时间需晚于当前时间"); return }

        if (!ensurePermissions()) return

        Prefs.save(this, name, scheme, trigger, pre)
        AlarmScheduler.schedule(this, trigger, pre)
        findViewById<TextView>(R.id.tvStatus).text = "已设置：${updateTimeTextStr()} 开抢「$name」"
        toast("闹钟已设置")
    }

    private fun updateTimeTextStr(): String =
        String.format("%04d-%02d-%02d %02d:%02d", pickYear, pickMonth + 1, pickDay, pickHour, pickMinute)

    private fun ensurePermissions(): Boolean {
        val requests = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requests.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            try {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            } catch (_: Exception) { }
            toast("请开启「精确闹钟」权限后重试")
            return false
        }
        if (requests.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, requests.toTypedArray(), 1)
            toast("请授权通知权限后重试")
            return false
        }
        return true
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
