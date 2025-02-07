package com.mucheng.mucute.client.game.module.effect

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import com.mucheng.mucute.client.game.data.Effect
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class SlowFallModule : Module("slow_fall", ModuleCategory.Effect) {

    override fun onEnabled() {

        if (isSessionCreated) {

            sendToggleMessage(true)

        }

    }

    override fun onDisabled() {
        if (isSessionCreated) {
            session.clientBound(MobEffectPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                event = MobEffectPacket.Event.REMOVE
                effectId = Effect.SLOW_FALLING
            })
            sendToggleMessage(false)
        }
    }

    private fun sendToggleMessage(enabled: Boolean) {

        val status = if (enabled) "§aEnabled" else "§cDisabled"

        val message = "§l§b[MuCute] §r§7Slow Fall §8» $status"



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
        if (packet is PlayerAuthInputPacket && isEnabled) {
            if (session.localPlayer.tickExists % 20 == 0L) {
                session.clientBound(MobEffectPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    event = MobEffectPacket.Event.ADD
                    effectId = Effect.SLOW_FALLING
                    amplifier = 0
                    isParticles = false
                    duration = 360000
                })
            }
        }
        return false
    }
}
