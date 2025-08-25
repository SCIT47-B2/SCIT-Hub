package net.dsa.scitHub.entity.community;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post_bookmarks")
public class PostBookmarksEntity {

    @EmbeddedId
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    private PostBookmarksId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_pb_post"))
    private PostsEntity post;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_pb_user"))
    private UsersEntity user;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    @Embeddable
    public static class PostBookmarksId implements Serializable {
        @Column(name = "post_id", columnDefinition = "int unsigned")
        private Integer postId;

        @Column(name = "user_id", columnDefinition = "int unsigned")
        private Integer userId;
    }
}

