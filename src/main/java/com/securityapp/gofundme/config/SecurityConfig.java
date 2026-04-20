package com.securityapp.gofundme.config;

import com.securityapp.gofundme.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            
            .authorizeHttpRequests(auth -> auth
                // ========== PAGES PUBLIQUES ==========
                .requestMatchers("/", "/home", "/register", "/login", "/verify").permitAll()
                .requestMatchers("/forgot-password", "/reset-password").permitAll() // ← AJOUTÉ
                .requestMatchers("/explore", "/campaign/details/**").permitAll()      // ← AJOUTÉ (explore public)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/oauth2/**").permitAll() // ← uploads ajouté
                .requestMatchers("/api/campaigns/**").permitAll() // ← si vous voulez que les likes fonctionnent sans login, sinon retirez
                .requestMatchers("/creator/**").permitAll()           // Profil public
                .requestMatchers("/api/campaigns/*/comments").permitAll() // Lecture commentaires
                .requestMatchers("/uploads/**").permitAll()           // Images uploadées
                // Pages protégées
                .requestMatchers("/profile/settings").authenticated()
                .requestMatchers("/campaign/create", "/campaign/save", "/campaign/edit/**", "/campaign/update/**", "/campaign/delete/**").authenticated()
                .requestMatchers("/dashboard", "/profile/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/campaign/*/donate", "/donation/**").authenticated()
                .requestMatchers("/api/payments/create-intent").authenticated()
                .anyRequest().authenticated()
            )
            
            // Formulaire classique
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            
            // OAuth2 Google
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(oAuth2SuccessHandler)
            )
            
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired=true")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}