package ch.zhaw.shareway.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.repository.RideRepository;

@Service
public class RideScheduler {

    @Autowired
    private RideRepository rideRepository;

    // Läuft alle 15 Minuten
    @Scheduled(fixedRate = 900000)
    public void autoCompleteRides() {
        LocalDateTime now = LocalDateTime.now();

        // Alle OPEN oder IN_PROGRESS Rides holen
        List<Ride> openRides = rideRepository.findByStatus(RideStatus.OPEN);
        List<Ride> inProgressRides = rideRepository.findByStatus(RideStatus.IN_PROGRESS);

        // OPEN Rides prüfen
        for (Ride ride : openRides) {
            if (shouldComplete(ride, now)) {
                ride.setStatus(RideStatus.COMPLETED);
                rideRepository.save(ride);
                System.out.println("Auto-completed ride: " + ride.getId());
            }
        }

        // IN_PROGRESS Rides prüfen
        for (Ride ride : inProgressRides) {
            if (shouldComplete(ride, now)) {
                ride.setStatus(RideStatus.COMPLETED);
                rideRepository.save(ride);
                System.out.println("Auto-completed ride: " + ride.getId());
            }
        }
    }

    private boolean shouldComplete(Ride ride, LocalDateTime now) {
        if (ride.getDepartureTime() == null) {
            return false;
        }

        // Default 120 Minuten falls nicht gesetzt
        int duration = ride.getDurationMinutes() != null ? ride.getDurationMinutes() : 120;

        LocalDateTime endTime = ride.getDepartureTime().plusMinutes(duration);
        return now.isAfter(endTime);
    }
}