package com.simpleshop.integration.persistence;

import com.simpleshop.SimpleShopApplication;
import com.simpleshop.order.infrastructure.adapter.out.persistence.DatabaseOrderNumberGenerator;
import com.simpleshop.shipping.infrastructure.adapter.out.persistence.DatabaseTrackingNumberGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SimpleShopApplication.class)
@ActiveProfiles("test")
class SequenceGeneratorIT {

    private static final Pattern ORDER_NUMBER_PATTERN = Pattern.compile("ORD-\\d{4}-\\d{5}");
    private static final Pattern TRACKING_NUMBER_PATTERN = Pattern.compile("SHIP-\\d{4}-\\d{5}");

    @Autowired
    private DatabaseOrderNumberGenerator orderNumberGenerator;

    @Autowired
    private DatabaseTrackingNumberGenerator trackingNumberGenerator;

    @Test
    void intP006_shouldGenerateUniqueMonotonicOrderAndTrackingNumbers() {
        orderNumberGenerator.ensureSequenceExists();
        trackingNumberGenerator.ensureSequenceExists();

        List<String> orderNumbers = List.of(
            orderNumberGenerator.generate().getValue(),
            orderNumberGenerator.generate().getValue(),
            orderNumberGenerator.generate().getValue()
        );

        List<String> trackingNumbers = List.of(
            trackingNumberGenerator.generate().getValue(),
            trackingNumberGenerator.generate().getValue(),
            trackingNumberGenerator.generate().getValue()
        );

        assertTrue(orderNumbers.stream().allMatch(value -> ORDER_NUMBER_PATTERN.matcher(value).matches()));
        assertTrue(trackingNumbers.stream().allMatch(value -> TRACKING_NUMBER_PATTERN.matcher(value).matches()));

        assertEquals(3, orderNumbers.stream().distinct().count());
        assertEquals(3, trackingNumbers.stream().distinct().count());

        assertTrue(extractNumericSuffix(orderNumbers.get(0)) < extractNumericSuffix(orderNumbers.get(1)));
        assertTrue(extractNumericSuffix(orderNumbers.get(1)) < extractNumericSuffix(orderNumbers.get(2)));

        assertTrue(extractNumericSuffix(trackingNumbers.get(0)) < extractNumericSuffix(trackingNumbers.get(1)));
        assertTrue(extractNumericSuffix(trackingNumbers.get(1)) < extractNumericSuffix(trackingNumbers.get(2)));
    }

    private int extractNumericSuffix(String value) {
        String[] parts = value.split("-");
        assertEquals(3, parts.length);
        return Integer.parseInt(parts[2]);
    }
}
