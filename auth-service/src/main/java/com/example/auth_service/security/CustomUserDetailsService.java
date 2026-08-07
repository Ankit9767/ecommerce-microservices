//package com.example.auth_service.security;
//
//import com.example.auth_service.entity.User;
//import com.example.auth_service.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService
//        implements UserDetailsService {
//
//    private final UserRepository repository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username)
//            throws UsernameNotFoundException {
//
//        User user = repository.findByUsername(username)
//                .orElseThrow(() ->
//                        new UsernameNotFoundException(username));
//
//        return new CustomUserDetails(user);
//    }
//
//}
