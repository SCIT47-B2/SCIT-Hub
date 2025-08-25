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
    name = "photo_comments",
    indexes = @Index(name = "idx_pc_photo_created", columnList = "photo_id, created_at")
)
public class PhotoCommentsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", columnDefinition = "int unsigned")
    private Integer commentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photo_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_pc_photo"))
    private PhotosEntity photo;

    // DDL상 NOT NULL, ON DELETE 미지정(=RESTRICT)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
        foreignKey = @ForeignKey(name = "fk_pc_user"))
    private UsersEntity user;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
