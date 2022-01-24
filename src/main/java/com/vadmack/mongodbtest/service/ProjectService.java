package com.vadmack.mongodbtest.service;

import com.vadmack.mongodbtest.dto.ProjectDto;
import com.vadmack.mongodbtest.dto.ProjectNoIdDto;
import com.vadmack.mongodbtest.entity.Project;
import com.vadmack.mongodbtest.entity.User;
import com.vadmack.mongodbtest.exception.NotFoundException;
import com.vadmack.mongodbtest.repository.ProjectRepository;
import com.vadmack.mongodbtest.util.PageableService;
import com.vadmack.mongodbtest.util.SequenceGeneratorService;
import com.vadmack.mongodbtest.util.SortService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository repository;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final PageableService pageableService;
    private final SortService sortService;

    private final ModelMapper modelMapper = new ModelMapper();

    public List<ProjectDto> findList(
            @Nullable String namePart,
            @Nullable Integer pageNumber,
            @Nullable Integer pageSize,
            @Nullable String[] sortBy
    ) {
        if (namePart == null) {
            namePart = "";
        }
        pageableService.validateParams(pageNumber, pageSize);
        Sort sort = sortService.createSort(sortBy);
        if (pageNumber != null && pageSize != null) {
            if (sortBy == null) {
                sortBy = new String[1];
                sortBy[0] = "id:0";
            }
            return repository.findAllByNameLikeIgnoreCase(
                    namePart,
                    PageRequest.of(pageNumber, pageSize, sort))
                    .stream().map(this::entityToDto)
                    .collect(Collectors.toList());
        } else {
            return repository.findAllByNameLikeIgnoreCase(namePart, sort)
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
        project.setOwnerId(getUserId());
        repository.save(project);
    }

    public void update(Long id, ProjectNoIdDto projectNoIdDto) {
        Long ownerId = getById(id).getOwnerId();
        if (!ownerId.equals(getUserId())) {
            throw new AccessDeniedException("Only available to the owner of the project");
        }
        Project project = dtoToEntity(projectNoIdDto);
        project.setId(id);
        project.setOwnerId(ownerId);
        repository.save(project);
    }

    public void delete(Long id) {
        Project project = getById(id);
        if (!project.getOwnerId().equals(getUserId())) {
            throw new AccessDeniedException("Only available to the owner of the project");
        }
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

    private Long getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }
}
