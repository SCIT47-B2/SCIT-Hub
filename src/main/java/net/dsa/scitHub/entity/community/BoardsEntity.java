package net.dsa.scitHub.entity.community;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.ArrayList;

/**
 * 게시판 마스터 (TABLE: boards)
 * - 프로그램 키(board_key)로 식별/라우팅
 * - 공지/공개/Q&A 플래그 보유
 */
@Getter @Setter
@ToString(exclude = {"posts"}) // ToString에서 연관관계 필드는 제외
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "boards",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_boards_board_key", columnNames = "board_key")
    },
    indexes = {
        // 라우팅/조회 최적화(선택)
        @Index(name = "idx_boards_is_flags", columnList = "is_qna,is_notice,is_public,created_at")
    }
)
public class BoardsEntity {

    @Id
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", columnDefinition = "int unsigned")
    private Integer boardId;

    // 프로그램에서 사용하는 키 (예: NOTICE_OPS, QNA)
    @Size(max = 50)
    @Column(name = "board_key", nullable = false, length = 50)
    private String boardKey;

    // 표시 이름
    @Size(max = 100)
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // 설명(선택)
    @Size(max = 255)
    @Column(name = "description", length = 255)
    private String description;

    // Q&A 성격 여부
    @Column(name = "is_qna", nullable = false)
    private Boolean isQna;

    // 공지 전용 여부
    @Column(name = "is_notice", nullable = false)
    private Boolean isNotice;

    // 공개 여부
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic;

    // 생성 시각
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ====== 라이프사이클 ======
    @PrePersist
    void onCreate() {
        if (this.isQna == null) this.isQna = false;
        if (this.isNotice == null) this.isNotice = false;
        if (this.isPublic == null) this.isPublic = true;
    }

    /*
     * 연관관계 매핑
     * 자주 호출할 것 같은 것만 리스트로 매핑하고, 나머지는 그때그때
     * 쿼리로 불러오는 것이 좋음
     */


    // 이 게시판의 게시글들
    @Builder.Default
    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    private List<PostsEntity> posts = new ArrayList<>();
}
