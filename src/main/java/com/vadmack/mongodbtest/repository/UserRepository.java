package com.vadmack.mongodbtest.repository;

import com.vadmack.mongodbtest.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findAllByUsernameLikeIgnoreCase(String namePart, Sort sort);
    List<User> findAllByUsernameLikeIgnoreCase(String namePart, Pageable pageable);
}
