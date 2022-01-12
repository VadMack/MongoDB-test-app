package com.vadmack.mongodbtest.service;

import com.vadmack.mongodbtest.dto.ProjectDto;
import com.vadmack.mongodbtest.dto.ProjectNoIdDto;
import com.vadmack.mongodbtest.entity.Project;
import com.vadmack.mongodbtest.exception.NotFoundException;
import com.vadmack.mongodbtest.exception.ValidationException;
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
                .orElseThrow(() -> new NotFoundException(String.format("Project with id=%d not found", id)));
    }

    public void create(ProjectNoIdDto projectNoIdDto) {
        validateDto(projectNoIdDto);
        Project project = modelMapper.map(projectNoIdDto, Project.class);
        project.setId(sequenceGeneratorService.generateSequence(Project.SEQUENCE_NAME));
        repository.save(project);
    }

    public void update(Long id, ProjectNoIdDto projectNoIdDto) {
        validateDto(projectNoIdDto);
        repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Project with id=%d not found", id)));
        Project updatedProject = modelMapper.map(projectNoIdDto, Project.class);
        updatedProject.setId(id);
        repository.save(updatedProject);
    }

    public void delete(Long id) {
        repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Project with id=%d not found", id)));
        Optional<Project> optionalProject = repository.findById(id);
        optionalProject.ifPresent(project -> repository.delete(project));
    }

    private void validateDto(ProjectNoIdDto dto) {
        if (dto.getName() == null ||
        dto.getName().isEmpty()){
            throw new ValidationException("The property 'name' is not defined");
        }
    }
}
