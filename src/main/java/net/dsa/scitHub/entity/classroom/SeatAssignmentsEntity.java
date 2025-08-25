package net.dsa.scitHub.entity.classroom;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;
import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "seat_assignments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_sa_seat", columnNames = {"seat_map_id","seat_id"}),
        @UniqueConstraint(name = "uq_sa_user", columnNames = {"seat_map_id","user_id"})
    }
)
public class SeatAssignmentsEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_assignment_id", columnDefinition = "int unsigned")
    private Integer seatAssignmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_map_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_sa_map"))
    private SeatMapsEntity seatMap;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_sa_seat"))
    private SeatsEntity seat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_sa_user"))
    private UsersEntity user;

    @CreatedDate
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

}
