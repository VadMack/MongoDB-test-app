package com.vadmack.mongodbtest.controller;

import com.vadmack.mongodbtest.dto.ProjectDto;
import com.vadmack.mongodbtest.dto.ProjectNoIdDto;
import com.vadmack.mongodbtest.entity.User;
import com.vadmack.mongodbtest.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService service;

    @GetMapping
    public ResponseEntity<List<ProjectDto>> findList(
            @RequestParam(value = "filterProjectName", required = false) String name,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(value = "sort", defaultValue = "id:0") String[] sortBy
    ) {
        return ResponseEntity.ok(service.findList(name, pageNumber, pageSize, sortBy));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ProjectDto> findById(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProjectNoIdDto projectNoIdDto,
                                    @AuthenticationPrincipal User user) {
        service.create(projectNoIdDto, user.getId());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("@projectService.userHasRights(#id, #user.id)")
    @PutMapping(value = "/{id}")
    public ResponseEntity<?> update(@PathVariable(value = "id") Long id,
                                    @Valid @RequestBody ProjectNoIdDto projectNoIdDto,
                                    @AuthenticationPrincipal User user) {
        service.update(id, projectNoIdDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("@projectService.userHasRights(#id, #user.id)")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable(value = "id") Long id,
                                    @AuthenticationPrincipal User user) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
