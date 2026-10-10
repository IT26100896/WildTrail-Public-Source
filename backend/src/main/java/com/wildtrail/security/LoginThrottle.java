package com.wildtrail.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Ten failed sign-ins per remote address per minute, bounded in-memory storage. */
@Component
public class LoginThrottle extends OncePerRequestFilter {
    private record Failures(long until,int count){}
    private final Map<String,Failures> failures=new HashMap<>();
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
        boolean login=req.getMethod().equals("POST")&&Set.of("/api/auth/login","/api/auth/admin-login").contains(req.getRequestURI());
        if(!login){chain.doFilter(req,res);return;}
        String key=req.getRemoteAddr();long now=System.currentTimeMillis();
        synchronized(failures){failures.entrySet().removeIf(e->e.getValue().until<now);var entry=failures.get(key);if(entry!=null&&entry.count>=10){res.setStatus(429);res.setHeader("Retry-After","60");res.setContentType("application/json");res.getWriter().write("{\"error\":\"Too many failed sign-ins. Please wait one minute.\"}");return;}}
        chain.doFilter(req,res);
        synchronized(failures){if(res.getStatus()==401){if(failures.size()>=10000)failures.clear();var previous=failures.get(key);failures.put(key,new Failures(previous==null?now+60000:previous.until,previous==null?1:previous.count+1));}else if(res.getStatus()<400)failures.remove(key);}
    }
}
