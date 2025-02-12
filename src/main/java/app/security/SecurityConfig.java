package app.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final UserDetailsServiceImpl customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .userDetailsService(customUserDetailsService)
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests
                            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                            .requestMatchers("/", "/users/login", "/users/register", "/about").permitAll()
                            .requestMatchers("/api/comments/**").authenticated()
                            .requestMatchers("/admin/**").hasRole("ADMIN") // this is for only the admin can access the admin pages
                            .anyRequest().authenticated();
                })
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/users/login")
                                .successHandler(customAuthenticationSuccessHandler)
                                .failureHandler(customAuthenticationFailureHandler)
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .permitAll())
                .logout(logout ->
                        logout
                                .logoutUrl("/users/logout")
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true))
                .build();
    }
}