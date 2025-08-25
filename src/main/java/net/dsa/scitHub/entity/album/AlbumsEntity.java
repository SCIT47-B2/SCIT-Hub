package net.dsa.scitHub.entity.album;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

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
    name = "albums",
    indexes = {
        @Index(name = "idx_albums_creator", columnList = "created_by"),
        @Index(name = "idx_albums_cohort", columnList = "cohort_no, created_at")
    }
)
public class AlbumsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "album_id", columnDefinition = "int unsigned")
    private Integer albumId;

    @Column(name = "cohort_no")
    private Integer cohortNo;

    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false,
        foreignKey = @ForeignKey(name = "fk_album_user"))
    private UsersEntity createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    /*
     * 연관관계 매핑
     * 자주 호출할 것 같은 것만 리스트로 매핑하고, 나머지는 그때그때
     * 쿼리로 불러오는 것이 좋음
     */

    // 이 앨범에 속한 사진 목록
    @Builder.Default
    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
    private List<PhotosEntity> photos = new ArrayList<>();
}
