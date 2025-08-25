package net.dsa.scitHub.entity.community;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "post_reports",
    uniqueConstraints = @UniqueConstraint(name = "uq_report_once", columnNames = {"post_id", "reporter_id"}),
    indexes = @Index(name = "idx_reports_post_created", columnList = "post_id, created_at")
)
public class PostReportsEntity {

    public enum Status { PENDING, CONFIRMED, REJECTED }

    @Id
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id", columnDefinition = "int unsigned")
    private Integer reportId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_rep_post"))
    private PostsEntity post;

    // 신고자 NULL 허용, 삭제 시 CASCADE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = true,
        foreignKey = @ForeignKey(name = "fk_rep_user"))
    private UsersEntity reporter;

    @Size(max = 255)
    @Column(name = "reason", length = 255)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status; // default PENDING

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (status == null) status = Status.PENDING;
    }
}
