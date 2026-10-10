package com.wildtrail.controller;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wildtrail.exception.ApiException;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
/** Readable snapshots demonstrate file write/read. SQL remains authoritative. */
@RestController @RequestMapping("/api/admin/files") @PreAuthorize("hasRole('ADMIN')")
public class FileReportController {
    private static final Map<String,String> QUERIES=Map.of("users","SELECT id,email,full_name,role FROM users","packages","SELECT * FROM safari_packages","bookings","SELECT * FROM bookings","vehicles","SELECT * FROM vehicles","payments","SELECT * FROM payments","reviews","SELECT * FROM reviews");
    private final JdbcTemplate jdbc;private final ObjectMapper mapper;private final Path directory;
    public FileReportController(JdbcTemplate jdbc,ObjectMapper mapper,@Value("${wildtrail.data-dir}")String dir){this.jdbc=jdbc;this.mapper=mapper;directory=Path.of(dir).resolve("reports");}
    @PostMapping("/{module}")public Map<String,Object> write(@PathVariable String module)throws java.io.IOException{
        String query=QUERIES.get(module);if(query==null)throw ApiException.badRequest("Unknown module");
        var rows=jdbc.queryForList(query);Files.createDirectories(directory);Path temp=Files.createTempFile(directory,module,".tmp");
        try{Files.writeString(temp,mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rows));Files.move(temp,path(module),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}finally{Files.deleteIfExists(temp);}return Map.of("file",module+".txt","records",rows.size());
    }
    @GetMapping("/{module}")public List<Map<String,Object>> read(@PathVariable String module)throws java.io.IOException{Path file=path(module);if(!Files.exists(file))throw ApiException.notFound("Create a report first");return mapper.readValue(Files.readString(file),new TypeReference<List<Map<String,Object>>>(){});}
    private Path path(String module){if(!QUERIES.containsKey(module))throw ApiException.badRequest("Unknown module");return directory.resolve(module+".txt");}
}
