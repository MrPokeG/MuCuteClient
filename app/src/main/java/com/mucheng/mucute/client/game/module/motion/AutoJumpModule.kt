package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

class AutoJumpModule : Module("auto_jump", ModuleCategory.Motion) {

    private val jumpHeight by floatValue("Jump Height", 0.42f, 0.4f..3.0f)
    private val motionInterval = 120 // Interval between adjustments in milliseconds
    private var lastMotionTime = 0L

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
        val message = "§l§b[MuCute] §r§7BHOP §8» $status"

        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            isNeedsTranslation = false
            this.message = message
            xuid = ""
            sourceName = ""
        }

        session.clientBound(textPacket)
    }

    override fun beforePacketBound(packet: BedrockPacket): Boolean {
        if (isEnabled) {
            val currentTime = System.currentTimeMillis()

            // Apply vertical motion adjustments at defined intervals
            if (currentTime - lastMotionTime >= motionInterval) {
                // Apply upward and downward motion to simulate jumping
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId

                    // Alternate vertical motion to simulate jumping up and down
                    motion = Vector3f.from(
                        session.localPlayer.motionX,  // Keep horizontal motion
                        if ((currentTime / motionInterval) % 2 == 0L) jumpHeight else -jumpHeight,  // Alternate between upwards and downwards motion
                        session.localPlayer.motionZ   // Keep horizontal motion
                    )
                }

                // Send the motion packet to the server
                session.clientBound(motionPacket)

                // Update the last motion time
                lastMotionTime = currentTime
            }
        }
        return false
    }
}