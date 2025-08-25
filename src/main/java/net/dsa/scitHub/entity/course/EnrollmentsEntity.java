package net.dsa.scitHub.entity.course;

import java.io.Serializable;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "enrollments")
public class EnrollmentsEntity {

    @EmbeddedId
    private EnrollmentId id;

    @MapsId("courseId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "course_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_enr_course")
    )
    private CoursesEntity course;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_enr_user")
    )
    private UsersEntity user;

    // ===== 복합키 클래스 =====
    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    @Embeddable
    public static class EnrollmentId implements Serializable {
        @Column(name = "course_id", columnDefinition = "int unsigned")
        private Integer courseId;

        @Column(name = "user_id", columnDefinition = "int unsigned")
        private Integer userId;
    }
}
