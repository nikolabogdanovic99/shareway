package ch.zhaw.shareway.model.discount;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import ch.zhaw.shareway.model.Ride;

public class FiveBucksDiscountTest {

    @Test
    public void testEmpty() {
        var discount = new FiveBucksDiscount();
        assertEquals(0, discount.getDiscount(new ArrayList<Ride>()), 0.01);
    }

    @Test
    public void testUnder50() {
        var discount = new FiveBucksDiscount();
        var rides = new ArrayList<Ride>();
        rides.add(new Ride("driver1", "vehicle1", "Zürich", "Winterthur", 
            LocalDateTime.now(), 20.0, 4, 4));
        assertEquals(0, discount.getDiscount(rides), 0.01);
    }

    @Test
    public void testExactly50() {
        var discount = new FiveBucksDiscount();
        var rides = new ArrayList<Ride>();
        rides.add(new Ride("driver1", "vehicle1", "Zürich", "Bern", 
            LocalDateTime.now(), 50.0, 4, 4));
        assertEquals(5, discount.getDiscount(rides), 0.01);
    }

    @Test
    public void testOver50() {
        var discount = new FiveBucksDiscount();
        var rides = new ArrayList<Ride>();
        rides.add(new Ride("driver1", "vehicle1", "Zürich", "Bern", 
            LocalDateTime.now(), 25.0, 4, 4));
        rides.add(new Ride("driver1", "vehicle1", "Bern", "Genf", 
            LocalDateTime.now(), 30.0, 4, 4));
        assertEquals(5, discount.getDiscount(rides), 0.01);
    }
}