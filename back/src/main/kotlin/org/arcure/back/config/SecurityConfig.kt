package org.arcure.back.config;

import fr.arcure.uniting.configuration.security.CustomUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.arcure.back.user.UserRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
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

val PASSWORD_ENCODER: PasswordEncoder = BCryptPasswordEncoder()

@Service
@Transactional(readOnly = true)
class SecurityService {

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
class LogoutSuccessHandler : SimpleUrlLogoutSuccessHandler() {
    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        response.status = HttpServletResponse.SC_OK
    }
}
