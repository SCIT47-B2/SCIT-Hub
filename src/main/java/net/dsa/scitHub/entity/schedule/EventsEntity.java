package net.dsa.scitHub.entity.schedule;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
    name = "events",
    indexes = {
        @Index(name = "idx_events_owner_created", columnList = "owner_user_id, created_at"),
        @Index(name = "idx_events_visibility_time", columnList = "visibility, cohort_no, it_class_scope, start_at"),
        @Index(name = "idx_events_time_range", columnList = "start_at, end_at")
    }
)
public class EventsEntity {

    public enum Visibility { GLOBAL, PERSONAL }
    public enum ItClassScope { ALL, A, B }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", columnDefinition = "int unsigned")
    private Integer eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false, length = 10)
    private Visibility visibility;

    // 개인 일정 소유자 (NULL 가능, 삭제 시 CASCADE)
    // 추후 DB에서 cascade 동작이 제대로 실행되지 않을 경우, 엔티티 쪽에
    // @OnDelete(action = OnDeleteAction.CASCADE) 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "owner_user_id",
        foreignKey = @ForeignKey(name = "fk_events_owner")
    )
    private UsersEntity owner;

    @Column(name = "cohort_no")
    private Integer cohortNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "it_class_scope", nullable = false, length = 5)
    private ItClassScope itClassScope;

    @Size(max = 150)
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Lob
    @Column(name = "description", columnDefinition = "MEDIUMTEXT")
    private String description;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_all_day", nullable = false)
    private Boolean isAllDay;

    @Column(name = "dday_enabled", nullable = false)
    private Boolean ddayEnabled;

    // 생성자 (NULL 가능, 삭제 시 SET NULL)
    // 추후 DB에서 set null 동작이 제대로 실행되지 않을 경우, 엔티티 쪽에
    // @OnDelete(action = OnDeleteAction.SET_NULL) 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "created_by",
        foreignKey = @ForeignKey(name = "fk_events_creator")
    )
    private UsersEntity createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (isAllDay == null) isAllDay = false;
        if (ddayEnabled == null) ddayEnabled = false;
        if (visibility == null) visibility = Visibility.GLOBAL;
        if (itClassScope == null) itClassScope = ItClassScope.ALL;
    }

    // 시간 정합성 검증 코드
    @PrePersist
    @PreUpdate
    void validateTimeRange() {
        if (endAt != null && startAt != null && endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("endAt must be after startAt");
        }
    }
}
