package net.dsa.scitHub.entity.reservation;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "reservation_reports",
    indexes = {
        @Index(name = "idx_rr_res_created", columnList = "reservation_id, created_at")
    }
)
public class ReservationReportsEntity {

    public enum Status { PENDING, CONFIRMED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", columnDefinition = "int unsigned")
    private Integer reportId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_rr_res"))
    private ReservationsEntity reservation;

    // DDL상 NOT NULL (사용자 삭제시 보존 의도면 SET NULL 권장 — 아래 체크 포인트 참고)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id",
        foreignKey = @ForeignKey(name = "fk_rr_reporter"))
    private UsersEntity reporter;

    @Size(max = 255)
    @Column(name = "reason", length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status; // default PENDING

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (status == null) status = Status.PENDING;
    }
}
