package org.arcure.back.player

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.arcure.back.config.SSEComponent
import org.arcure.back.game.*
import org.arcure.back.getMyPlayer
import org.arcure.back.token.TokenEntity
import org.arcure.back.token.TokenMapper
import org.arcure.back.token.TokenRepository
import org.arcure.back.token.TokenResponse
import org.arcure.back.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Entity
@Table(name = "player")
class PlayerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String? = null,
    var nbRedFlag: Int = 0,
    var nbBlackFlag: Int = 0,
    var color: String? = null,
    @Enumerated(EnumType.STRING)
    var role: PlayerRole? = null,
    @JsonIgnore
    @ManyToOne
    var game: GameEntity? = null,
    @ManyToOne
    var user: UserEntity? = null,
    @OneToMany(mappedBy = "player", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY, orphanRemoval = true)
    var playableTokens: MutableList<TokenEntity> = mutableListOf(),
    @OneToMany(mappedBy = "owner", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var myTokens: MutableList<TokenEntity> = mutableListOf()
)

enum class PlayerRole {
    POUVOIR, ORDRE, ECHO, PEUPLE, PAMPHLET, MOLOTOV, ECUSSON, ETOILE
}

@Repository
interface PlayerRepository : JpaRepository<PlayerEntity, Long> {
    fun findByGameIdAndUserId(gameId: Long, userId: Long): PlayerEntity?
}

class PlayerPayload(val name: String, val color: String)

class PlayerResponse(
    val id: Long,
    val name: String,
    val nbRedFlag: Int,
    val nbBlackFlag: Int,
    val color: String,
    val role: PlayerRole?,
    val userId: Long,
    val playableTokens: List<TokenResponse> = mutableListOf(),
    val myTokens: List<TokenResponse> = mutableListOf(),
    val isMe: Boolean
)

class SimplePlayerResponse(
    val id: Long,
    val name: String,
    val color: String,
)

@Component
class PlayerMapper(private val tokenMapper: TokenMapper) {
    fun toEntity(playerPayload: PlayerPayload): PlayerEntity {
        val player = PlayerEntity()
        player.name = playerPayload.name
        player.color = playerPayload.color
        return player
    }

    fun toSimpleResponse(player: PlayerEntity?): SimplePlayerResponse? {
        if (player == null) { return null}
        return SimplePlayerResponse(
            player.id!!,
            player.name!!,
            player.color!!
        )
    }

    fun toResponse(player: PlayerEntity, isMe: Boolean): PlayerResponse {
        return PlayerResponse(
            player.id!!,
            player.name!!,
            player.nbRedFlag,
            player.nbBlackFlag,
            player.color!!,
            player.role,
            player.user!!.id!!,
            player.playableTokens.map { tokenMapper.toResponse(it) },
            player.myTokens.map { tokenMapper.toResponse(it) },
            isMe
        )
    }
}

@Service
@Transactional(readOnly = true)
class PlayerService(
    private val playerRepository: PlayerRepository,
    private val gameRepository: GameRepository,
    private val tokenRepository: TokenRepository,
) {

    @Transactional
    fun giveToken(gameId: Long, playerId: Long) {
        val game = gameRepository.getReferenceById(gameId)
        val player = playerRepository.getReferenceById(playerId)
        val myPlayer = getMyPlayer(game)
        val token = getTokenToGive(player.id!!, myPlayer.id!!)

        token.player = player
        gameRepository.save(game)
    }

    /**
     * Take other player's token in priority, connected user's otherwise
     */
    private fun getTokenToGive(playerIdToGiveTo: Long, myPlayerId: Long): TokenEntity {
        val otherPlayerTokens = tokenRepository.findAllByPlayerIdAndOwnerId(myPlayerId, playerIdToGiveTo)

        val token = if (otherPlayerTokens.isNotEmpty()) otherPlayerTokens[0]
        else tokenRepository.findAllByPlayerIdAndOwnerId(myPlayerId, myPlayerId).first()

        return token
    }

}

@RestController
@RequestMapping("/api/games/{gameId}/players")
class PlayerController(private val playerService: PlayerService, private val gameService: GameService) {

    @PostMapping("/{playerId}/tokens")
    fun giveToken(
        @PathVariable("gameId") gameId: Long, @PathVariable("playerId") playerId: Long
    ): GameResponse {
        playerService.giveToken(gameId, playerId)
        return gameService.getCurrentGameAndNotifyOthers()
    }

}