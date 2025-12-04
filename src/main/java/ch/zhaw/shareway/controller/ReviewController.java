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

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.BookingStatus;
import ch.zhaw.shareway.model.Review;
import ch.zhaw.shareway.model.ReviewCreateDTO;
import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.repository.BookingRepository;
import ch.zhaw.shareway.repository.ReviewRepository;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.repository.UserRepository;
import ch.zhaw.shareway.service.UserService;

@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    RideRepository rideRepository;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    // POST /api/reviews - Neue Bewertung erstellen
    @PostMapping("/reviews")
    public ResponseEntity<Review> createReview(@RequestBody ReviewCreateDTO dto) {
        String userEmail = userService.getEmail();

        // Validierung: Rating zwischen 1-5
        if (dto.getRating() < 1 || dto.getRating() > 5) {
            return ResponseEntity.badRequest().build();
        }

        // Validierung: Ride existiert und ist COMPLETED
        Optional<Ride> optRide = rideRepository.findById(dto.getRideId());
        if (optRide.isEmpty() || optRide.get().getStatus() != RideStatus.COMPLETED) {
            return ResponseEntity.badRequest().build();
        }

        Ride ride = optRide.get();

        // Validierung: User hat an dieser Fahrt teilgenommen (approved booking)
        List<Booking> userBookings = bookingRepository.findByRiderIdAndRideId(userEmail, dto.getRideId());
        boolean hasApprovedBooking = userBookings.stream()
                .anyMatch(b -> b.getStatus() == BookingStatus.APPROVED);

        if (!hasApprovedBooking) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Validierung: Nur eine Review pro Rider pro Ride
        List<Review> existingReviews = reviewRepository.findByRideId(dto.getRideId());
        boolean alreadyReviewed = existingReviews.stream()
                .anyMatch(r -> r.getFromUserId().equals(userEmail));

        if (alreadyReviewed) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Review erstellen (toUserId ist der Driver)
        Review review = new Review(
            dto.getRideId(),
            userEmail,
            ride.getDriverId(),
            dto.getRating(),
            dto.getComment()
        );

        Review savedReview = reviewRepository.save(review);

        // User Rating aktualisieren
        updateUserRating(ride.getDriverId());

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

    // GET /api/reviews/ride/{rideId} - Reviews für eine Ride
    @GetMapping("/reviews/ride/{rideId}")
    public ResponseEntity<List<Review>> getReviewsByRide(@PathVariable String rideId) {
        List<Review> reviews = reviewRepository.findByRideId(rideId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    // GET /api/reviews/user/{userId} - Reviews für einen User (als Driver)
    @GetMapping("/reviews/user/{userId}")
    public ResponseEntity<List<Review>> getReviewsForUser(@PathVariable String userId) {
        List<Review> reviews = reviewRepository.findByToUserId(userId);
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    // Helper: User Rating aktualisieren
    private void updateUserRating(String userId) {
        List<Review> reviews = reviewRepository.findByToUserId(userId);
        if (reviews.isEmpty()) {
            return;
        }

        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        Optional<User> optUser = userRepository.findByEmail(userId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setRating(Math.round(averageRating * 10.0) / 10.0); // Eine Dezimalstelle
            user.setReviewCount(reviews.size());
            userRepository.save(user);
        }
    }
}