package com.vadmack.mongodbtest.controller;

import com.vadmack.mongodbtest.dto.ProjectDto;
import com.vadmack.mongodbtest.dto.ProjectNoIdDto;
import com.vadmack.mongodbtest.log.LogRequests;
import com.vadmack.mongodbtest.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService service;

    @LogRequests
    @GetMapping
    public ResponseEntity<List<ProjectDto>> findList(
            @RequestParam(value = "filterProjectName", required = false) String name,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) {
        return ResponseEntity.ok(service.findList(name, pageNumber, pageSize));
    }

    @LogRequests
    @GetMapping(value = "/{id}")
    public ResponseEntity<ProjectDto> findById(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @LogRequests
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProjectNoIdDto projectNoIdDto) {
        service.create(projectNoIdDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @LogRequests
    @PutMapping(value = "/{id}")
    public ResponseEntity<?> update(@PathVariable(value = "id") Long id,
                                    @Valid @RequestBody ProjectNoIdDto projectNoIdDto) {
        service.update(id, projectNoIdDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @LogRequests
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable(value = "id") Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
