package com.tp.foodai.security.entities;

import com.tp.foodai.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends AuditableAbstractAggregateRoot<User> {

    @Column(name = "firebase_uid", unique = true, nullable = false, length = 128)
    private String firebaseUid;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "provider", length = 50)
    private String provider; // "password", "google.com", etc.

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
