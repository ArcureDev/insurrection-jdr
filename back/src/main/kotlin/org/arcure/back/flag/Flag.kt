package org.arcure.back.flag

import jakarta.persistence.*
import org.arcure.back.game.GameRepository
import org.arcure.back.game.GameResponse
import org.arcure.back.game.GameService
import org.arcure.back.getMyPlayer
import org.arcure.back.player.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Entity
@Table(name = "flag")
class FlagEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne
    var player: PlayerEntity? = null,
    @Enumerated(EnumType.STRING)
    var color: FlagColor? = null,
    var date: LocalDateTime? = null,
)

enum class FlagColor { RED, BLACK }

@Repository
interface FlagRepository : JpaRepository<FlagEntity, Long> {}

class FlagResponse(
    id: Long? = null,
    playerResponse: SimplePlayerResponse? = null,
    color: FlagColor? = null,
    date: LocalDateTime? = null,
)

@Component
class FlagMapper(private val playerMapper: PlayerMapper) {

    fun toResponse(flagEntity: FlagEntity): FlagResponse {
        return FlagResponse(
            flagEntity.id,
            playerMapper.toSimpleResponse(flagEntity.player),
            flagEntity.color,
            flagEntity.date
        )
    }
}

@Service
@Transactional(readOnly = true)
class FlagService(private val gameRepository: GameRepository, private val playerRepository: PlayerRepository) {

    @Transactional
    fun create(gameId: Long, color: FlagColor) {
        val game = gameRepository.getReferenceById(gameId)
        val myPlayer = getMyPlayer(game)
        val flag = FlagEntity()
        flag.player = myPlayer
        flag.color = color
        flag.date = LocalDateTime.now()
        myPlayer.flags.add(flag)

        playerRepository.save(myPlayer)
    }
}

@RestController
@RequestMapping("/api/games/{gameId}/flags")
class FlagController(private val flagService: FlagService, private val gameService: GameService) {

    @PostMapping
    fun createFlag(@PathVariable("gameId") gameId: Long, @RequestBody flagColor: FlagColor): GameResponse {
        flagService.create(gameId, flagColor)
        return gameService.getCurrentGameAndNotifyOthers()
    }
}