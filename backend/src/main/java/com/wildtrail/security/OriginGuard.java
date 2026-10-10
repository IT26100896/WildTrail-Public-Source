package com.wildtrail.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Reject cross-site mutations; same-origin browser and origin-free command-line clients work. */
@Component
public class OriginGuard extends OncePerRequestFilter {
    private final Set<String> origins;
    public OriginGuard(@Value("${APP_ORIGINS:http://localhost:3000,http://127.0.0.1:3000}") String configured) { origins=Set.of(configured.split(",")); }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String origin=req.getHeader("Origin");
        if (!Set.of("GET","HEAD","OPTIONS").contains(req.getMethod()) && origin!=null && !origins.contains(origin)) {
            res.setStatus(403); res.setContentType("application/json"); res.getWriter().write("{\"error\":\"Request origin is not allowed\"}"); return;
        }
        chain.doFilter(req,res);
    }
}
