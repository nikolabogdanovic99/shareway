package ch.zhaw.shareway.model.discount;

import java.util.List;
import ch.zhaw.shareway.model.Ride;

public class SameRouteDiscount implements Discount {

    private String startLocation;
    private String endLocation;

    public SameRouteDiscount(String startLocation, String endLocation) {
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    @Override
    public double getDiscount(List<Ride> rides) {
        double sum = 0;
        int count = 0;
        for (Ride ride : rides) {
            if (ride.getStartLocation().equals(startLocation) && 
                ride.getEndLocation().equals(endLocation)) {
                sum += ride.getPricePerSeat();
                count++;
            }
        }
        if (count >= 2) {
            return sum * 15 / 100;
        }
        return 0.0;
    }
}