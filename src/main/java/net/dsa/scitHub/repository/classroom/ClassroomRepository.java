package net.dsa.scitHub.repository.classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.dsa.scitHub.entity.classroom.Classroom;
import net.dsa.scitHub.enums.ClassroomType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Integer> {

    /** 강의실 유형별 조회 */
    List<Classroom> findByType(ClassroomType type);

    /** 활성화된 강의실만 조회 */
    @Query("SELECT c FROM Classroom c WHERE c.isActive = true")
    List<Classroom> findActiveClassrooms();

    /** 활성화 상태와 유형으로 조회 */
    List<Classroom> findByIsActiveAndType(Boolean isActive, ClassroomType type);

    /** 강의실 이름으로 검색 */
    List<Classroom> findByNameContaining(String name);

    /** 강의실 이름으로 정확히 조회 */
    Optional<Classroom> findByName(String name);

    /** 활성화된 특정 유형의 강의실만 조회 */
    @Query("SELECT c FROM Classroom c WHERE c.isActive = true AND c.type = :type")
    List<Classroom> findActiveClassroomsByType(@Param("type") ClassroomType type);

    /** 특정 시간대에 예약 가능한 강의실 조회 */
    @Query("SELECT c FROM Classroom c WHERE c.isActive = true AND c.classroomId NOT IN " +
           "(SELECT r.classroom.classroomId FROM Reservation r WHERE " +
           "r.startAt < :endTime AND r.endAt > :startTime)")
    List<Classroom> findAvailableClassrooms(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime);

    /** 예약 수와 함께 강의실 조회 */
    @Query("SELECT c, COUNT(r) FROM Classroom c LEFT JOIN c.reservations r GROUP BY c ORDER BY COUNT(r) DESC")
    List<Object[]> findClassroomsWithReservationCount();
}
