package com.simpleshop.order.domain.model;

import com.simpleshop.order.domain.model.vo.OrderItemId;
import com.simpleshop.shared.domain.model.vo.Money;
import com.simpleshop.shared.domain.model.vo.Quantity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    private UUID id;
    
    @Column(name = "product_id", nullable = false)
    private UUID productId;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity", nullable = false))
    private Quantity quantity;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount", nullable = false)),
        @AttributeOverride(name = "currencyCode", column = @Column(name = "unit_price_currency", nullable = false, length = 3))
    })
    private Money unitPrice;
    
    @Column(name = "warehouse_id")
    private UUID warehouseId;
    
    protected OrderItem() {}
    
    private OrderItem(OrderItemId id, UUID productId, String productName, 
                      Quantity quantity, Money unitPrice, UUID warehouseId) {
        this.id = id.getValue();
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.warehouseId = warehouseId;
    }
    
    public static OrderItem create(UUID productId, String productName, 
                                   int quantity, Money unitPrice, UUID warehouseId) {
        if (productId == null) throw new IllegalArgumentException("Product ID cannot be null");
        if (productName == null || productName.isBlank()) throw new IllegalArgumentException("Product name cannot be empty");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (unitPrice == null) throw new IllegalArgumentException("Unit price cannot be null");
        
        return new OrderItem(
            OrderItemId.generate(),
            productId,
            productName,
            Quantity.of(quantity),
            unitPrice,
            warehouseId
        );
    }
    
    public Money getSubtotal() {
        return unitPrice.multiply(quantity.getValue());
    }
    
    public UUID getId() {
        return id;
    }
    
    public OrderItemId getOrderItemId() {
        return OrderItemId.of(id);
    }
    
    public UUID getProductId() {
        return productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public Quantity getQuantity() {
        return quantity;
    }
    
    public Money getUnitPrice() {
        return unitPrice;
    }
    
    public UUID getWarehouseId() {
        return warehouseId;
    }
}
