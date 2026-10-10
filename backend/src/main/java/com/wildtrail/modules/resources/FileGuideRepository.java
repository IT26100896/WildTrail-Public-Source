package com.wildtrail.modules.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wildtrail.exception.ApiException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/** Authoritative text-file storage. Atomic replacement prevents partially written records. */
@Repository
public class FileGuideRepository {
    private final Path file;
    private final ObjectMapper mapper;
    public FileGuideRepository(ObjectMapper mapper, @Value("${wildtrail.data-dir}") String directory) {
        this.mapper=mapper.copy().configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        this.file=Path.of(directory).toAbsolutePath().resolve("resources.txt");
    }
    public synchronized List<Guide> findAll() {
        try {
            if(!Files.exists(file)) return List.of();
            List<Guide> rows=mapper.readValue(Files.readString(file,StandardCharsets.UTF_8),new TypeReference<List<Guide>>(){});
            return rows.stream().map(g -> Guide.create(g.getId(),g.getFullName(),g.getPhone(),g.getLicenseNumber(),g.getSpecialty(),g.isAvailable())).toList();
        } catch(IOException e){throw new IllegalStateException("Unable to read guide records. Existing data was preserved.",e);}
    }
    public synchronized Guide save(int id,Guide input) {
        List<Guide> rows=new ArrayList<>(findAll());
        if(id>0 && rows.stream().noneMatch(g->g.getId()==id)) throw ApiException.notFound("Guide was not found");
        if(rows.stream().anyMatch(g->g.getId()!=id && g.getLicenseNumber().equalsIgnoreCase(input.getLicenseNumber()))) throw ApiException.conflict("Guide licence is already registered");
        int next=id>0 ? id : rows.stream().mapToInt(Guide::getId).max().orElse(0)+1;
        Guide saved=Guide.create(next,input.getFullName(),input.getPhone(),input.getLicenseNumber(),input.getSpecialty(),input.isAvailable());
        rows.removeIf(g->g.getId()==id); rows.add(saved); write(rows); return saved;
    }
    public synchronized void delete(int id) {
        List<Guide> rows=new ArrayList<>(findAll());
        if(!rows.removeIf(g->g.getId()==id)) throw ApiException.notFound("Guide was not found"); write(rows);
    }
    private void write(List<Guide> rows) {
        Path temp=null;
        try {
            Files.createDirectories(file.getParent());
            temp=Files.createTempFile(file.getParent(),"resources-",".tmp");
            Files.writeString(temp,mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rows),StandardCharsets.UTF_8);
            try(FileChannel channel=FileChannel.open(temp,StandardOpenOption.WRITE)){channel.force(true);}
            Files.move(temp,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException e){throw new IllegalStateException("Unable to save guide records",e);}
        finally {if(temp!=null) try{Files.deleteIfExists(temp);}catch(IOException ignored){}}
    }
}
