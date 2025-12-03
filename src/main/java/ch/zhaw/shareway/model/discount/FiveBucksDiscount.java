package ch.zhaw.shareway.model.discount;

import java.util.List;
import ch.zhaw.shareway.model.Ride;

public class FiveBucksDiscount implements Discount {

    @Override
    public double getDiscount(List<Ride> rides) {
        var sum = rides.stream().mapToDouble(r -> r.getPricePerSeat()).sum();
        if (sum >= 50) {
            return 5;
        }
        return 0;
    }
}