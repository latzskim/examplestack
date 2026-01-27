package com.simpleshop.identity.infrastructure.security;

import com.simpleshop.identity.domain.model.User;
import com.simpleshop.identity.domain.model.vo.UserId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class ShopUserDetails implements UserDetails {
    
    private final User user;
    
    public ShopUserDetails(User user) {
        this.user = user;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
    
    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail().getValue();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return user.isActive();
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
    
    public UserId getUserId() {
        return user.getUserId();
    }
    
    public User getUser() {
        return user;
    }
    
    public boolean isAdmin() {
        return user.isAdmin();
    }
}
