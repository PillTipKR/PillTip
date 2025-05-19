// PillTip\BE\src\main\java\com\example\oauth2\entity\Interests.java
// author : mireutale
// date : 2025-05-19
// description : interests(관심사) 엔티티

package com.example.oauth2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "interests")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Interests {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid;

    @OneToOne
    @MapsId
    @JoinColumn(name = "uuid")
    private User user;

    @Column
    private boolean diet;

    @Column
    private boolean health;

    @Column
    private boolean muscle;

    @Column
    private boolean aging;

    @Column
    private boolean nutrient;
} 