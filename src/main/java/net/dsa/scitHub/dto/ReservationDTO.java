package net.dsa.scitHub.dto;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {
    private Integer reservationId;
    private Integer classroomId;
    private Integer userId;
    private String userNameKor;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
