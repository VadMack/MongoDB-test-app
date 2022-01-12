package com.vadmack.mongodbtest.service;

import com.vadmack.mongodbtest.dto.ProjectDto;
import com.vadmack.mongodbtest.entity.Project;
import com.vadmack.mongodbtest.repository.ProjectRepository;
import com.vadmack.mongodbtest.util.SequenceGeneratorService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    ProjectRepository repository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    private final ModelMapper modelMapper = new ModelMapper();

    public List<Project> findAll() {
        return repository.findAll();
    }

    public Project findById(Long id) {
        return repository.findById(id).orElse(new Project());
    }

    public void create(ProjectDto projectDto) {
        Project project = modelMapper.map(projectDto, Project.class);
        project.setId(sequenceGeneratorService.generateSequence(Project.SEQUENCE_NAME));
        repository.save(project);
    }

    public void update(Long id, ProjectDto projectDto) {
        Project updatedProject = modelMapper.map(projectDto, Project.class);
        updatedProject.setId(id);
        repository.save(updatedProject);
    }

    public void delete(Long id) {
        Optional<Project> optionalProject = repository.findById(id);
        optionalProject.ifPresent(project -> repository.delete(project));
    }
}
