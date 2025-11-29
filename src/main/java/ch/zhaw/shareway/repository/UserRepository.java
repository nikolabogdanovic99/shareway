package ch.zhaw.shareway.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.UserRole;

public interface UserRepository extends MongoRepository<User, String> {
    // Query-Methoden (wie in Übung 2 gelernt)
    Optional<User> findByEmail(String email);
    Optional<User> findByAuth0Id(String auth0Id);
    List<User> findByRole(UserRole role);
    List<User> findByRatingGreaterThanEqual(Double minRating);
}