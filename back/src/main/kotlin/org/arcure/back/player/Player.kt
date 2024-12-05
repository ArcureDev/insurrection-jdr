package org.arcure.back.player

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.arcure.back.game.GameEntity
import org.arcure.back.game.GameRepository
import org.arcure.back.token.TokenEntity
import org.arcure.back.token.TokenMapper
import org.arcure.back.token.TokenResponse
import org.arcure.back.user.UserEntity
import org.springframework.context.annotation.Lazy
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
    @OneToMany(mappedBy = "player", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var playableTokens: MutableList<TokenEntity> = mutableListOf(),
    @OneToMany(mappedBy = "owner", cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    var myTokens: MutableList<TokenEntity> = mutableListOf()
) {
}

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
class PlayerService(private val playerRepository: PlayerRepository, private val gameRepository: GameRepository) {
}

@RestController
@RequestMapping("/api/games/{gameId}/players")
class PlayerController(val playerService: PlayerService) {

}