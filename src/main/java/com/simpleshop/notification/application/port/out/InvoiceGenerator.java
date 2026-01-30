package com.simpleshop.notification.application.port.out;

import com.simpleshop.order.application.query.OrderView;

public interface InvoiceGenerator {
    
    byte[] generateInvoice(OrderView order);
}
