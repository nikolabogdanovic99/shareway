package ch.zhaw.shareway.model.discount;

import java.util.List;
import ch.zhaw.shareway.model.Ride;

public interface Discount {
    public double getDiscount(List<Ride> rides);
}