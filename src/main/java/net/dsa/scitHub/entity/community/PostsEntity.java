package net.dsa.scitHub.entity.community;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;

import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "posts",
    indexes = {
        @Index(name = "idx_posts_board_created", columnList = "board_id, created_at"),
        @Index(name = "idx_posts_author_created", columnList = "author_id, created_at")
    }
)
public class PostsEntity {

    public enum Status { ACTIVE, DELETED, BLOCKED }

    @Id
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", columnDefinition = "int unsigned")
    private Integer postId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_posts_board"))
    private BoardsEntity board;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_posts_author"))
    private UsersEntity author;

    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status; // default ACTIVE

    @Column(name = "view_count", nullable = false, columnDefinition = "int unsigned default 0")
    private Integer viewCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        if (status == null) status = Status.ACTIVE;
        if (viewCount == null) viewCount = 0;
    }

    // 조회수 증가 메서드
    public void increaseViewCount() {
        this.viewCount = (this.viewCount == null ? 1 : this.viewCount + 1);
    }

    /*
     * 연관관계 매핑
     * 자주 호출할 것 같은 것만 리스트로 매핑하고, 나머지는 그때그때
     * 쿼리로 불러오는 것이 좋음
     */

    // 요거 뭐하는건지, 가져와야 하는지 검토 필요
    // Q&A 1:1 역방향
    @OneToOne(mappedBy = "post", fetch = FetchType.LAZY)
    private QnaPostsEntity qna;

    // 댓글 목록
    @Builder.Default
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<CommentsEntity> comments = new ArrayList<>();

    // 좋아요 목록
    @Builder.Default
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<PostLikesEntity> likes = new ArrayList<>();

    // 첨부파일 목록
    @Builder.Default
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<PostAttachmentsEntity> attachments = new ArrayList<>();

    // 태그 매핑(조인 엔티티 방식 권장)
    @Builder.Default
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<PostTagsEntity> postTags = new ArrayList<>();

}
