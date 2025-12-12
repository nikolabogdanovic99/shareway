package ch.zhaw.shareway.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.zhaw.shareway.model.Booking;
import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.model.User;
import ch.zhaw.shareway.model.VerificationStatus;
import ch.zhaw.shareway.repository.RideRepository;
import ch.zhaw.shareway.repository.UserRepository;
import ch.zhaw.shareway.service.BookingService;
import ch.zhaw.shareway.service.MailService;
import ch.zhaw.shareway.service.UserService;

@RestController
@RequestMapping("/api/service")
public class RideServiceController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;


    /**
     * Book a ride for myself (Rider) - MIT PROMO-CODE
     */
    @PutMapping("/me/bookride")
    public ResponseEntity<Booking> bookRideForMe(
            @RequestParam String rideId,
            @RequestParam(defaultValue = "1") int seats,
            @RequestParam(required = false) String pickupLocation,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String promoCode) {
        String userEmail = userService.getEmail();
        Optional<Booking> booking = bookingService.createBooking(
                rideId, userEmail, seats, pickupLocation, message, promoCode);

        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/me/approvebooking")
    public ResponseEntity<Booking> approveMyBooking(@RequestParam String bookingId) {
        String userEmail = userService.getEmail();
        Optional<Booking> booking = bookingService.approveBooking(bookingId, userEmail);

        if (booking.isPresent()) {
            Booking b = booking.get();

            // E-Mail an Rider senden
            Optional<Ride> ride = rideRepository.findById(b.getRideId());
            if (ride.isPresent()) {
                ch.zhaw.shareway.model.Mail mail = new ch.zhaw.shareway.model.Mail();
                mail.setTo(b.getRiderId());
                mail.setSubject("ShareWay - Buchung bestätigt!");
                mail.setMessage(
                        "Gute Nachrichten!\n\n" +
                                "Deine Buchung wurde bestätigt.\n\n" +
                                "Fahrt: " + ride.get().getStartLocation() + " → " + ride.get().getEndLocation() + "\n" +
                                "Abfahrt: " + ride.get().getDepartureTime() + "\n\n" +
                                "Viel Spass bei der Fahrt!\n" +
                                "Dein ShareWay Team");
                mailService.sendMail(mail);
            }

            return ResponseEntity.ok(b);
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/me/rejectbooking")
    public ResponseEntity<Booking> rejectMyBooking(@RequestParam String bookingId) {
        String userEmail = userService.getEmail();
        Optional<Booking> booking = bookingService.rejectBooking(bookingId, userEmail);

        if (booking.isPresent()) {
            Booking b = booking.get();

            // E-Mail an Rider senden
            Optional<Ride> ride = rideRepository.findById(b.getRideId());
            if (ride.isPresent()) {
                ch.zhaw.shareway.model.Mail mail = new ch.zhaw.shareway.model.Mail();
                mail.setTo(b.getRiderId());
                mail.setSubject("ShareWay - Buchung abgelehnt");
                mail.setMessage(
                        "Leider wurde deine Buchung abgelehnt.\n\n" +
                                "Fahrt: " + ride.get().getStartLocation() + " → " + ride.get().getEndLocation() + "\n\n"
                                +
                                "Bitte suche nach einer anderen Mitfahrgelegenheit.\n\n" +
                                "Dein ShareWay Team");
                mailService.sendMail(mail);
            }

            return ResponseEntity.ok(b);
        }
        return ResponseEntity.badRequest().build();
    }

    @PutMapping("/admin/verify")
    public ResponseEntity<User> verifyUser(@RequestParam String userId) {
        if (!userService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optUser.get();
        user.setVerificationStatus(VerificationStatus.VERIFIED);
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/admin/reject")
    public ResponseEntity<User> rejectUser(@RequestParam String userId) {
        if (!userService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optUser.get();
        user.setVerificationStatus(VerificationStatus.DENIED);
        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/me/completeride")
    public ResponseEntity<Ride> completeMyRide(@RequestParam String rideId) {
        String userEmail = userService.getEmail();

        Optional<Ride> optRide = rideRepository.findById(rideId);
        if (optRide.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Ride ride = optRide.get();

        if (!ride.getDriverId().equals(userEmail) && !userService.userHasRole("admin")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (ride.getStatus() != RideStatus.OPEN) {
            return ResponseEntity.badRequest().build();
        }

        ride.setStatus(RideStatus.COMPLETED);
        Ride savedRide = rideRepository.save(ride);
        return ResponseEntity.ok(savedRide);
    }

    @PutMapping("/me/cancelbooking")
    public ResponseEntity<Booking> cancelMyBooking(@RequestParam String bookingId) {
        String userEmail = userService.getEmail();
        Optional<Booking> booking = bookingService.cancelBooking(bookingId, userEmail);

        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        }
        return ResponseEntity.badRequest().build();
    }
}