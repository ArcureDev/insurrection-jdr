package org.arcure.back.game

import fr.arcure.uniting.configuration.security.CustomUser
import jakarta.persistence.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import org.arcure.back.config.SSEComponent
import org.arcure.back.config.annotation.IsMyGame
import org.arcure.back.player.PlayerEntity
import org.arcure.back.player.PlayerMapper
import org.arcure.back.player.PlayerPayload
import org.arcure.back.player.PlayerResponse
import org.arcure.back.token.NB_SHARD_TOKENS_MAX
import org.arcure.back.token.NB_SHARD_TOKENS_START
import org.arcure.back.token.TokenEntity
import org.arcure.back.token.TokenType
import org.arcure.back.user.UserRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
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
    var nbAvailableShardTokens: Int = NB_SHARD_TOKENS_MAX - NB_SHARD_TOKENS_START,
    @OneToMany(mappedBy = "game", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var players: MutableList <PlayerEntity> = mutableListOf(),
    @Enumerated(EnumType.STRING)
    var state: GameState = GameState.START,
    var url: String = UUID.randomUUID().toString()
) {
}

enum class GameState {
    START, ON_GOING, DONE
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
        AND g.state <> 'DONE'
        LIMIT 1
    """, nativeQuery = true)
    fun findCurrent(userId: Long): GameEntity?

    fun findByUrl(url: String): GameEntity?
}

class GameResponse(
    val id: Long,
    val nbAvailableShardTokens: Int,
    val players: List <PlayerResponse> = mutableListOf(),
    val state: GameState,
    val url: String
)

@Component
class GameMapper(private val playerMapper: PlayerMapper) {

    fun toResponse(game: GameEntity, myPlayer: PlayerEntity): GameResponse {
        val players = game.players.map { playerMapper.toResponse(it, it.id == myPlayer.id) }

        return GameResponse(
            game.id!!,
            game.nbAvailableShardTokens,
            players,
            game.state,
            game.url
        )
    }
}

@Service
@Transactional(readOnly = true)
class GameService(
    private val gameRepository: GameRepository, private val userRepository: UserRepository,
    private val sseComponent: SSEComponent, private val gameMapper: GameMapper
) {

    @Transactional
    fun create(playerPayload: PlayerPayload): GameResponse {
        check (gameRepository.findCurrent(CustomUser.get().userId) == null) {
            "Game already exists"
        }

        val gameEntity = GameEntity()
        val myPlayer = getMyPlayer(gameEntity, playerPayload)
        gameEntity.players.add(myPlayer)
        gameRepository.save(gameEntity)

        return gameMapper.toResponse(gameEntity, myPlayer)
    }

    fun getAllMine(): List<GameEntity> {
        return gameRepository.findAllByPlayers(CustomUser.get().userId)
    }

    fun getCurrentGame(): GameResponse? {
        val gameEntity = gameRepository.findCurrent(CustomUser.get().userId);
        val myPlayer = gameEntity?.players?.find { it.user?.id == CustomUser.get().userId }

        check(gameEntity != null) {
            "No current game"
        }
        check(myPlayer != null) {
            "No current player"
        }
        return gameMapper.toResponse(gameEntity, myPlayer)
    }

    @Transactional
    fun join(url: String, playerPayload: PlayerPayload): GameResponse {
        val gameEntity = gameRepository.findByUrl(url)
        check (gameEntity != null) {
            "Game not exists"
        }

        val myPlayer = getMyPlayer(gameEntity, playerPayload)
        gameEntity.players.add(myPlayer)

        gameRepository.save(gameEntity)

        val gameResponse = gameMapper.toResponse(gameEntity, myPlayer);
        sseComponent.notifySSE(gameResponse.players.map { it.userId }, gameResponse)

        return gameResponse
    }

    @Transactional
    fun closeGame(id: Long) {
        val gameEntity = gameRepository.getReferenceById(id)
        gameEntity.state = GameState.DONE
        gameRepository.save(gameEntity)

        sseComponent.notifySSE(gameEntity.players.mapNotNull { it.user?.id }, null)
    }

    fun getMyPlayer(gameEntity: GameEntity, playerPayload: PlayerPayload): PlayerEntity {
        return gameEntity.players.find { it.user?.id == CustomUser.get().userId } ?: generateMyPlayer(gameEntity, playerPayload)
    }

    private fun generateMyPlayer(gameEntity: GameEntity, playerPayload: PlayerPayload): PlayerEntity {
        val user = userRepository.getReferenceById(CustomUser.get().userId)
        val playerEntity = PlayerEntity()
        playerEntity.user = user
        playerEntity.game = gameEntity
        playerEntity.name = playerPayload.name
        playerEntity.color = playerPayload.color

        playerEntity.myTokens = mutableListOf(
            TokenEntity(null, TokenType.INFLUENCE, playerEntity, playerEntity),
            TokenEntity(null, TokenType.INFLUENCE, playerEntity, playerEntity),
            TokenEntity(null, TokenType.INFLUENCE, playerEntity, playerEntity)
        )

        return playerEntity
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
    fun getCurrent(): GameResponse? {
        return this.gameService.getCurrentGame()
    }

    @PostMapping
    fun create(@RequestBody @Valid player: PlayerPayload): GameResponse {
        return this.gameService.create(player)
    }

    @PutMapping
    fun join(@RequestParam url: String, @RequestBody @Valid player: PlayerPayload): GameResponse {
        return this.gameService.join(url, player)
    }

    @IsMyGame
    @DeleteMapping("/{gameId}")
    fun close(@PathVariable gameId: Long) {
        this.gameService.closeGame(gameId)
    }

    @GetMapping(path = ["/sse"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun subscribe(): SseEmitter? {
        return sseComponent.addSse()
    }
}
