package com.vadmack.mongodbtest.service;

import com.vadmack.mongodbtest.dto.ProjectDto;
import com.vadmack.mongodbtest.dto.ProjectNoIdDto;
import com.vadmack.mongodbtest.entity.Project;
import com.vadmack.mongodbtest.exception.NotFoundException;
import com.vadmack.mongodbtest.repository.ProjectRepository;
import com.vadmack.mongodbtest.util.PageableService;
import com.vadmack.mongodbtest.util.SequenceGeneratorService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final PageableService pageableService;

    private final ModelMapper modelMapper = new ModelMapper();

    public List<ProjectDto> findList(
            Optional<String> name,
            Optional<Integer> pageNumber,
            Optional<Integer> pageSize) {
        pageableService.validateParams(pageNumber, pageSize);
        if (pageNumber.isPresent()) {
            return repository.findAllByNameLikeIgnoreCase(
                    "*" + (name).orElse("") + "*",
                    PageRequest.of(pageNumber.get(), pageSize.get()))
                    .stream().map(this::entityToDto)
                    .collect(Collectors.toList());
        } else {
            return repository.findAllByNameLikeIgnoreCase(
                    "*" + (name).orElse("") + "*")
                    .stream().map(this::entityToDto)
                    .collect(Collectors.toList());
        }
    }

    public ProjectDto findById(Long id) {
        return entityToDto(getById(id));
    }

    public void create(ProjectNoIdDto projectNoIdDto) {
        Project project = dtoToEntity(projectNoIdDto);
        project.setId(sequenceGeneratorService.generateSequence(Project.SEQUENCE_NAME));
        repository.save(project);
    }

    public void update(Long id, ProjectNoIdDto projectNoIdDto) {
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
}
