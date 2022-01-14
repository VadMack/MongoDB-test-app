package com.vadmack.mongodbtest.util;

import com.vadmack.mongodbtest.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PageableService {

    public void validateParams(Optional<Integer> pageNumber, Optional<Integer> pageSize) {
        if (pageNumber.isPresent() ^ pageSize.isPresent()) {
            throw new ValidationException("Parameters \"pageNumber\" and \"pageSize\" should be set both or both empty");
        }
    }
}
