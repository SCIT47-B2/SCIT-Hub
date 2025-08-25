package net.dsa.scitHub.entity.community;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;

/**
 * 즐겨찾는 게시판 (TABLE: board_favorites)
 * - 복합 PK(user_id, board_id)
 * - 유저/게시판 삭제 시 CASCADE로 행 삭제 (DB 레벨)
 */
@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "board_favorites")
public class BoardFavoritesEntity {

    // ==== 복합키 정의 ====
    @EmbeddedId
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    @ToString.Include // ToString에 포함
    private BoardFavoritesId id;

    // 사용자 FK → users.user_id
    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_bf_user")
        // columnDefinition은 스키마 자동생성 시에만 필요
        // , columnDefinition = "int unsigned"
    )
    private UsersEntity user;

    // 게시판 FK → boards.board_id
    @MapsId("boardId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "board_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_bf_board")
        // , columnDefinition = "int unsigned"
    )
    private BoardsEntity board;

    // 등록 시각
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    // ==== 편의 생성자 ====
    public static BoardFavoritesEntity of(UsersEntity user, BoardsEntity board) {
        BoardFavoritesEntity e = new BoardFavoritesEntity();
        e.setId(new BoardFavoritesId(user.getUserId(), board.getBoardId()));
        e.setUser(user);
        e.setBoard(board);
        e.setCreatedAt(LocalDateTime.now());
        return e;
    }

    // ==== 복합키 클래스 ====
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    public static class BoardFavoritesId implements Serializable {
        @Column(name = "user_id", columnDefinition = "int unsigned")
        private Integer userId;

        @Column(name = "board_id", columnDefinition = "int unsigned")
        private Integer boardId;
    }
}
