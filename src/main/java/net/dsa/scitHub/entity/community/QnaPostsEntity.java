package net.dsa.scitHub.entity.community;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "qna_posts"
    // JoinColumn에서 이미 unique 제약조건을 걸었으므로 필요없음
    // uniqueConstraints = {
    //     @UniqueConstraint(name = "uk_qna_posts_post", columnNames = "post_id")
    // }
)
public class QnaPostsEntity {

    public enum AnswerStatus { PENDING, ANSWERED }

    @Id
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    @ToString.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qna_posts_id", columnDefinition = "int unsigned")
    private Integer qnaPostsId;

    // 1:1 (post_id UNIQUE + FK)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "post_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_qna_posts_post")
    )
    private PostsEntity post;

    @Enumerated(EnumType.STRING)
    @Column(name = "answer_status", nullable = false, length = 10)
    private AnswerStatus answerStatus;

    @PrePersist
    void onCreate() {
        if (answerStatus == null) answerStatus = AnswerStatus.PENDING;
    }
}
