package com.tientoan.autoaccessibility

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tientoan.autoaccessibility.auto.AdbCommand
import com.tientoan.autoaccessibility.service.AutoAccessibilityService

class MainActivity : AppCompatActivity() {

    private var mAccessibilityServiceIntent: Intent? = null

    companion object {
        const val REQUEST_ACCESSIBILITY_PERMISSION = 1
        const val REQUEST_WRITE_STORAGE_PERMISSION = 2
        const val REQUEST_OVERLAY_CODE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // xin quyen storage
        requestStoragePermission()

        // nếu quyền storage và overlay đã được cấp rồi thì xin luôn quyền accessibility
        if (Settings.canDrawOverlays(this) &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            requestAccessibilityPermission()
        }

        val btn = findViewById<Button>(R.id.btn)
        btn.setOnClickListener {
            mAccessibilityServiceIntent = Intent(this, AutoAccessibilityService::class.java)
            startService(mAccessibilityServiceIntent)
        }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_STORAGE_PERMISSION
            )
        }
    }

    private fun grantOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            openRequestOverlayPermission(this, REQUEST_OVERLAY_CODE)
        }
    }

    private fun openRequestOverlayPermission(activity: Activity, requestCode: Int) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName)
        )
        activity.startActivityForResult(intent, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_WRITE_STORAGE_PERMISSION -> {
                if ((grantResults.isEmpty() &&
                            grantResults[0] != PackageManager.PERMISSION_GRANTED)
                ) {
                    requestStoragePermission()
                    Toast.makeText(this, "Cấp quyền để tiếp tục!", Toast.LENGTH_LONG).show()
                } else {
                    // nếu đã cấp quyền storage thì tiếp tục xin quyền overlay
                    grantOverlayPermission()
                }
                return
            }
        }
    }

    private fun isAccessibilityEnable(): Boolean {
        val s = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return s.contains(getString(R.string.app_name))
    }

    private fun requestAccessibilityPermission() {
        if (!isAccessibilityEnable()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, REQUEST_ACCESSIBILITY_PERMISSION)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ACCESSIBILITY_PERMISSION -> {
                if (!isAccessibilityEnable()) {
                    Toast.makeText(this, "Cấp quyền để tiếp tục!", Toast.LENGTH_LONG).show()
                    finish()
                }

                // xin quyen root
                AdbCommand.requestRootPermission()
            }

            REQUEST_OVERLAY_CODE -> {
                if (Settings.canDrawOverlays(this)) {
                    // có quyền overlay bắt đầu xin quyền accessibility
                    requestAccessibilityPermission()
                }
            }
        }
    }
}