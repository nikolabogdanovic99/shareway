package ch.zhaw.shareway.model.discount;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import ch.zhaw.shareway.model.Ride;

public class SameRouteDiscountTest {

    @Test
    public void testDifferentRoutes() {
        SameRouteDiscount discount = new SameRouteDiscount("Zürich", "Bern");
        List<Ride> rides = new ArrayList<>();
        rides.add(createRide(25.0, "Zürich", "Basel"));
        rides.add(createRide(25.0, "Basel", "Genf"));
        assertEquals(0.0, discount.getDiscount(rides));
    }

    @Test
    public void testSameRoute_two() {
        SameRouteDiscount discount = new SameRouteDiscount("Zürich", "Bern");
        List<Ride> rides = new ArrayList<>();
        rides.add(createRide(25.0, "Zürich", "Bern"));
        rides.add(createRide(25.0, "Zürich", "Bern"));
        assertEquals(7.5, discount.getDiscount(rides));
    }

    @Test
    public void testSameRoute_three() {
        SameRouteDiscount discount = new SameRouteDiscount("Zürich", "Bern");
        List<Ride> rides = new ArrayList<>();
        rides.add(createRide(25.0, "Zürich", "Bern"));
        rides.add(createRide(25.0, "Zürich", "Bern"));
        rides.add(createRide(25.0, "Zürich", "Bern"));
        assertEquals(11.25, discount.getDiscount(rides));
    }

    @ParameterizedTest
    @CsvSource({"0,0", "1,0", "2,7.5", "3,11.25"})
    public void testMultipleRides(int numRides, double expected) {
        SameRouteDiscount discount = new SameRouteDiscount("Zürich", "Bern");
        List<Ride> rides = new ArrayList<>();
        for (int i = 0; i < numRides; i++) {
            rides.add(createRide(25.0, "Zürich", "Bern"));
        }
        assertEquals(expected, discount.getDiscount(rides));
    }

    private Ride createRide(double pricePerSeat, String start, String end) {
        Ride ride = new Ride();
        ride.setPricePerSeat(pricePerSeat);
        ride.setStartLocation(start);
        ride.setEndLocation(end);
        return ride;
    }
}