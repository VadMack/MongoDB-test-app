package com.vadmack.mongodbtest.util;

import com.vadmack.mongodbtest.exception.ValidationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class SortService {

    public Sort createSort(String[] sortBy) {
        return Sort.by(
                Arrays.stream(sortBy)
                        .map(sort -> sort.split(":", 2))
                        .map(array ->
                                new Sort.Order(convertDirection(array[1]), array[0])
                        ).collect(Collectors.toList())
        );
    }

    private Sort.Direction convertDirection(String direction) {
        if (direction.equals("0")) {
            return Sort.Direction.ASC;
        } else if (direction.equals("1")) {
            return Sort.Direction.DESC;
        } else {
            throw new ValidationException("Sort direction should be '0' or '1'");
        }
    }
}
