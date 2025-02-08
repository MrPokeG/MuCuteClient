package com.mucheng.mucute.client.game.module.motion

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.Ability
import org.cloudburstmc.protocol.bedrock.data.AbilityLayer
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket

class MotionFlyModule : Module("motion_fly", ModuleCategory.Motion) {

    private val verticalSpeedUp = floatValue("verticalUpSpeed", 7.0f, 1.0f..20.0f)
    private val verticalSpeedDown = floatValue("verticalDownSpeed", 7.0f, 1.0f..20.0f)
    private val motionInterval = floatValue("motionInterval", 100.0f, 100.0f..600.0f)
    private var lastMotionTime = 0L
    private var glideSpeed by floatValue("glideSpeed", 1.0f, 0.42f..3.2f)
    private var vanillaFly by floatValue("vanillaFly", 2.15f, 1.0f..5.0f)
    private var jitterState = false
    private var canFly = false


    private val enableFlyAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS,
                    Ability.MAY_FLY,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED
                )
            )
            walkSpeed = 0.1f
            flySpeed = vanillaFly
        })
    }

    private val disableFlyAbilitiesPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        abilityLayers.add(AbilityLayer().apply {
            layerType = AbilityLayer.Type.BASE
            abilitiesSet.addAll(Ability.entries.toTypedArray())
            abilityValues.addAll(
                arrayOf(
                    Ability.BUILD,
                    Ability.MINE,
                    Ability.DOORS_AND_SWITCHES,
                    Ability.OPEN_CONTAINERS,
                    Ability.ATTACK_PLAYERS,
                    Ability.ATTACK_MOBS,
                    Ability.OPERATOR_COMMANDS,
                    Ability.FLY_SPEED,
                    Ability.WALK_SPEED
                )
            )
            walkSpeed = 0.1f
        })
    }


    private fun handleFlyAbilities(isEnabled: Boolean) {
        if (!canFly && isEnabled) {
            enableFlyAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
            session.clientBound(enableFlyAbilitiesPacket)
            canFly = true
        } else if (canFly && !isEnabled) {
            disableFlyAbilitiesPacket.uniqueEntityId = session.localPlayer.uniqueEntityId
            session.clientBound(disableFlyAbilitiesPacket)
            canFly = false
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            handleFlyAbilities(isEnabled)
            if (isEnabled && System.currentTimeMillis() - lastMotionTime >= motionInterval.value) {
                val vertical = when {
                    packet.inputData.contains(PlayerAuthInputData.WANT_UP) -> verticalSpeedUp.value
                    packet.inputData.contains(PlayerAuthInputData.WANT_DOWN) -> -verticalSpeedDown.value
                    else -> 0f
                }
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(
                        session.localPlayer.motionX * glideSpeed,
                        vertical + (if (jitterState) 0.1f else -0.1f),
                        session.localPlayer.motionZ * glideSpeed
                    )
                }
                session.clientBound(motionPacket)
                jitterState = !jitterState
                lastMotionTime = System.currentTimeMillis()
            }
        }
    }
}