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
        return repository.findAll().stream().map(this::entityToDto)
                .collect(Collectors.toList());
    }

    public ProjectDto findById(Long id) {
        return repository.findById(id).map(this::entityToDto)
                .orElse(new ProjectDto());
    }

    public void create(ProjectNoIdDto projectNoIdDto) {
        Project project = dtoToProject(projectNoIdDto);
        project.setId(sequenceGeneratorService.generateSequence(Project.SEQUENCE_NAME));
        repository.save(project);
    }

    public void update(Long id, ProjectNoIdDto projectNoIdDto) {
        Project project = dtoToProject(projectNoIdDto);
        project.setId(id);
        repository.save(project);
    }

    public void delete(Long id) {
        Optional<Project> optionalProject = repository.findById(id);
        optionalProject.ifPresent(project -> repository.delete(project));
    }

    private ProjectDto entityToDto(Project project) {
        return modelMapper.map(project, ProjectDto.class);
    }

    private Project dtoToProject(ProjectNoIdDto dto) {
        return modelMapper.map(dto, Project.class);
    }
}
