package my.pollos.floatingclock

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class FloatingClockService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var timeTextView: TextView? = null
    private var handler: Handler? = null
    private var params: WindowManager.LayoutParams? = null
    private var touchX = 0f
    private var touchY = 0f
    private var initialX = 0
    private var initialY = 0

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_clock_layout, null)
        timeTextView = floatingView!!.findViewById<TextView>(R.id.timeView)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params!!.gravity = Gravity.TOP or Gravity.START
        params!!.x = 0
        params!!.y = 100

        // 设置拖动监听
        floatingView!!.setOnTouchListener { view: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchX = event.rawX
                    touchY = event.rawY
                    initialX = params!!.x
                    initialY = params!!.y
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    params!!.x = initialX + (event.rawX - touchX).toInt()
                    params!!.y = initialY + (event.rawY - touchY).toInt()
                    windowManager!!.updateViewLayout(floatingView, params)
                    return@setOnTouchListener true
                }
            }
            false
        }

        windowManager!!.addView(floatingView, params)
        startClock()
    }

    private fun startClock() {
        handler = Handler()
        handler!!.post(object : Runnable {
            override fun run() {
                val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
                timeTextView!!.text = sdf.format(Date())
                handler!!.postDelayed(this, 1) // 每毫秒更新一次
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null && windowManager != null) {
            windowManager!!.removeView(floatingView)
        }
        if (handler != null) {
            handler!!.removeCallbacksAndMessages(null)
        }
    }
}