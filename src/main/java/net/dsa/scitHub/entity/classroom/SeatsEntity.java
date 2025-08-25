package net.dsa.scitHub.entity.classroom;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;
import java.util.ArrayList;
import net.dsa.scitHub.entity.inquiry.InquiriesEntity;


@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(
    name = "seats",
    uniqueConstraints = @UniqueConstraint(name = "uq_seat_code", columnNames = {"seat_map_id","seat_code"})
)
public class SeatsEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id", columnDefinition = "int unsigned")
    private Integer seatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seat_map_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_seat_map"))
    private SeatMapsEntity seatMap;

    @Size(max = 20)
    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;

    @Column(name = "row_no")
    private Integer rowNo;

    @Column(name = "col_no")
    private Integer colNo;

    // 좌석 - 좌석 배정
    @Builder.Default
    @OneToMany(mappedBy = "seat", fetch = FetchType.LAZY)
    private List<SeatAssignmentsEntity> assignments = new ArrayList<>();

    // 좌석 - 문의
    @Builder.Default
    @OneToMany(mappedBy = "seat", fetch = FetchType.LAZY)
    private List<InquiriesEntity> inquiries = new ArrayList<>();
}
