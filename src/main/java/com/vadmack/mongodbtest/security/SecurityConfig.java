package com.vadmack.mongodbtest.security;

import com.vadmack.mongodbtest.entity.Role;
import com.vadmack.mongodbtest.entity.User;
import com.vadmack.mongodbtest.repository.UserRepository;
import com.vadmack.mongodbtest.util.SequenceGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserRepository userRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Override
    public void configure(AuthenticationManagerBuilder builder)
            throws Exception {
        builder.userDetailsService(username -> {
            if (userRepository.findAll().isEmpty()) {
                User user = new User(
                        sequenceGeneratorService.generateSequence(User.SEQUENCE_NAME),
                        "user",
                        passwordEncoder().encode("user"),
                        Set.of(new Role(Role.ROLE_USER)),
                        true
                );
                User admin = new User(
                        sequenceGeneratorService.generateSequence(User.SEQUENCE_NAME),
                        "admin",
                        passwordEncoder().encode("admin"),
                        Set.of(new Role(Role.ROLE_ADMIN)),
                        true
                );
                userRepository.saveAll(List.of(user, admin));
            }
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("User: %s, not found", username)));
        });
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Enable CORS and disable CSRF
        http = http.cors().and().csrf().disable();

        // Set session management to stateless
        http = http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and();

        http.httpBasic();

        http.authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/**").hasAnyRole("USER", "ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("USER", "ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/**").hasAnyRole("USER", "ADMIN");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
