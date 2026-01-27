package com.simpleshop.identity.domain.model;

import com.simpleshop.identity.domain.event.UserDeactivated;
import com.simpleshop.identity.domain.event.UserLoggedIn;
import com.simpleshop.identity.domain.event.UserRegistered;
import com.simpleshop.identity.domain.model.vo.PersonName;
import com.simpleshop.identity.domain.model.vo.UserId;
import com.simpleshop.identity.domain.model.vo.UserRole;
import com.simpleshop.identity.domain.model.vo.UserStatus;
import com.simpleshop.shared.domain.model.AggregateRoot;
import com.simpleshop.shared.domain.model.vo.Email;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends AggregateRoot<User> {
    
    @Id
    private UUID id;
    
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true))
    private Email email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "firstName", column = @Column(name = "first_name")),
        @AttributeOverride(name = "lastName", column = @Column(name = "last_name"))
    })
    private PersonName name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "last_login_at")
    private Instant lastLoginAt;
    
    protected User() {}
    
    private User(UserId id, Email email, String passwordHash, PersonName name, UserRole role) {
        this.id = id.getValue();
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.createdAt = Instant.now();
    }
    
    public static User register(Email email, String passwordHash, PersonName name) {
        User user = new User(UserId.generate(), email, passwordHash, name, UserRole.USER);
        user.registerEvent(new UserRegistered(user.getUserId(), user.email));
        return user;
    }
    
    public static User registerAdmin(Email email, String passwordHash, PersonName name) {
        User user = new User(UserId.generate(), email, passwordHash, name, UserRole.ADMIN);
        user.registerEvent(new UserRegistered(user.getUserId(), user.email));
        return user;
    }
    
    public void recordLogin() {
        this.lastLoginAt = Instant.now();
        registerEvent(new UserLoggedIn(getUserId()));
    }
    
    public void deactivate() {
        if (this.status == UserStatus.INACTIVE) {
            return;
        }
        this.status = UserStatus.INACTIVE;
        registerEvent(new UserDeactivated(getUserId()));
    }
    
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
    
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }
    
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
    
    public UserId getUserId() {
        return UserId.of(id);
    }
    
    public Email getEmail() {
        return email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public PersonName getName() {
        return name;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
}
