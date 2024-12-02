package org.arcure.back.token

import jakarta.persistence.*
import org.arcure.back.player.PlayerEntity

@Entity
@Table(name = "token")
class TokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Enumerated(EnumType.STRING)
    var type: TokenType? = null,
    var color: String? = null,
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
    USER, SHARD
}