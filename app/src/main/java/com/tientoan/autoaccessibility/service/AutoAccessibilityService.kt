package com.tientoan.autoaccessibility.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import com.tientoan.autoaccessibility.R
import com.tientoan.autoaccessibility.auto.AdbCommand
import java.io.ByteArrayOutputStream
import java.io.File

class AutoAccessibilityService : AccessibilityService() {
    private lateinit var mWindowManager: WindowManager
    private lateinit var mControllerView: View

    override fun onCreate() {
        super.onCreate()

        // Hiển thị nút reset
        addControllerView()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("ToanNTe", "Service connected: ")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO chạy các lệnh auto ở đây

        return START_STICKY
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    private fun addControllerView() {
        mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        mControllerView =
            LayoutInflater.from(this).inflate(R.layout.controller_layout, null)
        val btnReset = mControllerView.findViewById<Button>(R.id.btnReset)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            10,
            300,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.START or Gravity.TOP

        mWindowManager.addView(mControllerView, params)

        // opacity 50% cho button reset
        btnReset.background.alpha = 170

        btnReset.setOnClickListener {
            // TODO thêm xử lí cho nút reset
        }
    }

    private fun removeControllerView() {
        try {
            mWindowManager.removeView(mControllerView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeControllerView()
    }

    /**
     * Click 1 vị trí có tọa độ bất kì trên màn hình
     * @param x: tọa độ X
     *        y: tọa độ Y
     */
    private fun click(x: Int, y: Int) {
        val clickPath = Path()
        clickPath.moveTo(x.toFloat(), y.toFloat())
        val builder = GestureDescription.Builder()
        val gestureDescription = builder
            .addStroke(GestureDescription.StrokeDescription(clickPath, 0, 10))
            .build()
        dispatchGesture(gestureDescription, null, null)
    }

    /**
     * Nến ảnh sang base64 rồi xóa ảnh đó đi
     * @param: đường dẫn lưu screenshots
     */
    private fun getScreenshotBase64(pathScreenshot: String): String? {
        val screenshotDirectory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            pathScreenshot
        )

        val file = File(screenshotDirectory.toString())
        val listFile = file.listFiles() // lấy ra tất cả các file có trong thư mục screenshots
        if (listFile != null && listFile.isNotEmpty()) {
            val screenshot = listFile[listFile.size - 1] // lấy ảnh mới nhất
            val bitmap = BitmapFactory.decodeFile(screenshot.absolutePath)
            val bao = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bao)

            // xoa anh
            screenshot.delete()

            // nén sang base64
            return Base64.encodeToString(bao.toByteArray(), Base64.DEFAULT)
        }
        return null
    }
}