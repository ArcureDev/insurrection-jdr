package org.arcure.back

import fr.arcure.uniting.configuration.security.CustomUser
import org.arcure.back.game.GameEntity
import org.arcure.back.player.PlayerEntity

fun getMyPlayer(gameEntity: GameEntity): PlayerEntity {
    val myPlayer = gameEntity.players.find { it.user?.id == CustomUser.get().userId }

    check(myPlayer != null) {
        "My player doesn't exist"
    }

    return myPlayer
}