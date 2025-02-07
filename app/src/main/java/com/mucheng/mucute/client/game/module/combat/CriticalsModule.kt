package com.mucheng.mucute.client.game.module.combat

import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class CriticalsModule : Module("critic", ModuleCategory.Combat) {

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

        val message = "§l§b[MuCute] §r§7Criticals §8» $status"



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

        if (!isEnabled){

            return false
        }


      else if (packet is PlayerAuthInputPacket && isEnabled) {
            packet.inputData.add(PlayerAuthInputData.START_JUMPING)
            packet.inputData.add(PlayerAuthInputData.JUMPING)
            packet.position.add(0.0,0.2,0.0)
  }

        return false


    }

}
