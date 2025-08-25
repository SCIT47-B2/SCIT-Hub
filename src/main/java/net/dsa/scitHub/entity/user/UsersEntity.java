package net.dsa.scitHub.entity.user;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 사용자 기본 정보 (TABLE: users)
 * - MySQL 8.0 / utf8mb4_0900_ai_ci
 * - TINYINT → Boolean
 * - DATE → LocalDate, DATETIME → LocalDateTime
 */
@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
    }
//  , indexes = { // 스키마 자동생성 시에만 사용; 실제 운영 DDL과 다르면 생략
//      @Index(name = "idx_users_last_login_at", columnList = "last_login_at")
//  }
)
public class UsersEntity {

    // ====== ENUM 정의 ======
    public enum Gender { M, F, O, N }        // 남/여/기타/무응답
    public enum ITClass { A, B }              // IT 반
    public enum ITSession { AM, PM }          // IT 오전/오후
    public enum JPClass { A, B, C, D, E, F }  // 일본어 반

    // ====== 컬럼 매핑 ======
    @Id
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", columnDefinition = "int unsigned")
    private Integer userId;

    @Size(min = 4, max = 32)
    @Column(name = "username", nullable = false, length = 32)
    private String username;

    @Email
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    // BCrypt/Argon2 해시 저장
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "name_kor", nullable = false, length = 50)
    private String nameKor;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender; // DB 기본값 'N'

    @Column(name = "cohort_no")
    private Integer cohortNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "it_class")
    private ITClass itClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "it_session")
    private ITSession itSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "jp_class")
    private JPClass jpClass;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(
        name = "is_active",
        nullable = false,
        columnDefinition = "tinyint default 1"
    )
    private Boolean isActive;

    @Column(
        name = "is_admin",
        nullable = false,
        columnDefinition = "tinyint default 0"
    )
    private Boolean isAdmin;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ====== 라이프사이클 콜백 ======
    @PrePersist
    void onCreate() {
        // DB 기본값과 동기화
        if (this.isActive == null) this.isActive = true;
        if (this.isAdmin == null) this.isAdmin = false;
        if (this.gender == null) this.gender = Gender.N;
    }

    /*
     * 연관관계 매핑
     * 자주 호출할 것 같은 것만 리스트로 매핑하고, 나머지는 그때그때
     * 쿼리로 불러오는 것이 좋음
     */

}
