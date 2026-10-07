package com.mediconnect.settings;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_settings")
@Getter
@Setter
@NoArgsConstructor
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String key;

    @Column(name = "setting_value", nullable = false, length = 255)
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SystemSetting(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    @PrePersist
    @PreUpdate
    protected void onPersist() {
        this.updatedAt = LocalDateTime.now();
    }
}
