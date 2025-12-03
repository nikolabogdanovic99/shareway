package ch.zhaw.shareway.model.discount;

import java.util.List;
import ch.zhaw.shareway.model.Ride;

public class PercentageDiscount implements Discount {

    private int discount;
    
    public static String errorMessageGreaterZero = "Error: Discount value must be greater zero.";
    public static String errorMessage50 = "Error: Discount value must less or equal 50.";

    public PercentageDiscount(int discount) {
        if (discount <= 0) {
            throw new RuntimeException(errorMessageGreaterZero);
        }
        if (discount > 50) {
            throw new RuntimeException(errorMessage50);
        }
        this.discount = discount;
    }

    @Override
    public double getDiscount(List<Ride> rides) {
        double sum = 0;
        for (Ride ride : rides) {
            sum += ride.getPricePerSeat();
        }
        return sum * discount / 100;
    }
}