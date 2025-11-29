package ch.zhaw.shareway.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ch.zhaw.shareway.model.Ride;

public interface RideRepository extends MongoRepository<Ride, String> {
}
