package net.dsa.scitHub.entity.community;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;

@Getter @Setter
@ToString(exclude = {"children"}) // ToString에서 연관관계 필드는 제외
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "comments",
    indexes = {
        @Index(name = "idx_comments_post_created", columnList = "post_id, created_at")
    }
)
public class CommentsEntity {

    @Id
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", columnDefinition = "int unsigned")
    private Integer commentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_comments_post"))
    private PostsEntity post;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_comments_author"))
    private UsersEntity author;

    // 부모 댓글 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id",
        foreignKey = @ForeignKey(name = "fk_comments_parent"))
    private CommentsEntity parent;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "is_answer", nullable = false)
    private Boolean isAnswer;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (isAnswer == null) isAnswer = false;
    }

    // 자식 댓글들
    @Builder.Default
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY, cascade = {}, orphanRemoval = false)
    private List<CommentsEntity> children = new ArrayList<>();
}

