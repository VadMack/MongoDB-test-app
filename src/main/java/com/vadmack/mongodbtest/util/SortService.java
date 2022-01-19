package com.vadmack.mongodbtest.util;

import com.vadmack.mongodbtest.exception.ValidationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SortService {

    public Sort createSort(String[] sortBy) {
        Pattern pattern = Pattern.compile("(?<field>.+?):(?<value>[01]+?)");
        List<Sort.Order> orderList = new ArrayList<>();
        Arrays.stream(sortBy).forEach((entry) -> {
                    Matcher matcher;
                    matcher = pattern.matcher(entry);
                    if (matcher.find()) {
                        orderList.add(new Sort.Order(convertDirection(matcher.group("value")),
                                matcher.group("field")));
                    } else {
                        throw new ValidationException("Provided 'sort' parameter is invalid");
                    }
                }
        );
        return Sort.by(orderList);
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
