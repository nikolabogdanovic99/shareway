package ch.zhaw.shareway.model.discount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    public void testBelowOrEqualZero() {
        var exception1 = assertThrows(RuntimeException.class, () -> {
            new PercentageDiscount(0);
        });
        assertEquals(PercentageDiscount.errorMessageGreaterZero, exception1.getMessage());

        var exception2 = assertThrows(RuntimeException.class, () -> {
            new PercentageDiscount(-5);
        });
        assertEquals(PercentageDiscount.errorMessageGreaterZero, exception2.getMessage());
    }

    @Test
    public void testGreater50() {
        var exception1 = assertThrows(RuntimeException.class, () -> {
            new PercentageDiscount(51);
        });
        assertEquals(PercentageDiscount.errorMessage50, exception1.getMessage());

        var exception2 = assertThrows(RuntimeException.class, () -> {
            new PercentageDiscount(120);
        });
        assertEquals(PercentageDiscount.errorMessage50, exception2.getMessage());
    }

    @Test
    public void testMultipleRides_Mock() {
        PercentageDiscount discount = new PercentageDiscount(42);

        var ride1 = mock(Ride.class);
        var ride2 = mock(Ride.class);
        when(ride1.getPricePerSeat()).thenReturn(77.0);
        when(ride2.getPricePerSeat()).thenReturn(42.0);

        assertEquals(49.98, discount.getDiscount(List.of(ride1, ride2)), 0.01);
    }

    private Ride createRide(double pricePerSeat) {
        Ride ride = new Ride();
        ride.setPricePerSeat(pricePerSeat);
        return ride;
    }
}