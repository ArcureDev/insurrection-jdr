package org.arcure.back.token

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.arcure.uniting.configuration.security.CustomUser
import jakarta.persistence.*
import org.arcure.back.config.SSEComponent
import org.arcure.back.config.annotation.IsMyGame
import org.arcure.back.game.*
import org.arcure.back.player.PlayerEntity
import org.arcure.back.player.PlayerMapper
import org.arcure.back.player.SimplePlayerResponse
import org.springframework.context.annotation.Lazy
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random

val NB_SHARD_TOKENS_MAX: Int = 28
val NB_SHARD_TOKENS_START: Int = 8

@Entity
@Table(name = "token")
class TokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Enumerated(EnumType.STRING)
    var type: TokenType? = null,
    @ManyToOne
    var player: PlayerEntity? = null,
    @ManyToOne
    var owner: PlayerEntity? = null,
) {
    fun isMine(): Boolean {
        return player?.id != owner?.id
    }
}

enum class TokenType {
    INFLUENCE, SHARD
}

@Repository
interface TokenRepository : JpaRepository<TokenEntity, Long> {
}

class TokenResponse(
    val id: Long,
    val type: TokenType,
    val player: SimplePlayerResponse?,
    val owner: SimplePlayerResponse?,
)

@Component
class TokenMapper(@Lazy private val playerMapper: PlayerMapper) {
    fun toResponse(token: TokenEntity): TokenResponse {
        return TokenResponse(
            token.id!!,
            token.type!!,
            playerMapper.toSimpleResponse(token.player!!),
            playerMapper.toSimpleResponse(token.owner),
        )
    }
}

@Service
@Transactional(readOnly = true)
class TokenService(
    private val gameRepository: GameRepository, private val sseComponent: SSEComponent,
    private val gameMapper: GameMapper
) {

    @Transactional
    fun dealTokens(gameId: Long): GameResponse {
        val game = gameRepository.getReferenceById(gameId)
        val nbPlayers = game.players.size

        checkValidations(game, nbPlayers)

        val nbTokensPerPlayer = generateNumbers(nbPlayers)
        for (i in 0 until nbPlayers) {
            dealTokensToPlayer(game.players[i], nbTokensPerPlayer[i])
        }
        game.state = GameState.ON_GOING
        gameRepository.save(game)

        val myPlayer = game.players.find { it.user?.id == CustomUser.get().userId }

        check(myPlayer != null) {
            "My player doesn't exist"
        }

        val gameResponse = gameMapper.toResponse(game, myPlayer)

        sseComponent.notifySSE(gameResponse.players.map { it.userId }, gameResponse)

        return gameResponse
    }

    private fun dealTokensToPlayer(player: PlayerEntity, nbTokens: Int) {
        for (i in 0 until nbTokens) {
            val token = TokenEntity()
            token.player = player
            token.type = TokenType.SHARD
            player.playableTokens.add(token)
        }
    }

    private fun generateNumbers(nbPlayers: Int): List<Int> {
        val tokens = MutableList(nbPlayers) { 1 }
        var nbRemainingTokens = NB_SHARD_TOKENS_START - nbPlayers

        while (nbRemainingTokens > 0) {
            val randomPlayerPos = Random.nextInt(nbPlayers)
            if (tokens[randomPlayerPos] < 3) {
                tokens[randomPlayerPos]++
                nbRemainingTokens--
            }
        }

        return tokens
    }

    private fun checkValidations(game: GameEntity, nbPlayers: Int) {
        check(nbPlayers > 2) {
            "Must have at least 3 players"
        }

        check(nbPlayers <= 8) {
            "Must have at most 8 players"
        }

        check(game.state == GameState.START) {
            "Game state must be START"
        }
    }
}

@RestController
@RequestMapping("/api/games/{gameId}/tokens")
class TokenController(private val tokenService: TokenService) {

    @IsMyGame
    @GetMapping
    fun getTokens(@PathVariable("gameId") gameId: Long): GameResponse {
        return tokenService.dealTokens(gameId)
    }

}