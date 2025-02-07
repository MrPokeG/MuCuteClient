package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import kotlin.random.Random

class RandomMoveModule : Module("random_move", ModuleCategory.Motion) {

    private val maxMoveDistance by floatValue("Repeat", 4.5f, 2.0f..10.0f)
    private val moveCooldown by intValue("Delay", 300, 100..1000)

    override fun onEnabled() {
        if (isSessionCreated) {
            sendToggleMessage(true)
        }
    }

    override fun onDisabled() {
        if (isSessionCreated) {
            sendToggleMessage(false)
        }
    }

    private fun sendToggleMessage(enabled: Boolean) {
        val status = if (enabled) "§aEnabled" else "§cDisabled"
        val message = "§l§b[MuCute] §r§7Random Move §8» $status"

        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            isNeedsTranslation = false
            this.message = message
            xuid = ""
            sourceName = ""
        }

        session.clientBound(textPacket)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (isEnabled && packet is PlayerAuthInputPacket) {
            GlobalScope.launch {
                while (isEnabled) {
                    delay(moveCooldown.toLong())

                    // Generate random movement only for X and Z (no Y-axis movement)
                    val randomX = Random.nextFloat() * maxMoveDistance - (maxMoveDistance / 2)
                    val randomZ = Random.nextFloat() * maxMoveDistance - (maxMoveDistance / 2)

                    val motionPacket = SetEntityMotionPacket().apply {
                        runtimeEntityId = session.localPlayer.runtimeEntityId
                        motion = Vector3f.from(
                            randomX,  // X-axis movement
                            session.localPlayer.motionY, // Keep current Y motion
                            randomZ   // Z-axis movement
                        )
                    }

                    session.clientBound(motionPacket)
                }
            }
        }
        return false
    }
}