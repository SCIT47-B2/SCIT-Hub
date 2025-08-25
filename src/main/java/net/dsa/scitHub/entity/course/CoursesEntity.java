package net.dsa.scitHub.entity.course;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;

import net.dsa.scitHub.entity.user.UsersEntity;
import net.dsa.scitHub.entity.assignment.AssignmentsEntity;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "courses",
    indexes = {
        @Index(name = "idx_courses_instructor", columnList = "instructor_id"),
        @Index(name = "idx_courses_type_class", columnList = "course_type, class_section"),
        @Index(name = "idx_courses_created", columnList = "created_at")
    }
)
public class CoursesEntity {

    public enum CourseType { IT, JP }
    public enum ClassSection { A, B }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id", columnDefinition = "int unsigned")
    private Integer courseId;

    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    // 담당 강사(사용자) — 삭제 시 SET NULL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "instructor_id",
        foreignKey = @ForeignKey(name = "fk_courses_instructor")
    )
    private UsersEntity instructor;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false, length = 2)
    private CourseType courseType;

    @Column(name = "cohort_no")
    private Integer cohortNo;

    // nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "class_section", length = 1)
    private ClassSection classSection;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    /*
     * 연관관계 매핑
     * 자주 호출할 것 같은 것만 리스트로 매핑하고, 나머지는 그때그때
     * 쿼리로 불러오는 것이 좋음
     */

    // 내가 수강 등록한 강의(조인 엔티티)
    @Builder.Default
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<EnrollmentsEntity> enrollments = new ArrayList<>();

    // 내가 작성한 강의평가
    @Builder.Default
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<CourseEvaluationsEntity> evaluations = new ArrayList<>();

    // 내가 만든 과제
    @Builder.Default
    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY)
    private List<AssignmentsEntity> assignments = new ArrayList<>();
}
