/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.securityapp.gofundme.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author Handy
 */
// Idempotence filter (éviter les double paiements)
@Component
public class IdempotencyFilter extends OncePerRequestFilter {
    
    private Set<String> processedKeys = ConcurrentHashMap.newKeySet();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain chain) throws ServletException, IOException {
        if (request.getRequestURI().contains("/api/payments")) {
            String idempotencyKey = request.getHeader("Idempotency-Key");
            if (idempotencyKey != null && !processedKeys.add(idempotencyKey)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}