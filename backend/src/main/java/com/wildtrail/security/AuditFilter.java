package com.wildtrail.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
/** Append only metadata, never passwords, cookies, card data or request bodies. */
@Component
public class AuditFilter extends OncePerRequestFilter {
    private final Path file; private final ObjectMapper mapper;
    public AuditFilter(ObjectMapper mapper,@Value("${wildtrail.data-dir}")String dir){this.mapper=mapper;file=Path.of(dir).resolve("audit.txt");}
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
        try{chain.doFilter(req,res);}finally{if(!Set.of("GET","HEAD","OPTIONS").contains(req.getMethod())&&req.getRequestURI().startsWith("/api/")){
            var auth=org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();String actor=auth!=null&&auth.getPrincipal()instanceof SessionUser u?Integer.toString(u.uid()):"anonymous";
            append(Map.of("time",Instant.now().toString(),"method",req.getMethod(),"path",req.getRequestURI(),"actorId",actor,"status",res.getStatus()));
        }}
    }
    private synchronized void append(Map<String,Object> item){try{Files.createDirectories(file.getParent());Files.writeString(file,mapper.writeValueAsString(item)+System.lineSeparator(),StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(IOException e){logger.error("Unable to append audit record",e);}}
}
