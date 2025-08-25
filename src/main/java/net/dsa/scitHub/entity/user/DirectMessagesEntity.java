package net.dsa.scitHub.entity.user;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 쪽지함 (TABLE: direct_messages)
 * - 본문(MEDIUMTEXT), 송신/수신자 소프트삭제, 수신자 열람여부
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "direct_messages",
    indexes = {
        // 수신함 조회 최적화
        @Index(name = "idx_dm_receiver_unread", columnList = "receiver_id, deleted_by_receiver, is_read, created_at"),
        // 발신함 조회 최적화
        @Index(name = "idx_dm_sender_box", columnList = "sender_id, deleted_by_sender, created_at")
    }
)
public class DirectMessagesEntity {

    // PK (INT UNSIGNED)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id", columnDefinition = "int unsigned")
    private Integer messageId;

    // 컬렉션을 통한 양방향 매핑 대신, 쿼리로 수/발신자 기준 쪽지 검색하는 쪽으로
    // 발신자 FK → users.user_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "sender_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_dm_sender")
    )
    private UsersEntity sender;

    // 수신자 FK → users.user_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "receiver_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_dm_receiver")
    )
    private UsersEntity receiver;

    // 제목(선택)
    @Size(max = 200)
    @Column(name = "subject", length = 200)
    private String subject;

    // 본문: MEDIUMTEXT
    @Column(name = "body", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String body;

    // 수신자 열람 여부
    @Column(name = "is_read", nullable = false, columnDefinition = "tinyint default 0")
    private Boolean isRead;

    // 열람 시각
    @Column(name = "read_at")
    private LocalDateTime readAt;

    // 발송 시각
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 소프트 삭제 플래그
    @Column(name = "deleted_by_sender", nullable = false, columnDefinition = "tinyint default 0")
    private Boolean deletedBySender;

    @Column(name = "deleted_by_receiver", nullable = false, columnDefinition = "tinyint default 0")
    private Boolean deletedByReceiver;

    // ====== 라이프사이클 ======
    @PrePersist
    void onCreate() {
        if (this.isRead == null) this.isRead = false;
        if (this.deletedBySender == null) this.deletedBySender = false;
        if (this.deletedByReceiver == null) this.deletedByReceiver = false;
    }


    // ====== 편의 메서드 ======
    /** 수신자 열람 처리 */
    public void markAsRead() {
        if (Boolean.TRUE.equals(this.isRead)) return;
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}
