package com.securityapp.gofundme.config;

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

    // Instance locale pour casser le cycle
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");

        User user = userRepository.findByEmail(email)
            .orElseGet(() -> createUser(oauth2User));

        org.springframework.security.core.userdetails.UserDetails userDetails = 
            org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name().replace("ROLE_", ""))
                .build();

        Authentication newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        setDefaultTargetUrl("/home");
        super.onAuthenticationSuccess(request, response, newAuth);
    }

//    private User createUser(OAuth2User oauth2User) {
//        User user = new User();
//        user.setEmail(oauth2User.getAttribute("email"));
//        user.setFirstName(oauth2User.getAttribute("given_name"));
//        user.setLastName(oauth2User.getAttribute("family_name"));
//        user.setRole(Role.ROLE_USER);
//        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
//        user.setEmailVerified(true);
//        return userRepository.save(user);
//    }
        private User createUser(OAuth2User oauth2User) {
        User user = new User();
        user.setEmail(oauth2User.getAttribute("email"));
        user.setFirstName(oauth2User.getAttribute("given_name"));
        user.setLastName(oauth2User.getAttribute("family_name"));
        user.setRole(Role.ROLE_USER);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setEmailVerified(true);
        
        // RÉCUPÉRATION PHOTO GOOGLE
        String picture = oauth2User.getAttribute("picture");
        if (picture != null && !picture.isEmpty()) {
            user.setProfileImageUrl(picture);
        }
        
        return userRepository.save(user);
    }
}