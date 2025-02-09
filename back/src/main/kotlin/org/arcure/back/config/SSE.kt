package org.arcure.back.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import fr.arcure.uniting.configuration.security.CustomUser
import org.arcure.back.game.GameEntity
import org.arcure.back.game.GameMapper
import org.arcure.back.player.PlayerEntity
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.concurrent.thread

@Component
class SSEComponent(private val gameMapper: GameMapper) {
    private val sses: MutableMap<Long, SseEmitter> = ConcurrentHashMap()

    private val objectMapper = ObjectMapper()

    init {
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    fun addSse(): SseEmitter? {
        val userId = CustomUser.get().userId
        return sses.getOrPut(userId) { SseEmitter() }
    }

    fun removeSSE(userId: Long) {
        sses.remove(userId)
    }

    fun notifySSE(game: GameEntity) {
        notifySSE(game.players, game)
    }

    fun notifySSE(players: List<PlayerEntity>) {
        notifySSE(players, null)
    }

    private fun notifySSE(players: List<PlayerEntity>, game: GameEntity?) {
        players.forEach {
            Executors.newSingleThreadExecutor().execute(thread {
                val userId = it.user?.id
                val gameResponse = if (game == null) null else gameMapper.toResponse(game, it)
                sses[userId]?.send(objectMapper.writeValueAsString(gameResponse))
            })
        }
    }

}
