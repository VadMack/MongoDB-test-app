package com.vadmack.mongodbtest.service;

import com.vadmack.mongodbtest.dto.UserDto;
import com.vadmack.mongodbtest.dto.UserNoIdDto;
import com.vadmack.mongodbtest.entity.Project;
import com.vadmack.mongodbtest.entity.Role;
import com.vadmack.mongodbtest.entity.User;
import com.vadmack.mongodbtest.exception.NotFoundException;
import com.vadmack.mongodbtest.exception.ValidationException;
import com.vadmack.mongodbtest.repository.UserRepository;
import com.vadmack.mongodbtest.util.PageableService;
import com.vadmack.mongodbtest.util.SequenceGeneratorService;
import com.vadmack.mongodbtest.util.SortService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final ModelMapper modelMapper = new ModelMapper();
    private final SequenceGeneratorService sequenceGeneratorService;
    private final PageableService pageableService;
    private final SortService sortService;
    private final ProjectService projectService;
    private final PasswordEncoder passwordEncoder;

    public List<UserDto> findList(
            @Nullable String usernamePart,
            @Nullable Integer pageNumber,
            @Nullable Integer pageSize,
            @Nullable String[] sortBy
    ) {
        if (usernamePart == null) {
            usernamePart = "";
        }
        pageableService.validateParams(pageNumber, pageSize);
        Sort sort = sortService.createSort(sortBy);
        if (pageNumber != null && pageSize != null) {
            if (sortBy == null) {
                sortBy = new String[1];
                sortBy[0] = "id:0";
            }
            return repository.findAllByUsernameLikeIgnoreCase(
                    usernamePart,
                    PageRequest.of(pageNumber, pageSize, sort))
                    .stream().map(this::entityToDto)
                    .collect(Collectors.toList());
        } else {
            return repository.findAllByUsernameLikeIgnoreCase(usernamePart, sort)
                    .stream().map(this::entityToDto)
                    .collect(Collectors.toList());
        }
    }

    public UserDto findById(Long id) {
        return entityToDto(getById(id));
    }

    public void create(UserNoIdDto userNoIdDto) {
        validateAuthorities(userNoIdDto);
        User user = dtoToEntity(userNoIdDto);
        user.setId(sequenceGeneratorService.generateSequence(User.SEQUENCE_NAME));
        user.setPassword(passwordEncoder.encode(userNoIdDto.getPassword()));
        repository.save(user);
    }

    public void update(Long id, UserNoIdDto userNoIdDto) {
        validateAuthorities(userNoIdDto);
        getById(id);
        User user = dtoToEntity(userNoIdDto);
        user.setId(id);
        user.setPassword(passwordEncoder.encode(userNoIdDto.getPassword()));
        repository.save(user);
    }

    public void delete(Long id) {
        User user = getById(id);
        List<Project> projects = projectService.findAllByOwnerId(id);
        projects.forEach(project -> projectService.delete(project.getId()));
        repository.delete(user);
    }

    private void validateAuthorities(UserNoIdDto userNoIdDto) {
        userNoIdDto.getAuthorities().forEach(role -> {
            String strRole = role.getAuthority();
            if (!strRole.equals(Role.ROLE_USER) &&
                    (!strRole.equals(Role.ROLE_ADMIN))) {
                throw new ValidationException("Each authority should be one of the following: " +
                        Role.getAvailableRolesAsString());
            }
        });
    }

    private User dtoToEntity(UserNoIdDto dto) {
        return modelMapper.map(dto, User.class);
    }

    private UserDto entityToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    private User getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("User with id=%d not found", id)));
    }
}
