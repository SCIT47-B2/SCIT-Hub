package net.dsa.scitHub.entity.career;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "company_review_comments",
    indexes = @Index(name = "idx_crc_review_created", columnList = "review_id, created_at")
)
public class CompanyReviewCommentsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", columnDefinition = "int unsigned")
    private Integer commentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_crc_review"))
    private CompanyReviewsEntity review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_crc_user"))
    private UsersEntity author;

    @Lob
    @Column(name = "body", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String body;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
