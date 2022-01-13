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
        return entityToDto(getById(id));
    }

    public void create(ProjectNoIdDto projectNoIdDto) {
        validateDto(projectNoIdDto);
        Project project = dtoToEntity(projectNoIdDto);
        project.setId(sequenceGeneratorService.generateSequence(Project.SEQUENCE_NAME));
        repository.save(project);
    }

    public void update(Long id, ProjectNoIdDto projectNoIdDto) {
        validateDto(projectNoIdDto);
        getById(id);
        Project project = dtoToEntity(projectNoIdDto);
        project.setId(id);
        repository.save(project);
    }

    public void delete(Long id) {
        Project project = getById(id);
        repository.delete(project);
    }

    private ProjectDto entityToDto(Project project) {
        return modelMapper.map(project, ProjectDto.class);
    }

    private Project dtoToEntity(ProjectNoIdDto dto) {
        return modelMapper.map(dto, Project.class);
    }

    private Project getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Project with id=%d not found", id)));
    }

    private void validateDto(ProjectNoIdDto dto) {
        if (dto.getName() == null ||
        dto.getName().isEmpty()){
            throw new ValidationException("The property 'name' is not defined");
        }
    }
}
