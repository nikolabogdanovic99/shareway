package ch.zhaw.shareway.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.shareway.model.Review;
import ch.zhaw.shareway.model.ReviewCreateDTO;
import ch.zhaw.shareway.repository.ReviewRepository;

@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    ReviewRepository reviewRepository;

    // POST /api/reviews - Neue Bewertung erstellen
    @PostMapping("/reviews")
    public ResponseEntity<Review> createReview(@RequestBody ReviewCreateDTO dto) {
        // TODO: Später Validierung:
        // - Rating zwischen 1-5?
        // - Ride existiert und ist COMPLETED?
        // - User hat an dieser Fahrt teilgenommen?
        // - KI-Moderation des Kommentars
        
        Review review = new Review(
            dto.getRideId(),
            dto.getFromUserId(),
            dto.getToUserId(),
            dto.getRating(),
            dto.getComment()
        );
        
        // createdAt = now (default)
        
        Review savedReview = reviewRepository.save(review);
        return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
    }

    // GET /api/reviews - Alle Bewertungen
    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> allReviews = reviewRepository.findAll();
        return new ResponseEntity<>(allReviews, HttpStatus.OK);
    }

    // GET /api/reviews/{id} - Bewertung by ID
    @GetMapping("/reviews/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable String id) {
        Optional<Review> optReview = reviewRepository.findById(id);
        if (optReview.isPresent()) {
            return new ResponseEntity<>(optReview.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}