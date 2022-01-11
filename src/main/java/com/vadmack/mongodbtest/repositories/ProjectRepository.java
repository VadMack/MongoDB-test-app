package com.vadmack.mongodbtest.repositories;

import com.vadmack.mongodbtest.entities.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends MongoRepository<Project, Long> {
}
