package io.cstories.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import io.cstories.runtime.resources.Res
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.ExperimentalResourceApi

enum class MobilePlatform {
    Ios,
    Android,
}

data class MobileDevice(
    val id: String = "default",
    val name: String = "Mobile device",
    val platform: MobilePlatform = MobilePlatform.Android,
    val width: androidx.compose.ui.unit.Dp = 390.dp,
    val height: androidx.compose.ui.unit.Dp = 844.dp,
    val cornerRadius: androidx.compose.ui.unit.Dp = 36.dp,
) {
    companion object {
        val Default = MobileDevice()
    }
}

object MobileDeviceCatalog {
    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadBuiltIn(): List<MobileDevice> {
        val json = Json.parseToJsonElement(Res.readBytes("files/mobile-devices.json").decodeToString())
        return json.jsonObject.getValue("devices").jsonArray.map { element ->
            val device = element.jsonObject
            MobileDevice(
                id = device.getValue("id").jsonPrimitive.content,
                name = device.getValue("name").jsonPrimitive.content,
                platform = when (device.getValue("platform").jsonPrimitive.content) {
                    "ios" -> MobilePlatform.Ios
                    else -> MobilePlatform.Android
                },
                width = device.getValue("width").jsonPrimitive.content.toFloat().dp,
                height = device.getValue("height").jsonPrimitive.content.toFloat().dp,
                cornerRadius = device.getValue("cornerRadius").jsonPrimitive.content.toFloat().dp,
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun rememberMobileDevices(additional: List<MobileDevice>): List<MobileDevice> {
    var builtIn by remember { mutableStateOf<List<MobileDevice>?>(null) }
    LaunchedEffect(Unit) {
        builtIn = MobileDeviceCatalog.loadBuiltIn()
    }
    return remember(builtIn, additional) {
        val merged = LinkedHashMap<String, MobileDevice>()
        (builtIn.orEmpty() + additional).forEach { merged[it.id] = it }
        merged.values.toList()
    }
}
