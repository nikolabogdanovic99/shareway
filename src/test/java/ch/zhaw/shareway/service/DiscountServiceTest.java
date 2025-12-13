package ch.zhaw.shareway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import ch.zhaw.shareway.model.Ride;

public class DiscountServiceTest {

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
    }

    // ==================== isValidCode Tests ====================

    @ParameterizedTest
    @ValueSource(strings = {"WELCOME10", "welcome10", "Welcome10", "SHARE20", "share20", "SUMMER15"})
    void testIsValidCodeWithValidCodes(String code) {
        assertTrue(discountService.isValidCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALID", "TEST", "DISCOUNT50", "PROMO"})
    void testIsValidCodeWithInvalidCodes(String code) {
        assertFalse(discountService.isValidCode(code));
    }

    @Test
    void testIsValidCodeWithNull() {
        assertFalse(discountService.isValidCode(null));
    }

    @Test
    void testIsValidCodeWithEmptyString() {
        assertFalse(discountService.isValidCode(""));
    }

    // ==================== getDiscountPercent Tests ====================

    @ParameterizedTest
    @CsvSource({
        "WELCOME10, 10",
        "welcome10, 10",
        "SHARE20, 20",
        "share20, 20",
        "SUMMER15, 15",
        "summer15, 15"
    })
    void testGetDiscountPercentWithValidCodes(String code, int expectedPercent) {
        assertEquals(expectedPercent, discountService.getDiscountPercent(code));
    }

    @Test
    void testGetDiscountPercentWithInvalidCode() {
        assertEquals(0, discountService.getDiscountPercent("INVALID"));
    }

    @Test
    void testGetDiscountPercentWithNull() {
        assertEquals(0, discountService.getDiscountPercent(null));
    }

    // ==================== calculateDiscount Tests ====================

    @Test
    void testCalculateDiscountWithValidCode() {
        Ride ride = new Ride();
        ride.setPricePerSeat(100.0);
        
        double discount = discountService.calculateDiscount("WELCOME10", ride);
        assertEquals(10.0, discount, 0.01);
    }

    @Test
    void testCalculateDiscountWithShare20() {
        Ride ride = new Ride();
        ride.setPricePerSeat(50.0);
        
        double discount = discountService.calculateDiscount("SHARE20", ride);
        assertEquals(10.0, discount, 0.01);
    }

    @Test
    void testCalculateDiscountWithInvalidCode() {
        Ride ride = new Ride();
        ride.setPricePerSeat(100.0);
        
        double discount = discountService.calculateDiscount("INVALID", ride);
        assertEquals(0.0, discount, 0.01);
    }

    // ==================== calculateFinalPrice Tests ====================

    @ParameterizedTest
    @CsvSource({
        "WELCOME10, 100.0, 90.0",
        "SHARE20, 100.0, 80.0",
        "SUMMER15, 100.0, 85.0",
        "WELCOME10, 50.0, 45.0",
        "SHARE20, 50.0, 40.0"
    })
    void testCalculateFinalPriceWithValidCodes(String code, double originalPrice, double expectedFinalPrice) {
        assertEquals(expectedFinalPrice, discountService.calculateFinalPrice(code, originalPrice), 0.01);
    }

    @Test
    void testCalculateFinalPriceWithInvalidCode() {
        assertEquals(100.0, discountService.calculateFinalPrice("INVALID", 100.0), 0.01);
    }

    @Test
    void testCalculateFinalPriceWithNull() {
        assertEquals(100.0, discountService.calculateFinalPrice(null, 100.0), 0.01);
    }

    @Test
    void testCalculateFinalPriceWithEmptyCode() {
        assertEquals(100.0, discountService.calculateFinalPrice("", 100.0), 0.01);
    }
}
