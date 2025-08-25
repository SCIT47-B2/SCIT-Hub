package net.dsa.scitHub.entity.album;

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
    name = "photo_likes",
    uniqueConstraints = @UniqueConstraint(name = "uk_photo_likes_once", columnNames = {"photo_id","user_id"}),
    indexes = @Index(name = "idx_pl_photo", columnList = "photo_id")
)
public class PhotoLikesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_like_id", columnDefinition = "int unsigned")
    private Integer photoLikeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photo_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_pl_photo"))
    private PhotosEntity photo;

    // NULL 허용 (ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
        foreignKey = @ForeignKey(name = "fk_pl_user2"))
    private UsersEntity user;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
