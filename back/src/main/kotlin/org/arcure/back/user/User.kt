package org.arcure.back.user

import com.fasterxml.jackson.annotation.JsonIgnore
import fr.arcure.uniting.configuration.security.CustomUser
import jakarta.persistence.*
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.arcure.back.config.PASSWORD_ENCODER
import org.arcure.back.config.annotation.IsAuthenticated
import org.arcure.back.player.PlayerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@Entity(name = "user")
@Table(name = "user_account")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(unique = true)
    var username: String? = null,
    var password: String? = null,
    val ban: Boolean = false,
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
    var players: MutableList<PlayerEntity> = mutableListOf()
) {
}

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByUsername(username: String): UserEntity?
}

@Service
@Transactional(readOnly = true)
class UserService(val userRepository: UserRepository) {

    @Transactional
    fun create(credentials: Credentials): UserEntity {

        val userEntity = UserEntity()
        userEntity.username = credentials.username
        userEntity.password = PASSWORD_ENCODER.encode(credentials.password)
        return userRepository.save(userEntity)
    }

    fun getMyUser(): UserEntity {
        return userRepository.getReferenceById(CustomUser.get().userId)
    }

}

@RestController
@RequestMapping("/api/users")
class UserController(val userService: UserService) {

    @IsAuthenticated
    @GetMapping("/authentication")
    fun getConnectedUser(): UserEntity {
        return userService.getMyUser()
    }

    @PostMapping
    fun create(@RequestBody @Valid credentials: Credentials): UserEntity {
        return userService.create(credentials)
    }
}

data class Credentials(@NotBlank val username: String, @NotBlank val password: String){}