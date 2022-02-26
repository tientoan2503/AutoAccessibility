package com.tientoan.autoaccessibility.auto

import android.content.Context
import android.util.Log
import java.io.DataOutputStream
import java.io.IOException

object AdbCommand {

    fun requestRootPermission() {
        runCommand("devices")
    }

    fun openApp(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        context.startActivity(launchIntent)
    }

    fun closeApp(packageName: String) {
        runCommand("am force-stop $packageName")
    }

    fun inputText(text: String) {
        runCommand("input text $text")
    }

    fun tap(x: Int, y: Int) {
        runCommand("input tap $x $y")
    }

    fun reboot() {
        runCommand("reboot")
    }

    private fun runCommand(command: String) {
        try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            val cmd = "$command\n"
            os.writeBytes(cmd)
            os.writeBytes("exit\n")
            os.flush()
            os.close()
            Log.d("ToanNTe", "runCommand: $command")
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}