package ch.zhaw.shareway.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ch.zhaw.shareway.model.Ride;
import ch.zhaw.shareway.model.discount.Discount;
import ch.zhaw.shareway.model.discount.PercentageDiscount;

@Service
public class DiscountService {

    // Verfügbare Promo-Codes
    private static final Map<String, Integer> PROMO_CODES = new HashMap<>();
    
    static {
        PROMO_CODES.put("WELCOME10", 10);  // 10% Rabatt
        PROMO_CODES.put("SHARE20", 20);    // 20% Rabatt
        PROMO_CODES.put("SUMMER15", 15);   // 15% Rabatt
    }

    /**
     * Prüft ob ein Promo-Code gültig ist
     */
    public boolean isValidCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        return PROMO_CODES.containsKey(code.toUpperCase());
    }

    /**
     * Gibt den Rabatt-Prozentsatz für einen Code zurück
     */
    public int getDiscountPercent(String code) {
        if (!isValidCode(code)) {
            return 0;
        }
        return PROMO_CODES.get(code.toUpperCase());
    }

    /**
     * Berechnet den Rabatt mit PercentageDiscount Klasse
     */
    public double calculateDiscount(String code, Ride ride) {
        if (!isValidCode(code)) {
            return 0;
        }
        
        int percent = PROMO_CODES.get(code.toUpperCase());
        Discount discount = new PercentageDiscount(percent);
        
        return discount.getDiscount(List.of(ride));
    }

    /**
     * Berechnet den Endpreis nach Rabatt
     */
    public double calculateFinalPrice(String code, double originalPrice) {
        if (!isValidCode(code)) {
            return originalPrice;
        }
        
        int percent = PROMO_CODES.get(code.toUpperCase());
        double discountAmount = originalPrice * percent / 100;
        
        return originalPrice - discountAmount;
    }
}