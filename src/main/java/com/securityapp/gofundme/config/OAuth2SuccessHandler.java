package com.securityapp.gofundme.config;

import com.securityapp.gofundme.model.AuthProvider;
import com.securityapp.gofundme.model.Role;
import com.securityapp.gofundme.model.User;
import com.securityapp.gofundme.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateGoogleInfo(existingUser, oauth2User))
                .orElseGet(() -> createGoogleUser(oauth2User));

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .roles(user.getRole().name().replace("ROLE_", ""))
                        .build();

        Authentication newAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(newAuth);

        if (!user.isProfileCompleted()) {
            response.sendRedirect("/complete-profile");
            return;
        }

        if (user.getRole() == Role.ROLE_ADMIN) {
            response.sendRedirect("/admin/dashboard");
            return;
        }

        response.sendRedirect("/home");
    }

    private User createGoogleUser(OAuth2User oauth2User) {
        User user = new User();

        user.setEmail(oauth2User.getAttribute("email"));
        user.setFirstName(getOrDefault(oauth2User.getAttribute("given_name"), "Utilisateur"));
        user.setLastName(getOrDefault(oauth2User.getAttribute("family_name"), "Google"));

        user.setRole(Role.ROLE_USER);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setEmailVerified(true);

        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId(oauth2User.getAttribute("sub"));
        user.setProfileCompleted(false);

        String picture = oauth2User.getAttribute("picture");
        if (picture != null && !picture.isBlank()) {
            user.setProfileImageUrl(picture);
        }

        return userRepository.save(user);
    }

    private User updateGoogleInfo(User user, OAuth2User oauth2User) {
        boolean changed = false;

        if (user.getProvider() == null) {
            user.setProvider(AuthProvider.GOOGLE);
            changed = true;
        }

        if (user.getProviderId() == null || user.getProviderId().isBlank()) {
            user.setProviderId(oauth2User.getAttribute("sub"));
            changed = true;
        }

        String picture = oauth2User.getAttribute("picture");
        if (picture != null && !picture.isBlank()
                && (user.getProfileImageUrl() == null || user.getProfileImageUrl().isBlank())) {
            user.setProfileImageUrl(picture);
            changed = true;
        }

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            changed = true;
        }

        if (changed) {
            return userRepository.save(user);
        }

        return user;
    }

    private String getOrDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value;
    }
}
