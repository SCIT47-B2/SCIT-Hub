package net.dsa.scitHub.entity.community;

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
@Table(
    name = "post_likes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_post_likes_one_per_user", columnNames = {"post_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_post_likes_post", columnList = "post_id")
    }
)
public class PostLikesEntity {

    @Id
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id", columnDefinition = "int unsigned")
    private Integer postLikeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_pl_post"))
    private PostsEntity post;

    // user_id NULL 허용 (ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
        foreignKey = @ForeignKey(name = "fk_pl_user"))
    private UsersEntity user;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}

