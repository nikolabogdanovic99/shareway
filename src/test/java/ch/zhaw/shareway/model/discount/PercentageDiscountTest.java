package ch.zhaw.shareway.model.discount;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import ch.zhaw.shareway.model.Ride;

public class PercentageDiscountTest {

    @Test
    public void testEmpty() {
        PercentageDiscount discount = new PercentageDiscount(10);
        List<Ride> rides = new ArrayList<>();
        assertEquals(0.0, discount.getDiscount(rides));
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 15, 20})
    public void testSingleRide(int percent) {
        PercentageDiscount discount = new PercentageDiscount(percent);
        List<Ride> rides = new ArrayList<>();
        rides.add(createRide(50.0));
        assertEquals(50.0 * percent / 100, discount.getDiscount(rides));
    }

    @Test
    public void testMultipleRides() {
        PercentageDiscount discount = new PercentageDiscount(10);
        List<Ride> rides = new ArrayList<>();
        rides.add(createRide(25.0));
        rides.add(createRide(35.0));
        assertEquals(6.0, discount.getDiscount(rides));
    }

    private Ride createRide(double pricePerSeat) {
        Ride ride = new Ride();
        ride.setPricePerSeat(pricePerSeat);
        return ride;
    }
}