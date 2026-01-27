package com.simpleshop.identity.domain.model;

import com.simpleshop.identity.domain.event.UserDeactivated;
import com.simpleshop.identity.domain.event.UserLoggedIn;
import com.simpleshop.identity.domain.event.UserRegistered;
import com.simpleshop.identity.domain.model.vo.PersonName;
import com.simpleshop.identity.domain.model.vo.UserRole;
import com.simpleshop.identity.domain.model.vo.UserStatus;
import com.simpleshop.shared.domain.model.vo.Email;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UserTest {

    @Test
    public void register_createsActiveUserWithUserRole() {
        Email email = Email.of("test@example.com");
        PersonName name = PersonName.of("John", "Doe");
        
        User user = User.register(email, "hashedPassword", name);
        
        assertNotNull(user.getUserId());
        assertEquals(user.getEmail(), email);
        assertEquals(user.getPasswordHash(), "hashedPassword");
        assertEquals(user.getName().getFullName(), "John Doe");
        assertEquals(user.getRole(), UserRole.USER);
        assertEquals(user.getStatus(), UserStatus.ACTIVE);
        assertTrue(user.isActive());
        assertFalse(user.isAdmin());
        assertNotNull(user.getCreatedAt());
        assertNull(user.getLastLoginAt());
    }
    
    @Test
    public void registerAdmin_createsActiveUserWithAdminRole() {
        Email email = Email.of("admin@example.com");
        PersonName name = PersonName.of("Admin", "User");
        
        User user = User.registerAdmin(email, "hashedPassword", name);
        
        assertEquals(user.getRole(), UserRole.ADMIN);
        assertTrue(user.isAdmin());
    }
    
    @Test
    public void register_publishesUserRegisteredEvent() {
        Email email = Email.of("test@example.com");
        PersonName name = PersonName.of("John", "Doe");
        
        User user = User.register(email, "hashedPassword", name);
        
        var events = user.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof UserRegistered);
        
        UserRegistered event = (UserRegistered) events.iterator().next();
        assertEquals(event.getUserId(), user.getUserId());
        assertEquals(event.getEmail(), email);
    }
    
    @Test
    public void recordLogin_updatesLastLoginAtAndPublishesEvent() {
        User user = createTestUser();
        assertNull(user.getLastLoginAt());
        user.clearEvents();
        
        user.recordLogin();
        
        assertNotNull(user.getLastLoginAt());
        var events = user.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof UserLoggedIn);
    }
    
    @Test
    public void deactivate_setsStatusToInactiveAndPublishesEvent() {
        User user = createTestUser();
        assertTrue(user.isActive());
        user.clearEvents();
        
        user.deactivate();
        
        assertFalse(user.isActive());
        assertEquals(user.getStatus(), UserStatus.INACTIVE);
        var events = user.getDomainEvents();
        assertEquals(events.size(), 1);
        assertTrue(events.iterator().next() instanceof UserDeactivated);
    }
    
    @Test
    public void deactivate_isIdempotent() {
        User user = createTestUser();
        user.deactivate();
        user.clearEvents();
        
        user.deactivate();
        
        assertFalse(user.isActive());
        assertTrue(user.getDomainEvents().isEmpty());
    }
    
    @Test
    public void activate_setsStatusToActive() {
        User user = createTestUser();
        user.deactivate();
        
        user.activate();
        
        assertTrue(user.isActive());
        assertEquals(user.getStatus(), UserStatus.ACTIVE);
    }
    
    private User createTestUser() {
        return User.register(
            Email.of("test@example.com"),
            "hashedPassword",
            PersonName.of("Test", "User")
        );
    }
}
