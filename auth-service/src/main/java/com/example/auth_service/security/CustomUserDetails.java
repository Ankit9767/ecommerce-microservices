package com.example.auth_service.security;

import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.Permission;
import com.example.auth_service.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Role role : user.getRoles()) {

            /*
             * Role authority
             */
            authorities.add(
                    new SimpleGrantedAuthority(
                            role.getRoleName().name()
                    )
            );

            /*
             * Permission authorities
             */
            for (Permission permission : role.getPermissions()) {

                authorities.add(
                        new SimpleGrantedAuthority(
                                permission
                                        .getPermissionName()
                                        .name()
                        )
                );
            }
        }

        return authorities;
    }

    @Override
    public String getPassword() {

        return user.getPassword();
    }

    @Override
    public String getUsername() {

        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {

        return user.getEnabled();
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    public User getUser() {
        return user;
    }
}