package org.arcure.back.player

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.arcure.back.game.GameEntity
import org.arcure.back.token.TokenEntity
import org.arcure.back.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
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
    val nbRedFlag: Int = 0,
    val nbBlackFlag: Int = 0,
    @JsonIgnore
    @ManyToOne
    var game: GameEntity? = null,
    @ManyToOne
    var user: UserEntity? = null,
    @OneToMany(mappedBy = "player")
    val playableTokens: MutableList<TokenEntity> = mutableListOf(),
    @OneToMany(mappedBy = "owner")
    val myTokens: MutableList<TokenEntity> = mutableListOf()
) {
}

@Repository
interface PlayerRepository : JpaRepository<PlayerEntity, Long> {
    fun findByGameIdAndUserId(gameId: Long, userId: Long): PlayerEntity?
}

@Service
@Transactional(readOnly = true)
class PlayerService(private val playerRepository: PlayerRepository) {


}

@RestController
@RequestMapping("/api/games/{gameId}/players")
class PlayerController(val playerService: PlayerService) {

}