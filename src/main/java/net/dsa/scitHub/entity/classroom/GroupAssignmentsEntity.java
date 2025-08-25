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
    name = "group_assignments",
    uniqueConstraints = @UniqueConstraint(name = "uq_ga_unique", columnNames = {"cohort_no","class_section","user_id"}),
    indexes = @Index(name = "idx_ga_group", columnList = "group_id")
)
public class GroupAssignmentsEntity {

    public enum ClassSection { A, B }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id", columnDefinition = "int unsigned")
    private Integer assignmentId;

    @Column(name = "cohort_no", nullable = false)
    private Integer cohortNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_section", nullable = false, length = 1)
    private ClassSection classSection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_ga_group"))
    private StudyGroupsEntity group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_ga_user"))
    private UsersEntity user;

    @CreatedDate
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

}
