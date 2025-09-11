package net.dsa.scitHub.entity.classroom;

import jakarta.persistence.*;
import lombok.*;
import net.dsa.scitHub.entity.reservation.Reservation;
import net.dsa.scitHub.enums.ClassroomType;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "classroom")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"reservations", "seats"})
public class Classroom {

    /** 강의실 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "classroom_id")
    private Integer classroomId;

    /** 강의실 이름 */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /** 강의실 유형 (CLASSROOM: 강의실, STUDYROOM: 스터디룸) */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ClassroomType type = ClassroomType.CLASSROOM;

    /** 강의실 활성화 여부 */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** 강의실 예약 목록 */
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL)
    private List<Reservation> reservations;

    /** 강의실 좌석 목록 */
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL)
    private List<Seat> seats;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Classroom)) return false;
        Classroom classroom = (Classroom) o;
        return Objects.equals(classroomId, classroom.classroomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classroomId);
    }
}

