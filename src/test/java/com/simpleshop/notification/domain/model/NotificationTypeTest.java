package com.simpleshop.notification.domain.model;

import com.simpleshop.notification.domain.model.vo.NotificationType;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class NotificationTypeTest {

    @Test
    public void notificationTypes_haveCorrectDisplayNames() {
        assertEquals(NotificationType.ORDER_CONFIRMATION.getDisplayName(), "Order Confirmation");
        assertEquals(NotificationType.SHIPMENT_UPDATE.getDisplayName(), "Shipment Update");
        assertEquals(NotificationType.SHIPMENT_CREATED.getDisplayName(), "Shipment Created");
        assertEquals(NotificationType.INVOICE.getDisplayName(), "Invoice");
        assertEquals(NotificationType.USER_WELCOME.getDisplayName(), "Welcome");
    }

    @Test
    public void values_returnsAllTypes() {
        NotificationType[] types = NotificationType.values();
        
        assertEquals(types.length, 5);
        assertTrue(contains(types, NotificationType.ORDER_CONFIRMATION));
        assertTrue(contains(types, NotificationType.SHIPMENT_UPDATE));
        assertTrue(contains(types, NotificationType.SHIPMENT_CREATED));
        assertTrue(contains(types, NotificationType.INVOICE));
        assertTrue(contains(types, NotificationType.USER_WELCOME));
    }

    @Test
    public void valueOf_returnsCorrectType() {
        assertEquals(NotificationType.valueOf("ORDER_CONFIRMATION"), NotificationType.ORDER_CONFIRMATION);
        assertEquals(NotificationType.valueOf("SHIPMENT_UPDATE"), NotificationType.SHIPMENT_UPDATE);
        assertEquals(NotificationType.valueOf("SHIPMENT_CREATED"), NotificationType.SHIPMENT_CREATED);
        assertEquals(NotificationType.valueOf("INVOICE"), NotificationType.INVOICE);
        assertEquals(NotificationType.valueOf("USER_WELCOME"), NotificationType.USER_WELCOME);
    }

    private boolean contains(NotificationType[] types, NotificationType type) {
        for (NotificationType t : types) {
            if (t == type) return true;
        }
        return false;
    }
}
