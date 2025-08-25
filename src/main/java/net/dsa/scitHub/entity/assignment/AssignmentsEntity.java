package net.dsa.scitHub.entity.assignment;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;

import net.dsa.scitHub.entity.course.CoursesEntity;
import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "assignments",
    indexes = {
        @Index(name = "idx_asn_course", columnList = "course_id"),
        @Index(name = "idx_asn_creator", columnList = "created_by"),
        @Index(name = "idx_asn_due", columnList = "due_at")
    }
)
public class AssignmentsEntity {

    public enum ClassSection { A, B }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id", columnDefinition = "int unsigned")
    private Integer assignmentId;

    // 과목(선택, 삭제 시 SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id",
        foreignKey = @ForeignKey(name = "fk_asn_course"))
    private CoursesEntity course;

    @Column(name = "cohort_no")
    private Integer cohortNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_section", length = 1)
    private ClassSection classSection;

    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 200)
    @Column(name = "filename_pattern", length = 200)
    private String filenamePattern;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false,
        foreignKey = @ForeignKey(name = "fk_asn_creator"))
    private UsersEntity createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    /*
     * 연관관계 매핑
     * 자주 호출할 것 같은 것만 리스트로 매핑하고, 나머지는 그때그때
     * 쿼리로 불러오는 것이 좋음
     */

    // 과제 제출 목록
    @Builder.Default
    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY)
    private List<AssignmentSubmissionsEntity> submissions = new ArrayList<>();
}
