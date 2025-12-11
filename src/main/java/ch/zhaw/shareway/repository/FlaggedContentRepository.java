package ch.zhaw.shareway.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ch.zhaw.shareway.model.FlaggedContent;

public interface FlaggedContentRepository extends MongoRepository<FlaggedContent, String> {
    List<FlaggedContent> findByContentId(String contentId);
}