// PillTip\BE\src\main\java\com\example\oauth2\entity\UserPermissions.java
// author : mireutale
// date : 2025-05-21
// description : user_permissions(사용자 동의) 엔티티
package com.oauth2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermissions {
    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID uuid;

    @OneToOne
    @MapsId
    @JoinColumn(name = "uuid")
    private User user;

    @Column(name = "location_permission", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean locationPermission; // 위치 권한 허용 여부

    @Column(name = "camera_permission", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean cameraPermission; // 카메라 권한 허용 여부

    @Column(name = "gallery_permission", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean galleryPermission; // 갤러리 접근 권한 허용 여부

    @Column(name = "phone_permission", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean phonePermission; // 전화 권한 허용 여부

    @Column(name = "sms_permission", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean smsPermission; // SMS 권한 허용 여부
}