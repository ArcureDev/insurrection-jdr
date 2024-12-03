package org.arcure.back.game

import fr.arcure.uniting.configuration.security.CustomUser
import jakarta.persistence.*
import jakarta.validation.constraints.Max
import org.arcure.back.config.SSEComponent
import org.arcure.back.player.PlayerEntity
import org.arcure.back.user.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.http.MediaType
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*

@Entity
@Table(name = "game")
class GameEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Max(value = 28)
    val nbAvailableShardTokens: Int = 28,
    @OneToMany(mappedBy = "game", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    val players: MutableList <PlayerEntity> = mutableListOf(),
    @Enumerated(EnumType.STRING)
    var state: GameState = GameState.ON_GOING,
    val url: String = UUID.randomUUID().toString()
) {
}

enum class GameState {
    ON_GOING, DONE
}

@Repository
interface GameRepository : JpaRepository<GameEntity, Long> {
    @Query("""
        SELECT g.id, g.nb_available_shard_tokens, g.state, g.url
        FROM game g
        INNER JOIN player p ON p.game_id = g.id
        WHERE p.user_id = :userId
    """, nativeQuery = true)
    fun findAllByPlayers(userId: Long): MutableList<GameEntity>

    @Query("""
        SELECT g.id, g.nb_available_shard_tokens, g.state, g.url
        FROM game g
        INNER JOIN player p ON p.game_id = g.id
        WHERE p.user_id = :userId
        AND g.state = 'ON_GOING'
        LIMIT 1
    """, nativeQuery = true)
    fun findCurrent(userId: Long): GameEntity?

    fun findByUrl(url: String): GameEntity?
}

@Service
@Transactional(readOnly = true)
class GameService(private val gameRepository: GameRepository, private val userRepository: UserRepository,
                  val sseComponent: SSEComponent
) {

    @Transactional
    fun create(playerName: String): GameEntity {
        checkHasNoOnGoingGame()
        val gameEntity = GameEntity()
        addPlayerToGame(gameEntity, playerName)
        gameRepository.save(gameEntity)

        return gameEntity
    }

    fun getAllMine(): List<GameEntity> {
        return gameRepository.findAllByPlayers(CustomUser.get().userId)
    }

    fun getOnGoingGame(): GameEntity? {
        return gameRepository.findCurrent(CustomUser.get().userId)
    }

    @Transactional
    fun join(url: String, playerName: String): GameEntity {
        val gameEntity = gameRepository.findByUrl(url)
        check (gameEntity != null) {
            "Game not exists"
        }
        val onGoingGame = getOnGoingGame()
        if (gameEntity.id == onGoingGame?.id) {
            return gameEntity
        }
        addPlayerToGame(gameEntity, playerName)
        gameRepository.save(gameEntity)
        notifySSE(gameEntity)

        return gameEntity
    }

    @Transactional
    fun closeGame(id: Long) {
        val gameEntity = gameRepository.getReferenceById(id)
        gameEntity.state = GameState.DONE
        gameRepository.save(gameEntity)

        val usersIds = gameEntity.players.mapNotNull { it.user?.id };
        sseComponent.sendGameThroughSSE(usersIds, gameEntity)
    }

    private fun checkHasNoOnGoingGame() {
        check (getOnGoingGame() == null) {
            "Game already exists"
        }
    }

    private fun addPlayerToGame(gameEntity: GameEntity, playerName: String): PlayerEntity {
        val user = userRepository.getReferenceById(CustomUser.get().userId)
        val playerEntity = PlayerEntity()
        playerEntity.user = user
        playerEntity.game = gameEntity
        playerEntity.name = playerName
        gameEntity.players.add(playerEntity)
        return playerEntity
    }

    private fun notifySSE(gameEntity: GameEntity) {
        sseComponent.sendGameThroughSSE(gameEntity.players.mapNotNull { it.user?.id }, gameEntity)
    }
}

@RestController
@RequestMapping("/api/games")
class GameController(
    private val gameService: GameService, private val sseComponent: SSEComponent
) {

    @GetMapping("/me")
    fun getAllMine(): List<GameEntity> {
        return this.gameService.getAllMine()
    }

    @GetMapping("/me/current")
    fun getCurrent(): GameEntity? {
        return this.gameService.getOnGoingGame()
    }

    @PutMapping
    fun join(@RequestParam id: String, @RequestBody playerName: String): GameEntity {
        return this.gameService.join(id, playerName)
    }

    @PostMapping
    fun create(@RequestBody playerName: String): GameEntity {
        return this.gameService.create(playerName)
    }

    @DeleteMapping("/{gameId}")
    fun close(@PathVariable gameId: Long) {
        this.gameService.closeGame(gameId)
    }

    @GetMapping(path = ["/sse"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(): SseEmitter? {
        return sseComponent.addSse()
    }
}
