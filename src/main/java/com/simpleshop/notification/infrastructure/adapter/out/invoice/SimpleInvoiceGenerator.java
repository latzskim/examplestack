package com.simpleshop.notification.infrastructure.adapter.out.invoice;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.simpleshop.notification.application.port.out.InvoiceGenerator;
import com.simpleshop.order.application.query.OrderItemView;
import com.simpleshop.order.application.query.OrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class SimpleInvoiceGenerator implements InvoiceGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleInvoiceGenerator.class);
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    
    @Override
    public byte[] generateInvoice(OrderView order) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            
            document.open();
            
            // Title
            Paragraph title = new Paragraph("INVOICE", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Order details
            PdfPTable detailsTable = new PdfPTable(2);
            detailsTable.setWidthPercentage(100);
            detailsTable.setSpacingAfter(20);
            
            addTableRow(detailsTable, "Order Number:", order.orderNumber());
            addTableRow(detailsTable, "Order Date:", order.createdAt().toString());
            addTableRow(detailsTable, "Status:", order.status());
            
            document.add(detailsTable);
            
            // Shipping address
            Paragraph shippingHeader = new Paragraph("Shipping Address", HEADER_FONT);
            shippingHeader.setSpacingAfter(10);
            document.add(shippingHeader);
            
            document.add(new Paragraph(order.shippingStreet(), NORMAL_FONT));
            document.add(new Paragraph(order.shippingCity() + ", " + order.shippingPostalCode(), NORMAL_FONT));
            document.add(new Paragraph(order.shippingCountry(), NORMAL_FONT));
            document.add(new Paragraph(" "));
            
            // Order items table
            Paragraph itemsHeader = new Paragraph("Order Items", HEADER_FONT);
            itemsHeader.setSpacingAfter(10);
            document.add(itemsHeader);
            
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            
            // Header row
            itemsTable.addCell(createHeaderCell("Product"));
            itemsTable.addCell(createHeaderCell("Quantity"));
            itemsTable.addCell(createHeaderCell("Unit Price"));
            itemsTable.addCell(createHeaderCell("Total"));
            
            // Data rows
            for (OrderItemView item : order.items()) {
                itemsTable.addCell(createCell(item.productName()));
                itemsTable.addCell(createCell(String.valueOf(item.quantity())));
                itemsTable.addCell(createCell(item.unitPrice() + " " + item.currency()));
                itemsTable.addCell(createCell(item.subtotal() + " " + item.currency()));
            }
            
            document.add(itemsTable);
            
            // Total
            Paragraph total = new Paragraph("Total: " + order.totalAmount() + " " + order.currency(), HEADER_FONT);
            total.setAlignment(Element.ALIGN_RIGHT);
            total.setSpacingBefore(20);
            document.add(total);
            
            document.close();
            
            logger.debug("Invoice generated for order: {}", order.orderNumber());
            return outputStream.toByteArray();
            
        } catch (DocumentException e) {
            logger.error("Failed to generate invoice for order: {}", order.orderNumber(), e);
            throw new RuntimeException("Failed to generate invoice", e);
        }
    }
    
    private void addTableRow(PdfPTable table, String label, String value) {
        table.addCell(createCell(label));
        table.addCell(createCell(value));
    }
    
    private PdfPCell createCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, NORMAL_FONT));
        cell.setBorder(0);
        cell.setPadding(5);
        return cell;
    }
    
    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new java.awt.Color(220, 220, 220));
        cell.setPadding(5);
        return cell;
    }
}
