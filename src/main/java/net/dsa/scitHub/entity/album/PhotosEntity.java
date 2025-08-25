package net.dsa.scitHub.entity.album;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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
    name = "photos",
    indexes = {
        @Index(name = "idx_ph_album_created", columnList = "album_id, created_at"),
        @Index(name = "idx_ph_uploader", columnList = "uploader_id")
    }
)
public class PhotosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id", columnDefinition = "int unsigned")
    private Integer photoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "album_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_ph_album"))
    private AlbumsEntity album;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uploader_id",
        foreignKey = @ForeignKey(name = "fk_ph_uploader"))
    private UsersEntity uploader;

    @Size(max = 1024)
    @Column(name = "file_url", nullable = false, length = 1024)
    private String fileUrl;

    @Size(max = 255)
    @Column(name = "caption", length = 255)
    private String caption;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /*
     * 연관관계 매핑
     * 자주 호출할 것 같은 것만 리스트로 매핑하고, 나머지는 그때그때
     * 쿼리로 불러오는 것이 좋음
     */

    // 좋아요 목록
    @Builder.Default
    @OneToMany(mappedBy = "photo", fetch = FetchType.LAZY)
    private List<PhotoLikesEntity> likes = new ArrayList<>();

    // 댓글 목록
    @Builder.Default
    @OneToMany(mappedBy = "photo", fetch = FetchType.LAZY)
    private List<PhotoCommentsEntity> comments = new ArrayList<>();
}
