package com.vadmack.mongodbtest.repository;

import com.vadmack.mongodbtest.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@RequiredArgsConstructor
public class CustomProjectRepositoryImpl implements CustomProjectRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Project> findAllByNameLikeIgnoreCase(String namePart, Sort sort) {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").regex(namePart, "i"));
        query.with(sort);
        return mongoTemplate.find(query, Project.class);
    }

    @Override
    public List<Project> findAllByNameLikeIgnoreCase(String namePart, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").regex(namePart, "i"));
        query.with(pageable);
        return mongoTemplate.find(query, Project.class);
    }
}
