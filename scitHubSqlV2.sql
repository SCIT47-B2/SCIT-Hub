-- =============================================================
-- SCIT Hub — Schema v2 (MySQL 8.0+)
-- Charset: utf8mb4 / Collation: utf8mb4_0900_ai_ci
-- 목적: 요구사항 충족 + 테이블 수 축소
-- =============================================================

-- -------------------------------------------------------------
-- 0) 기본 설정
-- -------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS scithub2
    -- 기본 문자셋: 이모지 포함
    DEFAULT CHARACTER SET utf8mb4
    -- 한국어/일본어 등 CJK 문자열 비교에 무난한 Collation
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE scithub2;

SET NAMES utf8mb4;
SET time_zone = '+09:00';

-- =============================================================
-- 1) 사용자
-- =============================================================
-- 사용자
CREATE TABLE user (
    -- 내부 식별자(PK)
    user_id        INT PRIMARY KEY AUTO_INCREMENT,
    -- 기수(예: 47) — 조회/필터용
    cohort_no      INT NULL,
    -- 로그인 ID(유니크, 4~32자 제약)
    username       VARCHAR(32)  NOT NULL UNIQUE,
    -- 비밀번호 해시(BCrypt/Argon2 등)
    password_hash  VARCHAR(255) NOT NULL,
    -- 실명(한글)
    name_kor       VARCHAR(50)  NOT NULL,
    -- 생년월일(선택)
    birth_date     DATE NULL,
    -- 성별: M/F/기타(O)/무응답(N)
    gender         ENUM('M','F','O','N') NOT NULL DEFAULT 'N',
    -- 로그인/알림용 이메일(유니크)
    email          VARCHAR(255) NOT NULL UNIQUE,
    -- 전화번호(선택)
    phone          VARCHAR(20)  NULL,
    -- 프로필 이미지 URL
    avatar_url     VARCHAR(500) NULL,
    -- 현재 소속 조(FK). 동일 기수/반에서 1개 조만 소속.
    group_id       INT NULL,
    -- 예약 패널티: 신고 누적 횟수(3회 이상이면 금지)
    resv_strike_count INT NOT NULL DEFAULT 0,
    -- 예약 금지 해제 시각(금지 중이면 값 존재)
    resv_banned_until DATETIME NULL,
    -- 계정 활성(로그인 가능 여부)
    is_active      TINYINT NOT NULL DEFAULT 1,
    -- 역할
    role           ENUM('STUDENT','TEACHER','STAFF') NOT NULL DEFAULT 'STUDENT',
    -- 최근 로그인 시각
    last_login_at  DATETIME NULL,
    -- 생성 시각
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 업데이트 시각
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- username 길이 제약
    CONSTRAINT chk_user_username CHECK (CHAR_LENGTH(username) BETWEEN 4 AND 32)
) ENGINE=InnoDB;

-- 삭제 사용자(고스트) 계정 선생성
INSERT INTO user (
    username, email, phone, password_hash, name_kor, birth_date, gender,
    cohort_no, avatar_url,
    group_id, resv_strike_count, resv_banned_until,
    is_active, role, last_login_at
) VALUES (
    -- 고정 username/이메일(로그인 불가)
    '__deleted_user__',
    '__deleted_user__@local.invalid',
    NULL,
    '!',                    -- 임의값: 인증/로그인 불가
    '탈퇴한 회원',          -- 표시용
    NULL, 'N',
    NULL, NULL,
    NULL, 0, NULL,
    0, 'STUDENT', NULL
);

-- =============================================================
-- 2) 쪽지/알림
-- =============================================================

