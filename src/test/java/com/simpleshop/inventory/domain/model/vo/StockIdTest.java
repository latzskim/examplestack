package com.simpleshop.inventory.domain.model.vo;

import org.testng.annotations.Test;
import java.util.UUID;
import static org.testng.Assert.*;

public class StockIdTest {

    @Test
    public void shouldGenerateStockId() {
        StockId stockId = StockId.generate();
        
        assertNotNull(stockId);
        assertNotNull(stockId.getValue());
    }

    @Test
    public void shouldCreateStockIdFromUuid() {
        UUID uuid = UUID.randomUUID();
        StockId stockId = StockId.of(uuid);
        
        assertNotNull(stockId);
        assertEquals(stockId.getValue(), uuid);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldRejectNullValue() {
        StockId.of(null);
    }

    @Test
    public void shouldBeEqualWhenSameValue() {
        UUID uuid = UUID.randomUUID();
        StockId id1 = StockId.of(uuid);
        StockId id2 = StockId.of(uuid);
        
        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void shouldNotBeEqualWhenDifferentValue() {
        StockId id1 = StockId.generate();
        StockId id2 = StockId.generate();
        
        assertNotEquals(id1, id2);
    }
}
