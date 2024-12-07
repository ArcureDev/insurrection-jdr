package org.arcure.back.config;

import fr.arcure.uniting.configuration.security.CustomUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.aopalliance.intercept.MethodInvocation
import org.arcure.back.game.GameRepository
import org.arcure.back.player.PlayerRepository
import org.arcure.back.token.TokenRepository
import org.arcure.back.user.UserRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.expression.EvaluationContext
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.http.HttpStatus
import org.springframework.security.access.expression.SecurityExpressionRoot
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.HandlerExceptionResolver
import java.util.function.Supplier

val PASSWORD_ENCODER: PasswordEncoder = BCryptPasswordEncoder()

@Component
class CustomMethodSecurityExpressionHandler(private val applicationContext: ApplicationContext) :
    DefaultMethodSecurityExpressionHandler() {
    override fun createEvaluationContext(
        authentication: Supplier<Authentication>,
        mi: MethodInvocation
    ): EvaluationContext {
        val context = super.createEvaluationContext(authentication, mi) as StandardEvaluationContext
        val delegate = checkNotNull(context.rootObject.value as MethodSecurityExpressionOperations?)
        val root = CustomMethodSecurityExpressionRoot(delegate, applicationContext.getBean(SecurityService::class.java))
        context.setRootObject(root)
        return context
    }
}

class CustomMethodSecurityExpressionRoot(
    methodSecurityExpressionOperations: MethodSecurityExpressionOperations,
    private val securityService: SecurityService
) : SecurityExpressionRoot(methodSecurityExpressionOperations.authentication), MethodSecurityExpressionOperations {
    private var returnObject: Any? = null
    private var filterObject: Any? = null
    private var target: Any? = null

    fun isMyGame(gameId: Long): Boolean {
        return securityService.isMyGame(gameId)
    }

    fun isMyToken(gameId: Long, tokenId: Long): Boolean {
        return securityService.isMyToken(gameId, tokenId)
    }

    override fun setFilterObject(filterObject: Any) {
        this.filterObject = filterObject
    }

    override fun getFilterObject(): Any {
        return filterObject!!
    }

    override fun setReturnObject(returnObject: Any) {
        this.returnObject = returnObject
    }

    override fun getReturnObject(): Any {
        return returnObject!!
    }

    fun setThis(target: Any?) {
        this.target = target
    }

    override fun getThis(): Any {
        return target!!
    }
}

@Service
@Transactional(readOnly = true)
class SecurityService(
    private val playerRepository: PlayerRepository, private val tokenRepository: TokenRepository,
    private val gameRepository: GameRepository
) {

    fun isMyGame(gameId: Long): Boolean {
        return playerRepository.findByGameIdAndUserId(gameId, CustomUser.get().userId) != null
    }

    fun isMyToken(gameId: Long, tokenId: Long): Boolean {
        val game = gameRepository.getReferenceById(gameId)
        val myPlayer = game.players.find { it.user?.id ==  CustomUser.get().userId }

        check(myPlayer != null) {
            "You need to be a player of this game"
        }

        return tokenRepository.findByIdAndPlayerId(gameId, myPlayer.id!!) != null
    }

}

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val customUserDetailService: CustomUserDetailService, private val successHandler: AuthenticationSuccessHandler,
    private val failureHandler: AuthenticationFailureHandler, private val logoutSuccessHandl: LogoutSuccessHandler,
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
            }
            formLogin {
                loginPage = "/api/login"
                loginProcessingUrl = "/api/login"
                permitAll = true
                authenticationSuccessHandler = successHandler
                authenticationFailureHandler = failureHandler
            }
            logout {
                logoutUrl = "/api/logout"
                logoutSuccessHandler = logoutSuccessHandl
            }
        }
        return http.build()
    }

    @Bean
    fun authenticationManager(httpSecurity: HttpSecurity): AuthenticationManager {
        val auth = httpSecurity.getSharedObject(
            AuthenticationManagerBuilder::class.java
        )
        auth.userDetailsService<UserDetailsService>(customUserDetailService).passwordEncoder(PASSWORD_ENCODER)
        return CustomAuthenticationManager(auth.build())
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}

internal class CustomAuthenticationManager(private val authenticationManager: AuthenticationManager) : AuthenticationManager {
    @Throws(AuthenticationException::class)
    override fun authenticate(authentication: Authentication): Authentication {
        return authenticationManager.authenticate(authentication)
    }
}

@Service
@Transactional(readOnly = true)
class CustomUserDetailService(val userRepository: UserRepository) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): UserDetails {
        val user = userRepository.findByUsername(email)

        if (user == null ||  user.ban || user.username == null || user.password == null || user.id == null) {
            throw UsernameNotFoundException(String.format("User %s not found", email))
        } else {
            return CustomUser(user.username!!, user.password!!, user.id!!)
        }
    }
}

@Component
class AuthenticationSuccessHandler : SimpleUrlAuthenticationSuccessHandler() {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        clearAuthenticationAttributes(request)
    }
}

@Component
class AuthenticationFailureHandler(@param:Qualifier("handlerExceptionResolver") private val resolver: HandlerExceptionResolver) :
    SimpleUrlAuthenticationFailureHandler() {
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        resolver.resolveException(request, response, null, exception)
    }
}

@Component
class LogoutSuccessHandler(private val sseComponent: SSEComponent) : SimpleUrlLogoutSuccessHandler() {
    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        val principal = authentication?.principal
        check (principal is CustomUser) { "no principal" }
        sseComponent.removeSSE(principal.userId)
        response.status = HttpServletResponse.SC_OK
    }
}