-- 쪽지
CREATE TABLE direct_message (
    -- 내부 식별자(PK)
    message_id         INT PRIMARY KEY AUTO_INCREMENT,
    -- 발신자(삭제 시 RESTRICT → 고스트로 치환 트리거에서 처리)
    sender_id          INT NOT NULL,
    -- 수신자(삭제 시 RESTRICT → 고스트로 치환 트리거에서 처리)
    receiver_id        INT NOT NULL,
    -- 제목(선택)
    title              VARCHAR(200) NULL,
    -- 본문
    content            MEDIUMTEXT NOT NULL,
    -- 수신자 열람 여부(0/1)
    is_read            TINYINT NOT NULL DEFAULT 0,
    -- 열람 시각
    read_at            DATETIME NULL,
    -- 발송 시각
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 발신자 측 삭제 플래그(소프트 삭제)
    deleted_by_sender   TINYINT NOT NULL DEFAULT 0,
    -- FK
    CONSTRAINT fk_dm_sender   FOREIGN KEY (sender_id)   REFERENCES user(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 수신자/읽음여부/최신순
CREATE INDEX idx_dm_receiver_read  ON direct_message (receiver_id, is_read, created_at DESC);
-- 발신함 최신순
CREATE INDEX idx_dm_sender_created ON direct_message (sender_id, created_at);
-- 대량 업데이트 대비 보조 인덱스
CREATE INDEX idx_dm_sender_id      ON direct_message (sender_id);
CREATE INDEX idx_dm_receiver_id    ON direct_message (receiver_id);

-- 알림
CREATE TABLE notification (
    -- 내부 식별자(PK)
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    -- 수신자(삭제 시 함께 삭제)
    user_id         INT NOT NULL,
    -- 알림 유형(공지/댓글/쪽지/조배정/일정/예약/시스템)
    type            ENUM('NOTICE','COMMENT','MESSAGE','GROUP_ASSIGN','SCHEDULE','RESERVATION','SYSTEM') NOT NULL DEFAULT 'NOTICE',
    -- 알림 제목(토스트/목록 표기)
    title           VARCHAR(150) NOT NULL,
    -- 알림 본문(간단 설명)
    content         VARCHAR(500) NULL,
    -- 클릭 시 이동 경로(URL)
    target_url      VARCHAR(500) NULL,
    -- 참조 엔티티 유형(예: POST/EVENT)
    ref_type        VARCHAR(50) NULL,
    -- 참조 엔티티 ID
    ref_id          INT NULL,
    -- 읽음 여부(0/1)
    is_read         TINYINT NOT NULL DEFAULT 0,
    -- 생성 시각
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 읽음 시각
    read_at         DATETIME NULL,
    -- FK
    CONSTRAINT fk_noti_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 사용자별 미읽음 우선 최신순
CREATE INDEX idx_notification_user_latest ON notification (user_id, is_read, created_at DESC);


-- =============================================================
-- 3) 게시판/게시글/댓글/리액션/첨부
-- =============================================================

-- 게시판
CREATE TABLE board (
    -- 내부 식별자(PK)
    board_id     INT PRIMARY KEY AUTO_INCREMENT,
    -- 프로그램에서 식별하는 키(예: NOTICE_OPS, QNA) — 유니크
    board_key    VARCHAR(50) NOT NULL UNIQUE,
    -- 표시 이름(예: 공지-운영)
    name         VARCHAR(100) NOT NULL,
    -- 설명(선택)
    description  VARCHAR(255) NULL,
    -- Q&A 게시판 여부(1이면 작성 글이 QnA 성격)
    is_qna       TINYINT NOT NULL DEFAULT 0,
    -- 공지 전용 여부(1이면 운영측만 작성)
    is_notice    TINYINT NOT NULL DEFAULT 0,
    -- 공개 여부(1=비로그인 열람 허용 등 정책)
    is_public    TINYINT NOT NULL DEFAULT 1,
    -- 생성 시각
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 게시글
CREATE TABLE post (
    -- 내부 식별자(PK)
    post_id        INT PRIMARY KEY AUTO_INCREMENT,
    -- 게시판(FK, 삭제 시 글 같이 삭제)
    board_id       INT NOT NULL,
    -- 작성자(FK, 삭제는 RESTRICT → 고스트로 치환)
    author_id      INT NOT NULL,
    -- 제목
    title          VARCHAR(200) NOT NULL,
    -- 본문(HTML/Markdown 가능)
    content        MEDIUMTEXT NOT NULL,
    -- QnA 답변 상태(PENDING/ANSWERED). 댓글 is_answer와 동기화 트리거로 유지.
    answer_status  ENUM('PENDING','ANSWERED') NOT NULL DEFAULT 'PENDING',
    -- 태그 JSON 배열(예: ["spring","mysql"])
    tags_json      JSON NULL,
    -- 상태(활성/삭제/차단)
    status         ENUM('ACTIVE','DELETED','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    -- 조회수(증가 시 단순 +1)
    view_count     INT NOT NULL DEFAULT 0,
    -- 생성 시각
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 수정 시각
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- FK
    CONSTRAINT fk_post_board  FOREIGN KEY (board_id)  REFERENCES board(board_id) ON DELETE CASCADE,
    CONSTRAINT fk_post_author FOREIGN KEY (author_id) REFERENCES user(user_id)  ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 작성자별 조회/관리
CREATE INDEX idx_post_author_id      ON post (author_id);
-- 게시판별 최신순 리스트
CREATE INDEX idx_post_board_created  ON post (board_id, created_at DESC);
-- 작성자별 최신순
CREATE INDEX idx_post_author_created ON post (author_id, created_at);

-- CJK(한국어/일본어) 검색 품질 향상을 위한 ngram FULLTEXT
CREATE FULLTEXT INDEX ftx_post_title_content
ON post (title, content) WITH PARSER ngram;

-- 첨부 파일
CREATE TABLE attachment_file (
    -- 내부 식별자(PK)
    file_id       INT PRIMARY KEY AUTO_INCREMENT,
    -- 저장 URL(객체 스토리지 권장)
    file_url      VARCHAR(1024) NOT NULL,
    -- 원본 파일명(다운로드 시 표시)
    file_name     VARCHAR(255) NOT NULL,
    -- 파일 크기(Byte)
    file_size     INT NULL,
    -- 업로드 시각
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 다형성 댓글 테이블
CREATE TABLE comment (
    -- 댓글 내부 식별자(PK)
    comment_id   INT PRIMARY KEY AUTO_INCREMENT,
    -- 참조 유형(POST/PHOTO/REVIEW)
    ref_type     ENUM('POST','PHOTO','REVIEW') NOT NULL,
    -- 참조 대상 ID
    ref_id       INT NOT NULL,
    -- 작성자(FK, 삭제 RESTRICT → 고스트로 치환)
    author_id    INT NOT NULL,
    -- 부모 댓글 ID(대댓글). NULL이면 최상위 댓글.
    parent_id    INT NULL,
    -- 본문
    content      MEDIUMTEXT NOT NULL,
    -- QnA 답변 플래그(POST에서만 의미). 1개 이상이면 글 상태 ANSWERED.
    is_answer    TINYINT NOT NULL DEFAULT 0,
    -- 생성 시각
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 수정 시각
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- FK
    CONSTRAINT fk_cmt_author FOREIGN KEY (author_id) REFERENCES user(user_id)       ON DELETE RESTRICT,
    CONSTRAINT fk_cmt_parent FOREIGN KEY (parent_id) REFERENCES comment(comment_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 추후 댓글 삭제 시 대댓글 처리에 대한 정책 필요(현재는 RESTRICT)

-- 참조별 최신순
CREATE INDEX idx_comment_ref_created    ON comment (ref_type, ref_id, created_at);
-- 작성자별 최신순
CREATE INDEX idx_comment_author_created ON comment (author_id, created_at);
-- 작성자 보조 인덱스
CREATE INDEX idx_comment_author_id      ON comment (author_id);

-- 게시판 북마크
CREATE TABLE board_bookmark (
    -- 게시판(FK)
    board_id INT NOT NULL,
    -- 사용자(FK)
    user_id  INT NOT NULL,
    -- PK(복합): 1유저 1게시판 1회
    PRIMARY KEY (board_id, user_id),
    -- FK
    CONSTRAINT fk_bbm_board FOREIGN KEY (board_id) REFERENCES board(board_id) ON DELETE CASCADE,
    CONSTRAINT fk_bbm_user  FOREIGN KEY (user_id)  REFERENCES user(user_id)   ON DELETE CASCADE
) ENGINE=InnoDB;

-- 게시글 북마크
CREATE TABLE post_bookmark (
    -- 게시글(FK)
    post_id INT NOT NULL,
    -- 사용자(FK)
    user_id INT NOT NULL,
    -- PK(복합): 1유저 1게시글 1회
    PRIMARY KEY (post_id, user_id),
    -- FK
    CONSTRAINT fk_pbm_post FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_pbm_user FOREIGN KEY (user_id) REFERENCES user(user_id)   ON DELETE CASCADE
) ENGINE=InnoDB;

-- 게시글 좋아요
CREATE TABLE post_like (
    -- 게시글(FK)
    post_id INT NOT NULL,
    -- 사용자(FK)
    user_id INT NOT NULL,
    -- PK(복합): 1유저 1게시글 1회
    PRIMARY KEY (post_id, user_id),
    -- FK
    CONSTRAINT fk_plike_post FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE,
    CONSTRAINT fk_plike_user FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 다형성 신고 테이블
CREATE TABLE report (
    -- 내부 식별자(PK)
    report_id    INT PRIMARY KEY AUTO_INCREMENT,
    -- 신고자(FK, 삭제 RESTRICT → 고스트로 치환)
    reporter_id  INT NOT NULL,
    -- 참조 유형(POST/COMMENT/PHOTO/REVIEW)
    ref_type     ENUM('POST','COMMENT','PHOTO','REVIEW') NOT NULL,
    -- 참조 대상 ID
    ref_id       INT NOT NULL,
    -- 사유(스팸/욕설/광고/기타)
    reason       ENUM('SPAM','ABUSE','AD','OTHER') NOT NULL DEFAULT 'OTHER',
    -- 상세 설명(선택)
    description  MEDIUMTEXT NULL,
    -- 처리 상태(PENDING/RESOLVED/DISMISSED)
    status       ENUM('PENDING','RESOLVED','DISMISSED') NOT NULL DEFAULT 'PENDING',
    -- 신고 시각
    reported_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 처리 시각
    handled_at   DATETIME NULL,
    -- FK
    CONSTRAINT fk_cr_reporter FOREIGN KEY (reporter_id) REFERENCES user(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 신고 대상 삭제 시 어떻게 하지?

-- 신고 날짜별/상태별 최신순 인덱스
CREATE INDEX idx_report_status_reported ON report (status, reported_at DESC);

-- =============================================================
-- 4) 캘린더/문의
-- =============================================================

-- 일정 (캘린더)
CREATE TABLE event (
    -- 내부 식별자(PK)
    event_id       INT PRIMARY KEY AUTO_INCREMENT,
    -- GLOBAL: 공통 일정 / PERSONAL: 개인 일정
    visibility     ENUM('GLOBAL','PERSONAL') NOT NULL DEFAULT 'GLOBAL',
    -- 개인 일정일 때 소유자(FK, 삭제 시 함께 삭제)
    owner_user_id  INT NULL,
    -- 대상 기수(공통 일정 필터)
    cohort_no      INT NULL,
    -- 대상 반 범위(ALL/A/B)
    it_class_scope ENUM('ALL','A','B') NOT NULL DEFAULT 'ALL',
    -- 제목
    title          VARCHAR(150) NOT NULL,
    -- 설명(유의 사항 등)
    description    MEDIUMTEXT NULL,
    -- 시작 시각
    start_at       DATETIME NOT NULL,
    -- 종료 시각(> start_at)
    end_at         DATETIME NOT NULL,
    -- 종일 여부(1=종일)
    is_all_day     TINYINT NOT NULL DEFAULT 0,
    -- D-Day 표시 여부(홈 위젯 등)
    dday_enabled   TINYINT NOT NULL DEFAULT 0,
    -- 작성자(주로 관리자)
    created_by     INT NULL,
    -- 생성 시각
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 수정 시각
    updated_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 시간 일관성 제약
    CONSTRAINT chk_event_time CHECK (end_at > start_at),
    -- FK
    CONSTRAINT fk_event_owner   FOREIGN KEY (owner_user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_event_creator FOREIGN KEY (created_by)    REFERENCES user(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- GLOBAL/PERSONAL + 기간 조회 최적화
CREATE INDEX idx_event_visibility_time ON event (visibility, start_at, end_at);
-- 소유자 일정 조회
CREATE INDEX idx_event_owner_time      ON event (owner_user_id, start_at);

-- 문의(좌석 피드백 포함). 답변 테이블 없이 인라인 컬럼으로 단순화.
CREATE TABLE inquiry (
    -- 내부 식별자(PK)
    inquiry_id    INT PRIMARY KEY AUTO_INCREMENT,
    -- 작성자(FK, 삭제 RESTRICT → 고스트로 치환)
    user_id       INT NOT NULL,
    -- 카테고리(GENERAL/SEAT/RESERVATION/OTHER)
    category      ENUM('GENERAL','SEAT','RESERVATION','OTHER') NOT NULL DEFAULT 'GENERAL',
    -- 제목
    title       VARCHAR(150) NOT NULL,
    -- 본문(문의 내용)
    content       MEDIUMTEXT NOT NULL,
    -- 좌석 피드백 연계 시 좌석 ID(아래 좌석 생성 후 FK 추가)
    seat_id       INT NULL,
    -- 상태(OPEN/ANSWERED/CLOSED)
    status        ENUM('OPEN','ANSWERED','CLOSED') NOT NULL DEFAULT 'OPEN',
    -- 답변 본문(간단응답; 상세 스레딩 필요하면 comment 사용)
    answer_text   MEDIUMTEXT NULL,
    -- 답변자(FK, 삭제 시 NULL)
    answered_by   INT NULL,
    -- 답변 시각
    answered_at   DATETIME NULL,
    -- 생성 시각
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 수정 시각
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- FK
    CONSTRAINT fk_inq_user     FOREIGN KEY (user_id)     REFERENCES user(user_id)     ON DELETE RESTRICT,
    CONSTRAINT fk_inq_answerer FOREIGN KEY (answered_by) REFERENCES user(user_id)     ON DELETE SET NULL
) ENGINE=InnoDB;

-- 사용자별/상태별 최신순
CREATE INDEX idx_inquiry_user_status_created ON inquiry (user_id, status, created_at);

-- =============================================================
-- 5) 좌석/예약
-- =============================================================

-- 방 테이블
CREATE TABLE classroom (
    -- 내부 식별자(PK)
    room_id    INT PRIMARY KEY AUTO_INCREMENT,
    -- 방 이름(예: 4F-401, 4F-자습1)
    name       VARCHAR(100) NOT NULL,
    -- 유형: 강의실/자습/회의
    type       ENUM('CLASSROOM','STUDY','MEETING') NOT NULL DEFAULT 'CLASSROOM',
    -- 수용 인원(선택)
    capacity   INT NULL,
    -- 사용 여부(비활성 시 배정/예약 제한)
    is_active  TINYINT NOT NULL DEFAULT 1,
    -- 생성 시각
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 좌석맵/배정 테이블 없이 단일 seat로 축소 관리
CREATE TABLE seat (
    -- 내부 식별자(PK)
    seat_id          INT PRIMARY KEY AUTO_INCREMENT,
    -- 소속 방(FK)
    room_id          INT NOT NULL,
    -- 좌석 코드(표시용/유니크: 예 A-01)
    seat_code        VARCHAR(20) NOT NULL,
    -- UI 배치용 좌표(행)
    row_no           INT NULL,
    -- UI 배치용 좌표(열)
    col_no           INT NULL,
    -- 현재 배정 사용자(FK, 자리 비움은 NULL)
    assigned_user_id INT NULL,
    -- 배정 시각
    assigned_at      DATETIME NULL,
    -- 사용자당 1좌석만 허용(NULL은 제외)
    UNIQUE KEY uq_seat_user (assigned_user_id),
    -- FK
    CONSTRAINT fk_seat_room FOREIGN KEY (room_id)          REFERENCES classroom(room_id) ON DELETE CASCADE,
    CONSTRAINT fk_seat_user FOREIGN KEY (assigned_user_id) REFERENCES user(user_id)      ON DELETE SET NULL
) ENGINE=InnoDB;

-- (inquiry.seat_id) 좌석 FK 연결
ALTER TABLE inquiry
    ADD CONSTRAINT fk_inq_seat FOREIGN KEY (seat_id) REFERENCES seat(seat_id) ON DELETE SET NULL;

-- 예약 테이블
CREATE TABLE reservation (
    -- 내부 식별자(PK)
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    -- 방(FK)
    room_id        INT NOT NULL,
    -- 예약자(FK, 삭제 시 예약도 같이 삭제)
    user_id        INT NOT NULL,
    -- 시작 시각
    start_at       DATETIME NOT NULL,
    -- 종료 시각
    end_at         DATETIME NOT NULL,
    -- 상태(ACTIVE/취소/노쇼/차단)
    status         ENUM('ACTIVE','CANCELLED','NO_SHOW','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
    -- 생성 시각
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 시간 일관성
    CONSTRAINT chk_res_time CHECK (end_at > start_at),
    -- 1인 최대 120분 제약
    CONSTRAINT chk_res_duration CHECK (TIMESTAMPDIFF(MINUTE, start_at, end_at) BETWEEN 1 AND 120),
    -- FK
    CONSTRAINT fk_res_room FOREIGN KEY (room_id) REFERENCES classroom(room_id) ON DELETE CASCADE,
    CONSTRAINT fk_res_user FOREIGN KEY (user_id) REFERENCES user(user_id)      ON DELETE CASCADE
) ENGINE=InnoDB;

-- 방/시간대 조회
CREATE INDEX idx_res_room_time ON reservation (room_id, start_at, end_at);
-- 사용자/시간대 조회
CREATE INDEX idx_res_user_time ON reservation (user_id, start_at, end_at);

-- =============================================================
-- 6) 졸업생/강의/과제/사진
-- =============================================================

-- 회사(졸업생 리뷰용)
CREATE TABLE company (
    -- 내부 식별자(PK)
    company_id     INT PRIMARY KEY AUTO_INCREMENT,
    -- 회사명(유니크)
    name           VARCHAR(150) NOT NULL UNIQUE,
    -- 로고 URL(선택)
    logo_url       VARCHAR(500) NULL,
    -- 산업/계열(선택)
    sector         VARCHAR(100) NULL,
    -- 위치 텍스트(선택)
    location_text  VARCHAR(150) NULL,
    -- 인원수(표기용)
    headcount      INT NULL,
    -- 생성 시각
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 회사 리뷰 테이블
CREATE TABLE company_review (
    -- 내부 식별자(PK)
    review_id    INT PRIMARY KEY AUTO_INCREMENT,
    -- 회사(FK)
    company_id   INT NOT NULL,
    -- 리뷰어(FK, 삭제 RESTRICT → 고스트로 치환)
    reviewer_id  INT NULL,
    -- 평점(1~5)
    rating       TINYINT NOT NULL,
    -- 본문(선택)
    content         MEDIUMTEXT NULL,
    -- 작성 시각
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 제약
    CONSTRAINT chk_cr_rating CHECK (rating BETWEEN 1 AND 5),
    -- 1인 1회 리뷰
    UNIQUE KEY uq_review_once (company_id, reviewer_id),
    -- FK
    CONSTRAINT fk_cr_company FOREIGN KEY (company_id)  REFERENCES company(company_id)  ON DELETE CASCADE,
    CONSTRAINT fk_cr_user    FOREIGN KEY (reviewer_id) REFERENCES user(user_id)        ON DELETE SET NULL
) ENGINE=InnoDB;

-- 리뷰어별 조회
CREATE INDEX idx_cr_user            ON company_review (reviewer_id);
-- 회사별 최신순
CREATE INDEX idx_cr_company_created ON company_review (company_id, created_at);

-- 회사 평점 요약 뷰
CREATE OR REPLACE VIEW v_company_ratings AS
SELECT
    c.company_id,
    c.name,
    c.sector,
    c.location_text,
    c.headcount,
    AVG(r.rating) AS avg_rating,
    COUNT(r.review_id) AS review_count
FROM company c
LEFT JOIN company_review r ON r.company_id = c.company_id
GROUP BY c.company_id;

-- 회사 리뷰 좋아요
CREATE TABLE company_review_like (
    -- 리뷰(FK)
    review_id INT NOT NULL,
    -- 사용자(FK)
    user_id   INT NOT NULL,
    -- PK(복합): 1유저 1리뷰 1회
    PRIMARY KEY (review_id, user_id),
    -- FK
    CONSTRAINT fk_crlike_review FOREIGN KEY (review_id) REFERENCES company_review(review_id) ON DELETE CASCADE,
    CONSTRAINT fk_crlike_user   FOREIGN KEY (user_id)   REFERENCES user(user_id)             ON DELETE CASCADE
) ENGINE=InnoDB;

-- 과목(강의)
CREATE TABLE course (
    -- 내부 식별자(PK)
    course_id       INT PRIMARY KEY AUTO_INCREMENT,
    -- 과목명(예: Spring MVC)
    name            VARCHAR(150) NOT NULL,
    -- 담당 강사(FK, 삭제 시 NULL)
    instructor_id   INT NULL,
    -- 과목 트랙(IT/JP)
    course_type     ENUM('IT','JP') NOT NULL,
    -- 대상 기수(선택)
    cohort_no       INT NULL,
    -- 반(A/B, 선택)
    class_section   ENUM('A','B') NULL,
    -- 생성 시각
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- FK
    CONSTRAINT fk_course_instructor FOREIGN KEY (instructor_id) REFERENCES user(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 강사/기수/반 조회
CREATE INDEX idx_course_instructor ON course (instructor_id, cohort_no, class_section);
-- 기수/반/트랙 조회
CREATE INDEX idx_course_scope_type ON course (cohort_no, class_section, course_type);

-- 수강 등록(강의평가 가능 범위)
CREATE TABLE enrollment (
    -- 과목(FK)
    course_id INT NOT NULL,
    -- 수강자(FK)
    user_id   INT NOT NULL,
    -- PK(복합): 1과목 1유저 1레코드
    PRIMARY KEY (course_id, user_id),
    -- FK
    CONSTRAINT fk_enr_course FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_enr_user   FOREIGN KEY (user_id)   REFERENCES user(user_id)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- 유저별 수강 조회
CREATE INDEX idx_enr_user ON enrollment (user_id);

-- 강의 평가(6문항 1~5, 1인 1회)
CREATE TABLE course_evaluation (
    -- 내부 식별자(PK)
    evaluation_id INT PRIMARY KEY AUTO_INCREMENT,
    -- 과목(FK)
    course_id     INT NOT NULL,
    -- 평가자(FK, 삭제 RESTRICT → 고스트 치환)
    user_id       INT NULL,
    -- 성실성: 수업 준비 철저
    score_preparedness TINYINT NOT NULL,
    -- 전문성: 명확한 설명
    score_clarity     TINYINT NOT NULL,
    -- 명확성: 평가 기준/방식 객관·합리
    score_fairness    TINYINT NOT NULL,
    -- 소통성: 질문 수용/응답
    score_respond     TINYINT NOT NULL,
    -- 흥미도: 몰입/구성
    score_engagement  TINYINT NOT NULL,
    -- 열정성: 열정적으로 임함
    score_passion     TINYINT NOT NULL,
    -- 개선 의견(서술)
    comment_text      MEDIUMTEXT NULL,
    -- 작성 시각
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 점수 범위 제약
    CONSTRAINT chk_ce_scores CHECK (
        score_preparedness BETWEEN 1 AND 5 AND
        score_clarity      BETWEEN 1 AND 5 AND
        score_fairness     BETWEEN 1 AND 5 AND
        score_respond      BETWEEN 1 AND 5 AND
        score_engagement   BETWEEN 1 AND 5 AND
        score_passion      BETWEEN 1 AND 5
    ),
    -- 1과목 1유저 1회
    UNIQUE KEY uq_ce_once (course_id, user_id),
    -- FK
    CONSTRAINT fk_ce_course FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_ce_user   FOREIGN KEY (user_id)   REFERENCES user(user_id)     ON DELETE SET NULL
) ENGINE=InnoDB;

-- 평가자별 조회
CREATE INDEX idx_ce_user            ON course_evaluation (user_id);
-- 과목별 최신순
CREATE INDEX idx_ce_course_created  ON course_evaluation (course_id, created_at);

-- 과목별 평가 요약 뷰
CREATE OR REPLACE VIEW v_course_eval_avg AS
SELECT
    course_id,
    AVG(score_preparedness) AS avg_prepared,
    AVG(score_clarity)      AS avg_clarity,
    AVG(score_fairness)     AS avg_fairness,
    AVG(score_respond)      AS avg_respond,
    AVG(score_engagement)   AS avg_engagement,
    AVG(score_passion)      AS avg_passion,
    COUNT(*)                AS eval_count
FROM course_evaluation
GROUP BY course_id;

-- 강사별 과목 평가 요약 뷰
CREATE OR REPLACE VIEW v_instructor_eval_avg AS
SELECT
    c.instructor_id,
    c.course_id,
    c.name AS course_name,
    AVG(e.score_preparedness) AS avg_prepared,
    AVG(e.score_clarity)      AS avg_clarity,
    AVG(e.score_fairness)     AS avg_fairness,
    AVG(e.score_respond)      AS avg_respond,
    AVG(e.score_engagement)   AS avg_engagement,
    AVG(e.score_passion)      AS avg_passion,
    COUNT(e.evaluation_id)    AS eval_count
FROM course c
LEFT JOIN course_evaluation e ON e.course_id = c.course_id
GROUP BY c.instructor_id, c.course_id, c.name;

-- 과제
CREATE TABLE course_task (
    -- 내부 식별자(PK)
    task_id    INT PRIMARY KEY AUTO_INCREMENT,
    -- 과목(FK, 선택)
    course_id        INT NULL,
    -- 반(A/B, 선택)
    class_section    ENUM('A','B') NULL,
    -- 과제명
    name             VARCHAR(150) NOT NULL,
    -- 파일명 규칙(예: SCIT_47_B_홍길동_YYYYMMDD)
    filename_pattern VARCHAR(200) NULL,
    -- 마감 시각(선택)
    due_at           DATETIME NULL,
    -- 생성자(주로 강사/관리자)
    created_by       INT NOT NULL,
    -- 생성 시각
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- FK
    CONSTRAINT fk_asn_course  FOREIGN KEY (course_id)  REFERENCES course(course_id) ON DELETE SET NULL,
    CONSTRAINT fk_asn_creator FOREIGN KEY (created_by) REFERENCES user(user_id)     ON DELETE RESTRICT
    -- 강사 계정이 없어지면 고스트로 치환할 게 아니라 그냥 강사 계정을 비활성화시키면 되지 않을까?
) ENGINE=InnoDB;

-- 생성자별 조회
CREATE INDEX idx_asn_creator   ON course_task (created_by);
-- 반/마감 정렬
CREATE INDEX idx_asn_scope_due ON course_task (class_section, due_at);

-- 과제 제출(재제출 시 덮어쓰기)
CREATE TABLE course_task_submission (
    -- 내부 식별자(PK)
    submission_id     INT PRIMARY KEY AUTO_INCREMENT,
    -- 과제(FK)
    task_id           INT NOT NULL,
    -- 제출자(FK, 삭제 RESTRICT → 고스트 치환)
    user_id           INT NULL,
    -- 업로드 당시 원본 파일명
    original_filename VARCHAR(255) NOT NULL,
    -- 저장 파일명(서버/스토리지상의 유니크명)
    stored_filename   VARCHAR(255) NOT NULL,
    -- 저장 URL
    file_url          VARCHAR(1024) NOT NULL,
    -- 업로드 시각
    uploaded_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 갱신 시각(재제출 시)
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    -- 동일 과제+유저 1건만 허용(재제출은 UPDATE)
    UNIQUE KEY uq_submission_once (task_id, user_id),
    -- FK
    CONSTRAINT fk_sub_task FOREIGN KEY (task_id) REFERENCES course_task(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_sub_user FOREIGN KEY (user_id) REFERENCES user(user_id)        ON DELETE SET NULL
) ENGINE=InnoDB;

-- 제출자별 조회
CREATE INDEX idx_sub_user        ON course_task_submission (user_id);
-- 과제별 조회
CREATE INDEX idx_sub_task        ON course_task_submission (task_id);

-- 사진(앨범 테이블 제거 → album_name으로 그룹핑)
CREATE TABLE photo (
    -- 내부 식별자(PK)
    photo_id     INT PRIMARY KEY AUTO_INCREMENT,
    -- 앨범 이름(자유 텍스트, 예: '47기 OT')
    album_name   VARCHAR(150) NULL,
    -- 업로더(FK, 삭제 RESTRICT → 고스트 치환)
    uploader_id  INT NOT NULL,
    -- 파일 URL
    file_url     VARCHAR(1024) NOT NULL,
    -- 캡션(짧은 설명)
    caption      VARCHAR(255) NULL,
    -- 업로드 시각
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- FK
    CONSTRAINT fk_ph_uploader FOREIGN KEY (uploader_id) REFERENCES user(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 앨범별 최신순
CREATE INDEX idx_ph_album_created ON photo (album_name, created_at);

-- =============================================================
-- 트리거: 고스트 유저 처리 + Q&A 동기화
-- =============================================================

DELIMITER //

-- 사용자 삭제 전, RESTRICT FK를 고스트로 치환하여 삭제 진행 가능하게 만듦
DROP TRIGGER IF EXISTS trg_user_before_delete//
CREATE TRIGGER trg_user_before_delete
BEFORE DELETE ON user
FOR EACH ROW
BEGIN
    DECLARE v_ghost INT;

    -- 고스트 계정 자체 삭제 방지
    IF OLD.username = '__deleted_user__' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cannot delete ghost user';
    END IF;

    -- 고스트 user_id 조회(없으면 오류)
    SELECT user_id INTO v_ghost
    FROM user
    WHERE username = '__deleted_user__'
    LIMIT 1;

    IF v_ghost IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ghost user not found';
    END IF;

    -- 쪽지 발신/수신자 고스트로 교체
    UPDATE direct_message SET sender_id = v_ghost   WHERE sender_id   = OLD.user_id;
    UPDATE direct_message SET receiver_id = v_ghost WHERE receiver_id = OLD.user_id;

    -- 게시글/댓글 작성자 고스트로 교체
    UPDATE post    SET author_id   = v_ghost WHERE author_id   = OLD.user_id;
    UPDATE comment SET author_id   = v_ghost WHERE author_id   = OLD.user_id;

    -- 문의 작성자 고스트로 교체(답변자는 nullable이므로 FK 단에서 NULL 처리됨)
    UPDATE inquiry SET user_id     = v_ghost WHERE user_id     = OLD.user_id;

    -- 신고 작성자 고스트로 교체
    UPDATE report SET reporter_id = v_ghost  WHERE reporter_id = OLD.user_id;

    -- 과제/사진 업로더
    UPDATE course_task           SET created_by  = v_ghost WHERE created_by  = OLD.user_id;
    UPDATE photo                 SET uploader_id = v_ghost WHERE uploader_id = OLD.user_id;

    -- ★ UNIQUE 충돌 방지: 익명화만 (NULL)
    UPDATE company_review         SET reviewer_id = NULL WHERE reviewer_id = OLD.user_id;
    UPDATE course_evaluation      SET user_id     = NULL WHERE user_id     = OLD.user_id;
    UPDATE course_task_submission SET user_id     = NULL WHERE user_id     = OLD.user_id;

    -- 좌석 배정: 자연스러운 '자리 비움' 처리
    UPDATE seat SET assigned_user_id = NULL, assigned_at = NULL
    WHERE assigned_user_id = OLD.user_id;

    -- reaction, reservation 등 CASCADE 처리는 별도 교체 불필요
END//

DELIMITER ;

-- =============================================================
-- 끝. (ENGINE=InnoDB / FK 정합성 / 핵심 인덱스 포함)
-- =============================================================

-- 하단 트리거는 추후에 체크(필요 시)

-- ========= 보조 인덱스 =========
-- 이벤트: 공통 일정 스코프 + 시간대 조회 최적화
CREATE INDEX idx_event_scope_time ON event (cohort_no, it_class_scope, start_at);

-- ========= 폴리모픽 객체 삭제 시 정리 트리거 =========
DELIMITER //

-- 게시글 삭제 → 댓글 정리
DROP TRIGGER IF EXISTS trg_post_ad_cascade//
CREATE TRIGGER trg_post_ad_cascade
AFTER DELETE ON post
FOR EACH ROW
BEGIN
    DELETE FROM comment    WHERE ref_id=OLD.post_id;
END//

-- 사진 삭제 → 첨부/댓글/리액션 정리
DROP TRIGGER IF EXISTS trg_photo_ad_cascade//
CREATE TRIGGER trg_photo_ad_cascade
AFTER DELETE ON photo
FOR EACH ROW
BEGIN
    DELETE FROM attachment_file WHERE ref_id=OLD.photo_id;
    DELETE FROM comment    WHERE ref_id=OLD.photo_id;
END//

-- 회사 리뷰 삭제 → 댓글/리액션 정리
DROP TRIGGER IF EXISTS trg_company_review_ad_cascade//
CREATE TRIGGER trg_company_review_ad_cascade
AFTER DELETE ON company_review
FOR EACH ROW
BEGIN
    DELETE FROM comment  WHERE ref_type='REVIEW' AND ref_id=OLD.review_id;
END//

-- 예약 삭제 → 리액션(신고) 정리
DROP TRIGGER IF EXISTS trg_reservation_ad_cascade//
CREATE TRIGGER trg_reservation_ad_cascade
AFTER DELETE ON reservation
FOR EACH ROW
BEGIN
    DELETE FROM reaction WHERE ref_type='RESERVATION' AND ref_id=OLD.reservation_id;
END//

-- ========= 예약 신고(3회→1주 차단) 동기화 트리거 =========
-- 규칙:
-- - 같은 '예약'에 대해 'CONFIRMED'가 1개 이상이어도 strike는 "1건"만 카운트
-- - PENDING→CONFIRMED 전환 시: 현재 CONFIRMED 수가 1개(=이번이 첫 확정)일 때만 +1
-- - CONFIRMED→(기타) 전환/삭제 시: 해당 예약의 CONFIRMED 수가 0이 되면 -1
-- - 누적 3회 이상인 순간 banned_until = NOW()+7 DAY (이미 금지 중이면 더 멀면 그대로 유지)

-- 리액션 등록 시, 예약 신고(3회→1주 차단) 동기화
DROP TRIGGER IF EXISTS trg_react_ai_reservation_report//
CREATE TRIGGER trg_react_ai_reservation_report
AFTER INSERT ON reaction
FOR EACH ROW
BEGIN
    DECLARE v_target_user INT;
    DECLARE v_cnt INT DEFAULT 0;

    IF NEW.ref_type='RESERVATION' AND NEW.type='REPORT' AND NEW.status='CONFIRMED' THEN
        SELECT user_id INTO v_target_user FROM reservation
        WHERE reservation_id=NEW.ref_id LIMIT 1;

        IF v_target_user IS NOT NULL THEN
        SELECT COUNT(*) INTO v_cnt
            FROM reaction
        WHERE ref_type='RESERVATION' AND type='REPORT'
            AND ref_id=NEW.ref_id AND status='CONFIRMED';
        IF v_cnt=1 THEN
            UPDATE user
            SET resv_strike_count = resv_strike_count + 1,
                resv_banned_until = CASE
                    WHEN resv_strike_count + 1 >= 3
                    THEN GREATEST(IFNULL(resv_banned_until,'1970-01-01'),
                                    NOW() + INTERVAL 7 DAY)
                    ELSE resv_banned_until
                END
            WHERE user_id=v_target_user;
        END IF;
        END IF;
    END IF;
END//

-- 리액션 수정 시, 예약 신고(3회→1주 차단) 동기화
DROP TRIGGER IF EXISTS trg_react_au_reservation_report//
CREATE TRIGGER trg_react_au_reservation_report
AFTER UPDATE ON reaction
FOR EACH ROW
BEGIN
    DECLARE v_target_user INT;
    DECLARE v_cnt INT DEFAULT 0;

    IF NEW.ref_type='RESERVATION' AND NEW.type='REPORT' THEN
        -- 대상 예약자 조회
        SELECT user_id INTO v_target_user
        FROM reservation WHERE reservation_id = NEW.ref_id LIMIT 1;

        -- PENDING/REJECTED → CONFIRMED : 첫 확정이면 +1
        IF IFNULL(OLD.status,'PENDING') <> 'CONFIRMED'
        AND NEW.status='CONFIRMED'
        AND v_target_user IS NOT NULL THEN
        SELECT COUNT(*) INTO v_cnt
            FROM reaction
        WHERE ref_type='RESERVATION' AND type='REPORT'
            AND ref_id=NEW.ref_id AND status='CONFIRMED';
        IF v_cnt = 1 THEN
            UPDATE user
            SET resv_strike_count = resv_strike_count + 1,
                resv_banned_until = CASE
                    WHEN resv_strike_count + 1 >= 3
                        THEN GREATEST(IFNULL(resv_banned_until, '1970-01-01'),
                                        NOW() + INTERVAL 7 DAY)
                    ELSE resv_banned_until
                END
            WHERE user_id = v_target_user;
        END IF;
        END IF;

        -- CONFIRMED → PENDING/REJECTED : 더 이상 확정 없으면 -1
        IF OLD.status='CONFIRMED'
        AND IFNULL(NEW.status,'PENDING') <> 'CONFIRMED'
        AND v_target_user IS NOT NULL THEN
        SELECT COUNT(*) INTO v_cnt
            FROM reaction
        WHERE ref_type='RESERVATION' AND type='REPORT'
            AND ref_id=NEW.ref_id AND status='CONFIRMED';
        IF v_cnt = 0 THEN
            UPDATE user
            SET resv_strike_count = GREATEST(resv_strike_count - 1, 0)
            WHERE user_id = v_target_user;
        END IF;
        END IF;
    END IF;
END//

-- 리액션 삭제 → 예약 신고(3회→1주 차단) 정리
DROP TRIGGER IF EXISTS trg_react_ad_reservation_report//
CREATE TRIGGER trg_react_ad_reservation_report
AFTER DELETE ON reaction
FOR EACH ROW
BEGIN
    DECLARE v_target_user INT;
    DECLARE v_cnt INT DEFAULT 0;

    IF OLD.ref_type='RESERVATION' AND OLD.type='REPORT' AND OLD.status='CONFIRMED' THEN
        SELECT user_id INTO v_target_user
        FROM reservation WHERE reservation_id = OLD.ref_id LIMIT 1;

        IF v_target_user IS NOT NULL THEN
        SELECT COUNT(*) INTO v_cnt
            FROM reaction
        WHERE ref_type='RESERVATION' AND type='REPORT'
            AND ref_id=OLD.ref_id AND status='CONFIRMED';
        IF v_cnt = 0 THEN
            UPDATE user
            SET resv_strike_count = GREATEST(resv_strike_count - 1, 0)
            WHERE user_id = v_target_user;
        END IF;
        END IF;
    END IF;
END//

-- ========= Q&A 상태 동기화(해당 게시판이 Q&A일 때만) =========
-- 댓글 등록 시, Q&A 상태 동기화
DROP TRIGGER IF EXISTS trg_cmt_ai_qna//
CREATE TRIGGER trg_cmt_ai_qna
AFTER INSERT ON comment
FOR EACH ROW
BEGIN
    IF NEW.ref_type='POST' AND NEW.is_answer=1 AND EXISTS (
        SELECT 1 FROM post p JOIN board b ON b.board_id=p.board_id
        WHERE p.post_id=NEW.ref_id AND b.is_qna=1
    ) THEN
        UPDATE post SET answer_status='ANSWERED' WHERE post_id=NEW.ref_id;
    END IF;
END//

-- 댓글 수정 시, Q&A 상태 동기화
DROP TRIGGER IF EXISTS trg_cmt_au_qna//
CREATE TRIGGER trg_cmt_au_qna
AFTER UPDATE ON comment
FOR EACH ROW
BEGIN
    DECLARE v_cnt INT DEFAULT 0;

    IF NEW.ref_type='POST' AND EXISTS (
        SELECT 1 FROM post p JOIN board b ON b.board_id=p.board_id
        WHERE p.post_id=NEW.ref_id AND b.is_qna=1
    ) THEN
        IF NEW.is_answer=1 AND IFNULL(OLD.is_answer,0)=0 THEN
        UPDATE post SET answer_status='ANSWERED' WHERE post_id=NEW.ref_id;
        END IF;

        IF OLD.is_answer=1 AND IFNULL(NEW.is_answer,0)=0 THEN
        SELECT COUNT(*) INTO v_cnt
            FROM comment
        WHERE ref_type='POST' AND ref_id=NEW.ref_id AND is_answer=1;
        IF v_cnt=0 THEN
            UPDATE post SET answer_status='PENDING' WHERE post_id=NEW.ref_id;
        END IF;
        END IF;
    END IF;
END//

-- 댓글 삭제 시, Q&A 상태 동기화
DROP TRIGGER IF EXISTS trg_cmt_ad_qna//
CREATE TRIGGER trg_cmt_ad_qna
AFTER DELETE ON comment
FOR EACH ROW
BEGIN
    DECLARE v_cnt INT DEFAULT 0;

    IF OLD.ref_type='POST' AND OLD.is_answer=1 AND EXISTS (
        SELECT 1 FROM post p JOIN board b ON b.board_id=p.board_id
        WHERE p.post_id=OLD.ref_id AND b.is_qna=1
    ) THEN
        SELECT COUNT(*) INTO v_cnt
        FROM comment
        WHERE ref_type='POST' AND ref_id=OLD.ref_id AND is_answer=1;
        IF v_cnt=0 THEN
        UPDATE post SET answer_status='PENDING' WHERE post_id=OLD.ref_id;
        END IF;
    END IF;
END//

-- ========= 좌석 배정 ↔ 사용자(기수/반) 정합성 =========
-- 좌석 배정 시, 사용자(기수/반) 정합성 검증
DROP TRIGGER IF EXISTS trg_seat_bi_validate//
CREATE TRIGGER trg_seat_bi_validate
BEFORE INSERT ON seat
FOR EACH ROW
BEGIN
    DECLARE v_cohort INT; DECLARE v_class CHAR(1);
    IF NEW.assigned_user_id IS NOT NULL THEN
        SELECT cohort_no, it_class INTO v_cohort, v_class
            FROM user WHERE user_id=NEW.assigned_user_id;
        IF v_cohort IS NULL OR v_class IS NULL
            OR v_cohort <> NEW.cohort_no OR v_class <> NEW.class_section THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Seat assignee must match seat cohort/class';
        END IF;
    END IF;
END//

-- 좌석 변경 시, 사용자(기수/반) 정합성 검증
DROP TRIGGER IF EXISTS trg_seat_bu_validate//
CREATE TRIGGER trg_seat_bu_validate
BEFORE UPDATE ON seat
FOR EACH ROW
BEGIN
    DECLARE v_cohort INT; DECLARE v_class CHAR(1);
    IF NEW.assigned_user_id IS NOT NULL THEN
        SELECT cohort_no, it_class INTO v_cohort, v_class
            FROM user WHERE user_id=NEW.assigned_user_id;
        IF v_cohort IS NULL OR v_class IS NULL
            OR v_cohort <> NEW.cohort_no OR v_class <> NEW.class_section THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Seat assignee must match seat cohort/class';
        END IF;
    END IF;
END//

-- 사용자의 기수/반 변경 시, 맞지 않는 좌석 자동 해제
DROP TRIGGER IF EXISTS trg_user_au_seat_unassign//
CREATE TRIGGER trg_user_au_seat_unassign
AFTER UPDATE ON user
FOR EACH ROW
BEGIN
    IF (OLD.cohort_no <> NEW.cohort_no) OR (OLD.it_class <> NEW.it_class) THEN
        UPDATE seat
        SET assigned_user_id = NULL, assigned_at = NULL
        WHERE assigned_user_id = NEW.user_id
        AND (cohort_no <> NEW.cohort_no OR class_section <> NEW.it_class);
    END IF;
END//

-- ========= 사용자 ↔ 소속 조 정합성 검증 =========
-- 사용자 등록 시, 소속 그룹 정합성 검증
DROP TRIGGER IF EXISTS trg_user_bi_group_validate//
CREATE TRIGGER trg_user_bi_group_validate
BEFORE INSERT ON user
FOR EACH ROW
BEGIN
    IF NEW.group_id IS NOT NULL THEN
        IF NOT EXISTS (
        SELECT 1 FROM student_group g
        WHERE g.group_id=NEW.group_id
            AND g.cohort_no=NEW.cohort_no
            AND g.class_section=NEW.it_class
        ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT='User group must match user cohort/class';
        END IF;
    END IF;
END//

-- 사용자 수정 시, 소속 그룹 정합성 검증
DROP TRIGGER IF EXISTS trg_user_bu_group_validate//
CREATE TRIGGER trg_user_bu_group_validate
BEFORE UPDATE ON user
FOR EACH ROW
BEGIN
    IF NEW.group_id IS NOT NULL THEN
        IF NOT EXISTS (
        SELECT 1 FROM student_group g
        WHERE g.group_id=NEW.group_id
            AND g.cohort_no=NEW.cohort_no
            AND g.class_section=NEW.it_class
        ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT='User group must match user cohort/class';
        END IF;
    END IF;
END//

DELIMITER ;

-- 권장 보조 인덱스(집계/조회용)
CREATE INDEX idx_react_ref  ON reaction (ref_type, ref_id, type);
CREATE INDEX idx_react_user ON reaction (user_id, created_at);
CREATE INDEX idx_react_reservation ON reaction (ref_type, type, ref_id, status);

DELIMITER //

-- 예약 관련 트리거
-- 사용자가 1주 차단 중일 때 예약 불가
DROP TRIGGER IF EXISTS trg_res_bi_enforce_ban//
CREATE TRIGGER trg_res_bi_enforce_ban
BEFORE INSERT ON reservation
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM user
        WHERE user_id = NEW.user_id
        AND resv_banned_until IS NOT NULL
        AND resv_banned_until > NOW()
    ) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'User is temporarily banned from making reservation';
    END IF;
END//

DELIMITER ;