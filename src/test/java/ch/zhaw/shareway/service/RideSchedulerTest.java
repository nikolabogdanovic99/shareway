package ch.zhaw.shareway.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.RideStatus;
import ch.zhaw.shareway.repository.RideRepository;

@ExtendWith(MockitoExtension.class)
public class RideSchedulerTest {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private RideScheduler rideScheduler;

    @Test
    void testAutoCompleteRidesNoOpenRides() {
        when(rideRepository.findByStatus(RideStatus.OPEN)).thenReturn(Collections.emptyList());

        rideScheduler.autoCompleteRides();

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void testAutoCompleteRidesFutureRide() {
        Ride futureRide = new Ride();
        futureRide.setId("future-ride");
        futureRide.setDepartureTime(LocalDateTime.now().plusDays(1));
        futureRide.setDurationMinutes(60);
        futureRide.setStatus(RideStatus.OPEN);

        when(rideRepository.findByStatus(RideStatus.OPEN)).thenReturn(Arrays.asList(futureRide));

        rideScheduler.autoCompleteRides();

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void testAutoCompleteRidesPastRide() {
        Ride pastRide = new Ride();
        pastRide.setId("past-ride");
        pastRide.setDepartureTime(LocalDateTime.now().minusHours(5));
        pastRide.setDurationMinutes(60);
        pastRide.setStatus(RideStatus.OPEN);

        when(rideRepository.findByStatus(RideStatus.OPEN)).thenReturn(Arrays.asList(pastRide));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

        rideScheduler.autoCompleteRides();

        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    void testAutoCompleteRidesDefaultDuration() {
        // Ride without duration set (should use default 120 minutes)
        Ride ride = new Ride();
        ride.setId("no-duration-ride");
        ride.setDepartureTime(LocalDateTime.now().minusHours(3)); // 3 hours ago
        ride.setDurationMinutes(null); // Default 120 minutes
        ride.setStatus(RideStatus.OPEN);

        when(rideRepository.findByStatus(RideStatus.OPEN)).thenReturn(Arrays.asList(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

        rideScheduler.autoCompleteRides();

        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    void testAutoCompleteRidesNullDepartureTime() {
        Ride ride = new Ride();
        ride.setId("null-departure");
        ride.setStatus(RideStatus.OPEN);

        when(rideRepository.findByStatus(RideStatus.OPEN)).thenReturn(Arrays.asList(ride));

        rideScheduler.autoCompleteRides();

        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void testAutoCompleteRidesMultipleRides() {
        Ride futureRide = new Ride();
        futureRide.setId("future-ride");
        futureRide.setDepartureTime(LocalDateTime.now().plusDays(1));
        futureRide.setDurationMinutes(60);
        futureRide.setStatus(RideStatus.OPEN);

        Ride pastRide1 = new Ride();
        pastRide1.setId("past-ride-1");
        pastRide1.setDepartureTime(LocalDateTime.now().minusHours(5));
        pastRide1.setDurationMinutes(60);
        pastRide1.setStatus(RideStatus.OPEN);

        Ride pastRide2 = new Ride();
        pastRide2.setId("past-ride-2");
        pastRide2.setDepartureTime(LocalDateTime.now().minusDays(1));
        pastRide2.setDurationMinutes(30);
        pastRide2.setStatus(RideStatus.OPEN);

        when(rideRepository.findByStatus(RideStatus.OPEN))
                .thenReturn(Arrays.asList(futureRide, pastRide1, pastRide2));
        when(rideRepository.save(any(Ride.class))).thenAnswer(i -> i.getArgument(0));

        rideScheduler.autoCompleteRides();

        // Only past rides should be saved (2 rides)
        verify(rideRepository, times(2)).save(any(Ride.class));
    }

    @Test
    void testAutoCompleteRidesRideStillInProgress() {
        // Ride that just started (within duration)
        Ride inProgressRide = new Ride();
        inProgressRide.setId("in-progress");
        inProgressRide.setDepartureTime(LocalDateTime.now().minusMinutes(30));
        inProgressRide.setDurationMinutes(60); // Still 30 minutes left
        inProgressRide.setStatus(RideStatus.OPEN);

        when(rideRepository.findByStatus(RideStatus.OPEN)).thenReturn(Arrays.asList(inProgressRide));

        rideScheduler.autoCompleteRides();

        verify(rideRepository, never()).save(any(Ride.class));
    }
}
