package com.simpleshop.e2e.scenarios;

import com.simpleshop.e2e.config.E2EBaseTest;
import com.simpleshop.e2e.fixtures.TestDataFixture;
import com.simpleshop.e2e.pages.ProductDetailPage;
import com.simpleshop.e2e.pages.ProductListPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Order(1)
class CatalogBrowsingE2E extends E2EBaseTest {

    @Autowired
    TestDataFixture fixture;

    private UUID category1Id, category2Id;
    private UUID activeProduct1Id, activeProduct2Id, inactiveProductId;

    @BeforeEach
    void setUpData() {
        fixture.cleanAll();
        category1Id = fixture.createCategory("Electronics");
        category2Id = fixture.createCategory("Books");
        activeProduct1Id = fixture.createProduct("Laptop", "ELEC-001", new BigDecimal("999.99"), "USD", category1Id, true);
        activeProduct2Id = fixture.createProduct("Java Book", "BOOK-001", new BigDecimal("29.99"), "USD", category2Id, true);
        inactiveProductId = fixture.createProduct("Old Phone", "ELEC-002", new BigDecimal("199.99"), "USD", category1Id, false);
        UUID warehouseId = fixture.createWarehouse("Main WH", "123 St", "NY", "10001", "US");
        fixture.addStock(activeProduct1Id, warehouseId, 50);
        fixture.addStock(activeProduct2Id, warehouseId, 30);
    }

    @Test
    void shouldShowOnlyActiveProductsOnProductList() {
        navigateTo("/products");
        var productList = new ProductListPage(page);

        assertThat(productList.isProductVisible("Laptop")).isTrue();
        assertThat(productList.isProductVisible("Java Book")).isTrue();
        assertThat(productList.isProductVisible("Old Phone")).isFalse();
    }

    @Test
    void shouldFilterByCategory() {
        navigateTo("/products");
        var productList = new ProductListPage(page);
        productList.filterByCategory("Electronics");

        assertThat(productList.isProductVisible("Laptop")).isTrue();
        assertThat(productList.isProductVisible("Java Book")).isFalse();
    }

    @Test
    void shouldShowProductDetailPage() {
        navigateTo("/products/" + activeProduct1Id);
        var detail = new ProductDetailPage(page);

        assertThat(detail.getProductName()).isEqualTo("Laptop");
        assertThat(detail.getProductSku()).contains("ELEC-001");
        assertThat(detail.getStockStatus()).containsIgnoringCase("in stock");
    }

    @Test
    void shouldRedirectInactiveProductToProducts() {
        navigateTo("/products/" + inactiveProductId);

        assertThat(page.url()).contains("/products");
        assertThat(page.url()).doesNotContain(inactiveProductId.toString());
    }
}
