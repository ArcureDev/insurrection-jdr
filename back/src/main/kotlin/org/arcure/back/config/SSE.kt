package org.arcure.back.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import fr.arcure.uniting.configuration.security.CustomUser
import org.arcure.back.game.GameEntity
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@Component
class SSEComponent() {
    private val sses: MutableMap<Long, SseEmitter> = ConcurrentHashMap()

    private val objectMapper = ObjectMapper()

    init {
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    fun addSse(): SseEmitter? {
        val userId = CustomUser.get().userId
        return sses.computeIfAbsent(userId) { _: Long? -> SseEmitter(Long.MAX_VALUE) }
    }

    fun removeSSE(userId: Long) {
        sses.remove(userId)
    }

    // à chaque fois que la game est mise à jour
    fun sendGameThroughSSE(playersIds: List<Long>, game: GameEntity) {
        playersIds.forEach {
            sses[it]?.send(objectMapper.writeValueAsString(game))
        }
    }
}
