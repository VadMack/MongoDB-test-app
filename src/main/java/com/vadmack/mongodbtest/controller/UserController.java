package com.vadmack.mongodbtest.controller;

import com.vadmack.mongodbtest.dto.UserDto;
import com.vadmack.mongodbtest.dto.UserNoIdDto;
import com.vadmack.mongodbtest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<List<UserDto>> findList(
            @RequestParam(value = "filterUsername", required = false) String username,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(value = "sort", defaultValue = "id:0") String[] sortBy
    ) {
        return ResponseEntity.ok(service.findList(username, pageNumber, pageSize, sortBy));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable(value = "id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody UserNoIdDto userNoIdDto) {
        service.create(userNoIdDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<?> update(@PathVariable(value = "id") Long id,
                                    @Valid @RequestBody UserNoIdDto userNoIdDto) {
        service.update(id, userNoIdDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable(value = "id") Long id) {
        service.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
