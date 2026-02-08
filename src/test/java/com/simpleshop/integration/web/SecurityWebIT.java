package com.simpleshop.integration.web;

import com.simpleshop.SimpleShopApplication;
import com.simpleshop.cart.domain.model.Cart;
import com.simpleshop.cart.domain.model.vo.SessionId;
import com.simpleshop.cart.infrastructure.adapter.out.persistence.CartJpaRepository;
import com.simpleshop.catalog.domain.model.Product;
import com.simpleshop.catalog.domain.model.vo.Money;
import com.simpleshop.catalog.domain.model.vo.Sku;
import com.simpleshop.catalog.infrastructure.adapter.out.persistence.ProductJpaRepository;
import com.simpleshop.identity.domain.model.User;
import com.simpleshop.identity.domain.model.vo.PersonName;
import com.simpleshop.identity.infrastructure.adapter.out.persistence.JpaUserRepository;
import com.simpleshop.identity.infrastructure.security.ShopUserDetails;
import com.simpleshop.inventory.infrastructure.adapter.out.persistence.JpaStockRepository;
import com.simpleshop.inventory.infrastructure.adapter.out.persistence.JpaWarehouseRepository;
import com.simpleshop.notification.infrastructure.adapter.out.persistence.JpaNotificationLogRepository;
import com.simpleshop.order.infrastructure.adapter.out.persistence.OrderJpaRepository;
import com.simpleshop.shared.domain.model.vo.Email;
import com.simpleshop.shipping.infrastructure.adapter.out.persistence.ShipmentJpaRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SimpleShopApplication.class)
@ActiveProfiles("test")
class SecurityWebIT {

    private static final String CART_SESSION_ID = "CART_SESSION_ID";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JpaUserRepository userRepository;

    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private CartJpaRepository cartRepository;

    @Autowired
    private JpaNotificationLogRepository notificationLogRepository;

    @Autowired
    private ShipmentJpaRepository shipmentRepository;

    @Autowired
    private OrderJpaRepository orderRepository;

    @Autowired
    private JpaStockRepository stockRepository;

    @Autowired
    private JpaWarehouseRepository warehouseRepository;

    @BeforeEach
    void cleanDatabase() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        notificationLogRepository.deleteAll();
        shipmentRepository.deleteAll();
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        stockRepository.deleteAll();
        warehouseRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void intP009_shouldEnforceAuthorizationMatrixAcrossRoutes() throws Exception {
        mockMvc.perform(get("/products"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/cart"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/orders"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/admin/orders"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        User user = createUser("web-user@example.com", false, "user-pass");
        User admin = createUser("web-admin@example.com", true, "admin-pass");

        mockMvc.perform(get("/orders").with(shopUser(user)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/admin/orders").with(shopUser(user)))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/orders").with(shopUser(admin)))
            .andExpect(status().isOk());
    }

    @Test
    void intP010_shouldEnforceCsrfOnStateChangingEndpoints() throws Exception {
        Product product = productRepository.save(Product.create(
            "Router",
            "Wi-Fi Router",
            Sku.of("sku-router"),
            Money.usd(new BigDecimal("129.00")),
            UUID.randomUUID(),
            null
        ));

        User user = createUser("csrf-user@example.com", false, "user-pass");
        User admin = createUser("csrf-admin@example.com", true, "admin-pass");

        mockMvc.perform(post("/cart/add")
                .param("productId", product.getId().toString())
                .param("quantity", "1"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/cart/add")
                .with(csrf())
                .param("productId", product.getId().toString())
                .param("quantity", "1"))
            .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/checkout")
                .with(shopUser(user))
                .param("street", "1 Test St")
                .param("city", "Austin")
                .param("postalCode", "73301")
                .param("country", "USA"))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/checkout")
                .with(shopUser(user))
                .with(csrf())
                .param("street", "1 Test St")
                .param("city", "Austin")
                .param("postalCode", "73301")
                .param("country", "USA"))
            .andExpect(status().is3xxRedirection());

        UUID randomOrderId = UUID.randomUUID();

        mockMvc.perform(post("/admin/orders/{id}/confirm", randomOrderId)
                .with(shopUser(admin)))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/orders/{id}/confirm", randomOrderId)
                .with(shopUser(admin))
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void intP105_shouldMergeSessionCartOnSuccessfulLogin() throws Exception {
        Product product = productRepository.save(Product.create(
            "Speaker",
            "Smart Speaker",
            Sku.of("sku-speaker"),
            Money.usd(new BigDecimal("249.00")),
            UUID.randomUUID(),
            null
        ));

        String rawPassword = "merge-pass";
        User user = createUser("merge-user@example.com", false, rawPassword);

        String sessionCartId = "session-cart-merge";
        Cart sessionCart = Cart.createForSession(SessionId.of(sessionCartId));
        sessionCart.addItem(product.getId(), product.getPrice(), 2);
        cartRepository.save(sessionCart);

        Cart userCart = Cart.createForUser(user.getUserId().getValue());
        userCart.addItem(product.getId(), product.getPrice(), 1);
        cartRepository.save(userCart);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CART_SESSION_ID, sessionCartId);

        MvcResult result = mockMvc.perform(post("/login")
                .param("username", user.getEmail().getValue())
                .param("password", rawPassword)
                .session(session)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andReturn();

        Cart mergedCart = cartRepository.findByUserId(user.getUserId().getValue()).orElseThrow();
        assertEquals(1, mergedCart.getItems().size());
        assertEquals(3, mergedCart.getItems().getFirst().getQuantity().getValue());

        assertTrue(cartRepository.findBySessionIdValue(sessionCartId).isEmpty());

        HttpSession resultingSession = result.getRequest().getSession(false);
        if (resultingSession != null) {
            assertNull(resultingSession.getAttribute(CART_SESSION_ID));
        }
    }

    private User createUser(String email, boolean admin, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = admin
            ? User.registerAdmin(Email.of(email), encodedPassword, PersonName.of("Admin", "User"))
            : User.register(Email.of(email), encodedPassword, PersonName.of("Regular", "User"));
        return userRepository.save(user);
    }

    private RequestPostProcessor shopUser(User user) {
        ShopUserDetails details = new ShopUserDetails(user);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
            details,
            details.getPassword(),
            details.getAuthorities()
        );
        return authentication(authentication);
    }
}
