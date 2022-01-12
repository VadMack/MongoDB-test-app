package com.vadmack.mongodbtest.service;

import com.vadmack.mongodbtest.dto.ProjectDto;
import com.vadmack.mongodbtest.dto.ProjectNoIdDto;
import com.vadmack.mongodbtest.entity.Project;
import com.vadmack.mongodbtest.repository.ProjectRepository;
import com.vadmack.mongodbtest.util.SequenceGeneratorService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    ProjectRepository repository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    private final ModelMapper modelMapper = new ModelMapper();

    public List<ProjectDto> findAll() {
        return repository.findAll().stream().map(project -> modelMapper.map(project, ProjectDto.class))
                .collect(Collectors.toList());
    }

    public ProjectDto findById(Long id) {
        return repository.findById(id).map(project -> modelMapper.map(project, ProjectDto.class))
                .orElse(new ProjectDto());
    }

    public void create(ProjectNoIdDto projectNoIdDto) {
        Project project = modelMapper.map(projectNoIdDto, Project.class);
        project.setId(sequenceGeneratorService.generateSequence(Project.SEQUENCE_NAME));
        repository.save(project);
    }

    public void update(Long id, ProjectNoIdDto projectNoIdDto) {
        Project updatedProject = modelMapper.map(projectNoIdDto, Project.class);
        updatedProject.setId(id);
        repository.save(updatedProject);
    }

    public void delete(Long id) {
        Optional<Project> optionalProject = repository.findById(id);
        optionalProject.ifPresent(project -> repository.delete(project));
    }
}
