package net.dsa.scitHub.repository.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.dsa.scitHub.entity.reservation.Reservation;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    
    /** 사용자별 예약 조회 */
    List<Reservation> findByUser_UserId(Integer userId);
    
    /** 강의실별 예약 조회 */
    List<Reservation> findByClassroom_ClassroomId(Integer classroomId);
    
    /** 시간 충돌하는 예약 조회 */
    @Query("SELECT r FROM Reservation r WHERE r.classroom.classroomId = :classroomId AND r.startAt < :endAt AND r.endAt > :startAt")
    List<Reservation> findConflictingReservations(@Param("classroomId") Integer classroomId, 
                                                  @Param("startAt") LocalDateTime startAt, 
                                                  @Param("endAt") LocalDateTime endAt);
    
    /** 특정 날짜 범위의 예약 조회 */
    @Query("SELECT r FROM Reservation r WHERE r.startAt >= :start AND r.endAt <= :end")
    List<Reservation> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /** 특정 사용자의 특정 날짜 범위 예약 조회 */
    @Query("SELECT r FROM Reservation r WHERE r.user.userId = :userId AND r.startAt >= :startOfDay AND r.startAt < :endOfDay")
    List<Reservation> findByUserAndDate(@Param("userId") Integer userId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /** 현재 진행 중인 예약 조회 */
    @Query("SELECT r FROM Reservation r WHERE r.startAt <= :now AND r.endAt > :now")
    List<Reservation> findOngoingReservations(@Param("now") LocalDateTime now);
    
    /** 다가올 예약 조회 */
    @Query("SELECT r FROM Reservation r WHERE r.startAt > :now ORDER BY r.startAt")
    List<Reservation> findUpcomingReservations(@Param("now") LocalDateTime now);
    
    /** 지난 예약 조회 */
    @Query("SELECT r FROM Reservation r WHERE r.endAt < :now ORDER BY r.endAt DESC")
    List<Reservation> findPastReservations(@Param("now") LocalDateTime now);
    
    /** 사용자별 다가올 예약 조회 */
    @Query("SELECT r FROM Reservation r WHERE r.user.userId = :userId AND r.startAt > :now ORDER BY r.startAt")
    List<Reservation> findUpcomingReservationsByUser(@Param("userId") Integer userId, @Param("now") LocalDateTime now);
    
    /** 강의실별 예약 수 조회 */
    @Query("SELECT r.classroom.classroomId, COUNT(r) FROM Reservation r GROUP BY r.classroom.classroomId ORDER BY COUNT(r) DESC")
    List<Object[]> countReservationsByClassroom();
    
    /** 사용자별 예약 수 조회 */
    @Query("SELECT r.user.userId, COUNT(r) FROM Reservation r GROUP BY r.user.userId ORDER BY COUNT(r) DESC")
    List<Object[]> countReservationsByUser();
    
    /** 특정 시간대의 강의실 가용성 체크 */
    @Query("SELECT c.classroomId FROM Classroom c WHERE c.isActive = true AND c.classroomId NOT IN " +
           "(SELECT r.classroom.classroomId FROM Reservation r WHERE r.startAt < :endTime AND r.endAt > :startTime)")
    List<Integer> findAvailableClassroomIds(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /** 특정 기간의 예약 통계 */
    @Query("SELECT DATE(r.startAt), COUNT(r) FROM Reservation r WHERE r.startAt BETWEEN :startDate AND :endDate GROUP BY DATE(r.startAt) ORDER BY DATE(r.startAt)")
    List<Object[]> findReservationStatsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
