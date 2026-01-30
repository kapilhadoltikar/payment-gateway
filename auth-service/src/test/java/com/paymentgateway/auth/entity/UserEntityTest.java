package com.paymentgateway.auth.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void userEntity_GettersSettersWork() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");
        user.setRoles(Set.of("USER"));
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getRoles()).containsExactly("USER");
        assertThat(user.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void userEntity_BuilderWorks() {
        User user = User.builder()
                .username("builduser")
                .password("pass")
                .email("build@test.com")
                .roles(Set.of("ADMIN"))
                .build();

        assertThat(user.getUsername()).isEqualTo("builduser");
        assertThat(user.getRoles()).containsExactly("ADMIN");
    }

    @Test
    void userEntity_PrePersistWorks() {
        User user = new User();
        user.onCreate();
        assertThat(user.getCreatedAt()).isNotNull();
    }
}
