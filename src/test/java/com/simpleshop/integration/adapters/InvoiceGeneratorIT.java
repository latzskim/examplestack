package com.simpleshop.integration.adapters;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.simpleshop.SimpleShopApplication;
import com.simpleshop.notification.infrastructure.adapter.out.invoice.SimpleInvoiceGenerator;
import com.simpleshop.order.application.query.OrderItemView;
import com.simpleshop.order.application.query.OrderView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SimpleShopApplication.class)
@ActiveProfiles("test")
class InvoiceGeneratorIT {

    @Autowired
    private SimpleInvoiceGenerator invoiceGenerator;

    @Test
    void intP107_shouldGeneratePdfInvoiceWithExpectedMetadata() throws Exception {
        String orderNumber = "ORD-2026-12345";
        BigDecimal totalAmount = new BigDecimal("149.98");

        OrderView order = new OrderView(
            UUID.randomUUID(),
            orderNumber,
            UUID.randomUUID(),
            List.of(
                new OrderItemView(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Noise Cancelling Headphones",
                    2,
                    new BigDecimal("74.99"),
                    "USD",
                    new BigDecimal("149.98"),
                    UUID.randomUUID()
                )
            ),
            "77 Sound Ave",
            "Nashville",
            "37201",
            "USA",
            "CONFIRMED",
            totalAmount,
            "USD",
            2,
            Instant.now(),
            Instant.now(),
            null,
            null,
            null,
            null
        );

        byte[] pdf = invoiceGenerator.generateInvoice(order);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);

        String header = new String(pdf, 0, Math.min(pdf.length, 4), StandardCharsets.US_ASCII);
        assertEquals("%PDF", header);

        String extractedText = extractText(pdf);
        assertTrue(extractedText.contains(orderNumber));
        assertTrue(extractedText.contains("149.98"));
    }

    private String extractText(byte[] pdfBytes) throws Exception {
        PdfReader reader = new PdfReader(pdfBytes);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);
        StringBuilder allText = new StringBuilder();

        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            allText.append(extractor.getTextFromPage(page));
            allText.append('\n');
        }

        reader.close();
        return allText.toString();
    }
}
