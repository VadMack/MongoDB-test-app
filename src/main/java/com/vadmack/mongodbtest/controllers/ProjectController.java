package com.vadmack.mongodbtest.controllers;

import com.vadmack.mongodbtest.dto.ProjectDto;
import com.vadmack.mongodbtest.entities.Project;
import com.vadmack.mongodbtest.repositories.ProjectRepository;
import com.vadmack.mongodbtest.util.SequenceGeneratorService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository repository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    private final ModelMapper modelMapper = new ModelMapper();

    @GetMapping
    public ResponseEntity<List<Project>> findAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Project> findById(@PathVariable(value = "id") Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity create(@RequestBody ProjectDto projectDto) {
        Project project = modelMapper.map(projectDto, Project.class);
        project.setId(sequenceGeneratorService.generateSequence(Project.SEQUENCE_NAME));
        repository.save(project);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity update(@PathVariable(value = "id") Long id,
                                 @RequestBody ProjectDto projectDto) {
        Optional<Project> optionalProject = repository.findById(id);
        if (optionalProject.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            Project updatedProject = modelMapper.map(projectDto, Project.class);
            updatedProject.setId(id);
            repository.save(updatedProject);
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity delete(@PathVariable(value = "id") Long id) {
        Optional<Project> optionalProject = repository.findById(id);
        if (optionalProject.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            repository.delete(optionalProject.get());
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
    }
}
