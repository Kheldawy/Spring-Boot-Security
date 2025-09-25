package com.example.librarysystem.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import com.example.librarysystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity // Aktiverar Spring Securitys webbskydd
@EnableMethodSecurity // Tillåter säkerhet på metodnivå
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserRepository userRepository; // Hämta användare från databasen

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Krypterar lösenord
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Behörighetsregler för olika endpoints
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/logout","/csrf-token","/books").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/loans/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/loans/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/loans/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/authors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/authors/**").hasRole("ADMIN")
                        .anyRequest().authenticated() // Alla andra anrop kräver inloggning
                )

                // Hantering av inloggning
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler())
                        .permitAll()
                )

                // Utloggning
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .permitAll()
                )

                // Hantering av säkerhetsundantag
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.warn("Access denied for user: {} on URL: {}",
                                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous",
                                    request.getRequestURI());
                            // Skickar 403 om åtkomst nekas
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Access denied: You do not have permission to perform this action\"}");
                        })

                        .authenticationEntryPoint((request, response, authException) -> {
                            logger.warn("Authentication required for URL: {}", request.getRequestURI());
                            // Skickar 401 om ej inloggad
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Authentication failed: Please provide valid credentials\"}");
                        })
                )
                // CSRF-skydd
                .csrf(csrf -> csrf
                        .csrfTokenRepository(new HttpSessionCsrfTokenRepository())
                        .ignoringRequestMatchers("/h2-console/**")
                        .requireCsrfProtectionMatcher(request -> {
                            String method = request.getMethod();
                            return "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
                        })

//                .csrf(csrf ->csrf.disable()
                )
                // Loggning av CSRF-token
                .addFilterAfter(new CsrfTokenLoggingFilter(), org.springframework.security.web.csrf.CsrfFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Hämta användare från databasen via UserRepository
        return username -> userRepository.findByEmailIgnoreCase(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole())
                        .build())
                .orElseThrow(() -> {
                    logger.error("User not found with email: {}", username);
                    return new UsernameNotFoundException("User not found with email: " + username);
                });
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        // Hantering av autentisering
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {

        // Logik vid lyckad inloggning
        return (request, response, authentication) -> {
            logger.info("Login successful for user: {}", authentication.getName());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Login successful\"}");
            response.setStatus(HttpServletResponse.SC_OK);
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {

        // Logik vid misslyckad inloggning
        return (request, response, exception) -> {
            logger.warn("Login failed for user: {} with error: {}",
                    request.getParameter("username"), exception.getMessage());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Login failed: " + exception.getMessage() + "\"}");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        };
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {

        // Logik vid lyckad utloggning
        return (request, response, authentication) -> {
            logger.info("Logout successful for user: {}",
                    authentication != null ? authentication.getName() : "anonymous");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Logout successful");
            response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(responseBody));
            response.setStatus(HttpServletResponse.SC_OK);
        };
    }

    // Filter för att logga CSRF-token för varje förfrågan
    public static class CsrfTokenLoggingFilter extends OncePerRequestFilter {
        private static final Logger csrfLogger = LoggerFactory.getLogger(CsrfTokenLoggingFilter.class);

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                csrfLogger.debug("CSRF token generated: {}", csrfToken.getToken());
                response.setHeader("X-CSRF-TOKEN", csrfToken.getToken());
            }
            filterChain.doFilter(request, response);
        }
    }
}
