package ch.zhaw.shareway.model.discount;

import java.util.List;
import ch.zhaw.shareway.model.Ride;

public class PercentageDiscount implements Discount {

    private int discount;

    public PercentageDiscount(int discount) {
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