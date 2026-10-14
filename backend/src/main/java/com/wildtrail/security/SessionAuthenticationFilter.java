package com.wildtrail.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SessionAuthenticationFilter
extends OncePerRequestFilter {
    private final SessionCookieService cookies;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    public SessionAuthenticationFilter(SessionCookieService cookies, org.springframework.jdbc.core.JdbcTemplate jdbc) {
        this.cookies = cookies;
        this.jdbc = jdbc;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        // Use one consistent identity across the website and admin console.
        for (String role : List.of("ROLE_ADMIN", "ROLE_CUSTOMER")) {
            SessionUser user = this.cookies.read(request, role);
            if (user == null) continue;
            var rows = jdbc.queryForList("SELECT email,full_name AS \"name\" FROM users WHERE id=? AND role=?", user.uid(), role);
            if (rows.isEmpty()) continue;
            user=new SessionUser(user.uid(),rows.get(0).get("email").toString(),rows.get(0).get("name").toString(),role);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken((Object)user, null, List.of(new SimpleGrantedAuthority(user.role())));
            SecurityContextHolder.getContext().setAuthentication((Authentication)authentication);
            break;
        }
        chain.doFilter((ServletRequest)request, (ServletResponse)response);
    }
}
