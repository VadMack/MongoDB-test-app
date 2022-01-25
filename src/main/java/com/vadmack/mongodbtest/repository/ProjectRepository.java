package com.vadmack.mongodbtest.repository;

import com.vadmack.mongodbtest.entity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends MongoRepository<Project, Long>, CustomProjectRepository {
    List<Project> findAllByOwnerId(Long ownerId);
}
