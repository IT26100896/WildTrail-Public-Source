package com.wildtrail.modules.resources;
import java.util.*;
import org.springframework.web.bind.annotation.*;
/** Public team cards exclude contact numbers and licence identifiers. */
@RestController
public class TeamController {
    private final FileGuideRepository repository;
    public TeamController(FileGuideRepository repository){this.repository=repository;}
    @GetMapping("/api/team")public List<Map<String,String>> team(){return repository.findAll().stream().filter(Guide::isAvailable).map(g->Map.of("name",g.getFullName(),"role",g.getAssignmentRole())).toList();}
}
