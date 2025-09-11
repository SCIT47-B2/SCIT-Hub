package net.dsa.scitHub.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dsa.scitHub.dto.ReservationDTO;
import net.dsa.scitHub.entity.classroom.Classroom;
import net.dsa.scitHub.entity.reservation.Reservation;
import net.dsa.scitHub.entity.user.User;
import net.dsa.scitHub.repository.classroom.ClassroomRepository;
import net.dsa.scitHub.repository.reservation.ReservationRepository;
import net.dsa.scitHub.repository.user.UserRepository;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ReservationService {

    @Autowired
    private final ReservationRepository rr;
    private final ClassroomRepository cr;
    private final UserRepository ur;

    /**
     * 특정 강의실의 날짜별 예약 목록 조회
     * @param classroomId 강의실 ID
     * @param date 조회할 날짜
     * @return 예약 DTO 목록
     */
    public List<ReservationDTO> getReservationsForClassroomByDate(Integer classroomId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Reservation> reservations = rr.findByDateRange(startOfDay, endOfDay).stream()
                .filter(r -> r.getClassroom().getClassroomId().equals(classroomId))
                .collect(Collectors.toList());
        return reservations.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    /**
     * 새로운 예약 생성
     * @param user 예약자
     * @param requestDTO 예약 요청 정보
     * @return 생성된 예약 DTO
     */
    public ReservationDTO createReservation(User user, ReservationDTO requestDTO) {
        // 1. 강의실 존재 여부 확인
        Classroom classroom = cr.findById(requestDTO.getClassroomId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의실입니다."));

        // 2. 하루에 한 번만 예약 가능
        LocalDate today = requestDTO.getStartAt().toLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        long userReservationsToday = rr.findByUserAndDate(user.getUserId(), startOfDay, endOfDay).size();
        if (userReservationsToday > 0) {
            throw new IllegalArgumentException("하루에 한 번만 예약할 수 있습니다.");
        }

        // 3. 예약 시간 중복 확인
        List<Reservation> conflictingReservations = rr.findConflictingReservations(
                requestDTO.getClassroomId(), requestDTO.getStartAt(), requestDTO.getEndAt());

        if (!conflictingReservations.isEmpty()) {
            throw new IllegalArgumentException("이미 예약된 시간입니다.");
        }

        // 4. 예약 엔티티 생성 및 저장
        Reservation newReservation = Reservation.builder()
                .classroom(classroom)
                .user(user)
                .startAt(requestDTO.getStartAt())
                .endAt(requestDTO.getEndAt())
                .build();

        Reservation savedReservation = rr.save(newReservation);
        return convertToDto(savedReservation);
    }

    /**
     * 예약 삭제
     * @param user 요청한 사용자
     * @param reservationId 삭제할 예약 ID
     */
    public void deleteReservation(User user, Integer reservationId) {
        Reservation reservation = rr.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // 본인 예약만 삭제 가능
        if (!reservation.getUser().getUserId().equals(user.getUserId())) {
            throw new AccessDeniedException("예약을 취소할 권한이 없습니다.");
        }

        rr.delete(reservation);
    }

    /**
     * Reservation 엔티티를 ReservationDTO로 변환
     * @param entity Reservation
     * @return ReservationDTO
     */
    private ReservationDTO convertToDto(Reservation entity) {
        if (entity == null) {
            return null;
        }
        User user = entity.getUser();
        Classroom classroom = entity.getClassroom();

        return ReservationDTO.builder()
                .reservationId(entity.getReservationId())
                .classroomId(classroom != null ? classroom.getClassroomId() : null)
                .userId(user != null ? user.getUserId() : null)
                .userNameKor(user != null ? user.getNameKor() : "알 수 없음")
                .startAt(entity.getStartAt())
                .endAt(entity.getEndAt())
                .build();
    }



    
}
