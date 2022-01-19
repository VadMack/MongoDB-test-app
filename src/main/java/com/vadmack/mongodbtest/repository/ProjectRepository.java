package com.vadmack.mongodbtest.repository;

import com.vadmack.mongodbtest.entity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends MongoRepository<Project, Long>, CustomProjectRepository {

}
