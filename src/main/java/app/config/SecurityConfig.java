package app.config;

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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests
                            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                            .requestMatchers("/", "/users/login", "/users/register").permitAll()
                            .anyRequest().authenticated();
                })
                .formLogin(formLogin ->
                        formLogin
                                .loginPage("/users/login")
                                .failureHandler(customAuthenticationFailureHandler)
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .defaultSuccessUrl("/home", true)
                                .permitAll())
                .logout(logout ->
                        logout
                                .logoutUrl("/users/logout")
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true))
                .build();
    }
}
