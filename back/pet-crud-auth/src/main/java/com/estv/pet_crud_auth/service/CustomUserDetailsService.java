package com.estv.pet_crud_auth.service;

import com.estv.pet_crud_auth.entity.UserCredential;
import com.estv.pet_crud_auth.repository.UserCredentialRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserCredentialRepository userCredentialRepository;

    public CustomUserDetailsService(UserCredentialRepository userCredentialRepository) {
        this.userCredentialRepository = userCredentialRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserCredential user = this.userCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority(user.getRole().name())));
    }

    public UserDetails loadUserById(UUID id) {
        UserCredential user = userCredentialRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(
                        new SimpleGrantedAuthority(user.getRole().name())));
    }
}
