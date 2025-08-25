package net.dsa.scitHub.entity.classroom;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "seat_maps",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_active_map", columnNames = {"room_id","cohort_no","class_section","version_no"}),
        @UniqueConstraint(name = "uq_sm_active_one", columnNames = {"room_id","cohort_no","class_section","active_one"})
    },
    indexes = @Index(name = "idx_sm_room_created", columnList = "room_id, created_at")
)
public class SeatMapsEntity {

    public enum ClassSection { A, B }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_map_id", columnDefinition = "int unsigned")
    private Integer seatMapId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_sm_room"))
    private RoomsEntity room;

    @Column(name = "cohort_no", nullable = false)
    private Integer cohortNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_section", nullable = false, length = 1)
    private ClassSection classSection;

    @Column(name = "version_no", nullable = false, columnDefinition = "int default 1")
    private Integer versionNo;

    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint default 1")
    private Boolean isActive;

    // 생성 칼럼(읽기 전용 매핑)
    @Column(name = "active_one", insertable = false, updatable = false)
    private Boolean activeOne;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (isActive == null) isActive = true;
        if (versionNo == null) versionNo = 1;
    }

    // 좌석 맵 - 좌석
    @Builder.Default
    @OneToMany(mappedBy = "seatMap", fetch = FetchType.LAZY)
    private List<SeatsEntity> seats = new ArrayList<>();

    // 좌석 맵 - 좌석 배정
    @Builder.Default
    @OneToMany(mappedBy = "seatMap", fetch = FetchType.LAZY)
    private List<SeatAssignmentsEntity> assignments = new ArrayList<>();
}
