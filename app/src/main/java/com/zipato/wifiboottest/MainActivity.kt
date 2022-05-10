package com.zipato.wifiboottest

import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.zipato.wifiboottest.databinding.ActivityMainBinding
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.IOException
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@ExperimentalSerializationApi
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var ok: CheckBox
    private lateinit var totalCount: TextView
    private lateinit var okCount: TextView
    private lateinit var failCount: TextView
    private lateinit var ipaddress: TextView

    private lateinit var stats: WifiStats
    private val executor = Executors.newSingleThreadScheduledExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        ok = findViewById(R.id.ok)
        totalCount = findViewById(R.id.totalCount)
        okCount = findViewById(R.id.okCount)
        failCount = findViewById(R.id.failCount)
        ipaddress = findViewById(R.id.ipaddr)
        ok.keepScreenOn = true

        executor.schedule(::updateStats, 30, TimeUnit.SECONDS)
        executor.schedule(::reboot, 60, TimeUnit.SECONDS)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateStats() {
        val path = getExternalFilesDirs(null).last()
        stats = readStats(path)
        val addr = getIpv4HostAddress()
        stats = WifiStats(
            time = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
            ok = addr != null,
            totalCount = stats.totalCount + 1,
            okCount = if (addr == null) stats.okCount else stats.okCount + 1,
            failCount = if (addr != null) stats.failCount else stats.failCount + 1,
            address = addr
        )
        writeStats(path, stats)
        writeLog(path, stats)
        if (hasWindowFocus()) {
            runOnUiThread(::showStats)
        }
    }

    private fun writeStats(path: File, stats: WifiStats) {
        val file = File(path, "wifistats.json")
        val stream = file.outputStream()
        Json.encodeToStream(stats, stream)
        stream.close()
    }

    private fun writeLog(path: File, stats: WifiStats) {
        val log = File(path, "wifilog.json")
        val json = Json.encodeToString(stats)
        log.appendText(json + "\n")
    }

    private fun readStats(path: File): WifiStats {
        val file = File(path, "wifistats.json")
        try {
            val stream = file.inputStream()
            return Json.decodeFromStream(stream)
        } catch (e: IOException) {
            return WifiStats(time = Instant.fromEpochMilliseconds(System.currentTimeMillis()))
        }
    }

    fun getIpv4HostAddress(): String? {
        NetworkInterface.getNetworkInterfaces()
            ?.toList()
            ?.filter { it.name == "wlan0" }
            ?.map { networkInterface ->
                networkInterface.inetAddresses
                    ?.toList()
                    ?.find {
                        !it.isLoopbackAddress && it is Inet4Address
                    }?.let { return it.hostAddress }
            }
        return null
    }

    fun showStats() {
        ok.isChecked = stats.ok
        totalCount.text = stats.totalCount.toString()
        okCount.text = stats.okCount.toString()
        failCount.text = stats.failCount.toString()
        ipaddress.text = stats.address ?: "NO ADDRESS"
    }

    fun reboot() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        pm.reboot(null)
    }
}