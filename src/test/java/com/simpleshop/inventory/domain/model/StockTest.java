package com.simpleshop.inventory.domain.model;

import com.simpleshop.inventory.domain.event.StockDepleted;
import com.simpleshop.inventory.domain.event.StockReleased;
import com.simpleshop.inventory.domain.event.StockReplenished;
import com.simpleshop.inventory.domain.event.StockReserved;
import com.simpleshop.inventory.domain.exception.InsufficientStockException;
import com.simpleshop.shared.domain.model.vo.Quantity;
import org.testng.annotations.Test;
import java.util.UUID;
import static org.testng.Assert.*;

public class StockTest {
    
    private final UUID productId = UUID.randomUUID();
    private final UUID warehouseId = UUID.randomUUID();
    
    @Test
    public void shouldCreateStock() {
        Stock stock = Stock.create(productId, warehouseId, Quantity.of(100));
        
        assertNotNull(stock.getId());
        assertEquals(stock.getProductId(), productId);
        assertEquals(stock.getWarehouseId(), warehouseId);
        assertEquals(stock.getQuantity().getValue(), 100);
        assertEquals(stock.getReservedQuantity().getValue(), 0);
        assertEquals(stock.getAvailableQuantity().getValue(), 100);
    }
    
    @Test
    public void shouldReplenishStock() {
        Stock stock = Stock.create(productId, warehouseId, Quantity.of(50));
        stock.clearEvents();
        
        stock.replenish(Quantity.of(30));
        
        assertEquals(stock.getQuantity().getValue(), 80);
        assertEquals(stock.getDomainEvents().size(), 1);
        assertTrue(stock.getDomainEvents().iterator().next() instanceof StockReplenished);
    }
    
    @Test
    public void shouldReserveStock() {
        Stock stock = Stock.create(productId, warehouseId, Quantity.of(100));
        stock.clearEvents();
        
        stock.reserve(Quantity.of(30));
        
        assertEquals(stock.getQuantity().getValue(), 100);
        assertEquals(stock.getReservedQuantity().getValue(), 30);
        assertEquals(stock.getAvailableQuantity().getValue(), 70);
        assertTrue(stock.getDomainEvents().iterator().next() instanceof StockReserved);
    }
    
    @Test(expectedExceptions = InsufficientStockException.class)
    public void shouldThrowWhenReservingMoreThanAvailable() {
        Stock stock = Stock.create(productId, warehouseId, Quantity.of(50));
        stock.reserve(Quantity.of(60));
    }
    
    @Test
    public void shouldReleaseReservedStock() {
        Stock stock = Stock.create(productId, warehouseId, Quantity.of(100));
        stock.reserve(Quantity.of(30));
        stock.clearEvents();
        
        stock.release(Quantity.of(20));
        
        assertEquals(stock.getReservedQuantity().getValue(), 10);
        assertEquals(stock.getAvailableQuantity().getValue(), 90);
        assertTrue(stock.getDomainEvents().iterator().next() instanceof StockReleased);
    }
    
    @Test
    public void shouldConfirmReservation() {
        Stock stock = Stock.create(productId, warehouseId, Quantity.of(100));
        stock.reserve(Quantity.of(30));
        stock.clearEvents();
        
        stock.confirmReservation(Quantity.of(30));
        
        assertEquals(stock.getQuantity().getValue(), 70);
        assertEquals(stock.getReservedQuantity().getValue(), 0);
        assertEquals(stock.getAvailableQuantity().getValue(), 70);
    }
    
    @Test
    public void shouldRegisterStockDepletedWhenAvailableBecomesZero() {
        Stock stock = Stock.create(productId, warehouseId, Quantity.of(50));
        stock.clearEvents();
        
        stock.reserve(Quantity.of(50));
        
        boolean hasDepletedEvent = stock.getDomainEvents().stream()
            .anyMatch(e -> e instanceof StockDepleted);
        assertTrue(hasDepletedEvent);
    }
}
