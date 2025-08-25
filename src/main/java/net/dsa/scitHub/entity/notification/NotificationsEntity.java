package net.dsa.scitHub.entity.notification;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;

/**
 * 알림 (TABLE: notifications)
 */
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "notifications",
    indexes = {
        @Index(name = "idx_notif_user_unread", columnList = "user_id, is_read, created_at"),
        @Index(name = "idx_notif_ref", columnList = "ref_type, ref_id")
    }
)
public class NotificationsEntity {

    public enum Type {
        NOTICE, COMMENT, MESSAGE, GROUP_ASSIGN, SCHEDULE, RESERVATION, SYSTEM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", columnDefinition = "int unsigned")
    private Integer notificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_notif_user")
    )
    private UsersEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private Type type;

    @Size(max = 150)
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Size(max = 500)
    @Column(name = "body", length = 500)
    private String body;

    @Size(max = 500)
    @Column(name = "target_url", length = 500)
    private String targetUrl;

    // 참조 엔티티 (폴리모픽)
    @Size(max = 50)
    @Column(name = "ref_type", length = 50)
    private String refType;

    @Column(name = "ref_id", columnDefinition = "int unsigned")
    private Integer refId;

    @Column(name = "is_read", nullable = false, columnDefinition = "tinyint default 0")
    private Boolean isRead;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // ===== 라이프사이클 =====
    @PrePersist
    void onCreate() {
        if (isRead == null) isRead = false;
    }

    // ===== 편의 메서드 =====
    public void markAsRead() {
        if (!Boolean.TRUE.equals(this.isRead)) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }

    public void setRef(String refType, Integer refId) {
        this.refType = refType;
        this.refId = refId;
    }
}
