package net.dsa.scitHub.entity.career;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;

import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "company_reviews",
    uniqueConstraints = @UniqueConstraint(name = "uq_review_once", columnNames = {"company_id", "reviewer_id"}),
    indexes = {
        @Index(name = "idx_cr_company_created", columnList = "company_id, created_at")
    }
)
public class CompanyReviewsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id", columnDefinition = "int unsigned")
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_cr_company"))
    private CompaniesEntity company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id",
        foreignKey = @ForeignKey(name = "fk_cr_user"))
    private UsersEntity reviewer;

    // tinyint(1..5) → Java는 Integer로 관리 + Bean Validation 보강
    @Min(1) @Max(5)
    @Column(name = "rating", nullable = false, columnDefinition = "tinyint")
    private Integer rating;

    @Lob
    @Column(name = "body", columnDefinition = "MEDIUMTEXT")
    private String body;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    // 리뷰에 대한 댓글 목록
    @Builder.Default
    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY)
    private List<CompanyReviewCommentsEntity> comments = new ArrayList<>();
}
