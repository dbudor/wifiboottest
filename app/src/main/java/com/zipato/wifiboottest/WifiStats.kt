package com.zipato.wifiboottest

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class WifiStats(
    val time: Instant,
    val totalCount: Int = 0,
    val okCount: Int = 0,
    val failCount: Int = 0,
    val ok: Boolean = false,
    val address: String? = null
)
