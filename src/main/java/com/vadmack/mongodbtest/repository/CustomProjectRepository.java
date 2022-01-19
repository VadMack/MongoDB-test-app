package com.vadmack.mongodbtest.repository;

import com.vadmack.mongodbtest.entity.Project;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface CustomProjectRepository {

    List<Project> findAllByNameLikeIgnoreCase(String regex, Sort sort);

    List<Project> findAllByNameLikeIgnoreCase(String regex, Pageable sort);

}
