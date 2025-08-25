package net.dsa.scitHub.entity.course;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "course_evaluations",
    uniqueConstraints = @UniqueConstraint(name = "uq_ce_once", columnNames = {"course_id","user_id"}),
    indexes = @Index(name = "idx_ce_course_created", columnList = "course_id, created_at")
)
public class CourseEvaluationsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "evaluation_id", columnDefinition = "int unsigned")
    private Integer evaluationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "course_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_ce_course")
    )
    private CoursesEntity course;

    // DDL: NOT NULL, FK에 ON DELETE 미지정(= RESTRICT/NO ACTION)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        foreignKey = @ForeignKey(name = "fk_ce_user")
    )
    private UsersEntity user;

    // 1~5
    @Min(1) @Max(5) @Column(name = "score_preparedness", nullable = false, columnDefinition = "tinyint")
    private Integer scorePreparedness;

    @Min(1) @Max(5) @Column(name = "score_clarity",      nullable = false, columnDefinition = "tinyint")
    private Integer scoreClarity;

    @Min(1) @Max(5) @Column(name = "score_fairness",     nullable = false, columnDefinition = "tinyint")
    private Integer scoreFairness;

    @Min(1) @Max(5) @Column(name = "score_respond",      nullable = false, columnDefinition = "tinyint")
    private Integer scoreRespond;

    @Min(1) @Max(5) @Column(name = "score_engagement",   nullable = false, columnDefinition = "tinyint")
    private Integer scoreEngagement;

    @Min(1) @Max(5) @Column(name = "score_passion",      nullable = false, columnDefinition = "tinyint")
    private Integer scorePassion;

    @Lob
    @Column(name = "comment_text", columnDefinition = "MEDIUMTEXT")
    private String commentText;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
