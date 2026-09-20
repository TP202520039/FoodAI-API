package com.tp.foodai.shared.interfaces.rest.middleware;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Writes the {@code Server-Timing} header on every response. The header must be set BEFORE
 * the body is committed, so the total here is "request received -> handler returned"; the
 * JSON serialisation tail is not included (a few ms). Registered via {@code @Component}
 * scanning (com.tp.foodai.FoodaiApplication) — no FilterRegistrationBean needed. Runs
 * independently of FirebaseAuthFilter (that one is wired into the Spring Security chain
 * via SecurityConfig.addFilterBefore; this is a plain servlet filter).
 */
@Component
@Order(1)
public class ServerTimingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        ServerTiming.reset();
        long t0 = System.nanoTime();
        try {
            chain.doFilter(req, res);
        } finally {
            double totalMs = (System.nanoTime() - t0) / 1e6;
            if (!res.isCommitted()) {
                res.setHeader("Server-Timing", ServerTiming.render(totalMs));
            }
            ServerTiming.reset();
        }
    }
}
