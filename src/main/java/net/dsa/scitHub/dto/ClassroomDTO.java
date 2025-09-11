package net.dsa.scitHub.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dsa.scitHub.enums.ClassroomType;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDTO {
    private Integer classroomId;
    private String name;
    private ClassroomType type;
    private Boolean isActive;
    private List<ReservationDTO> reservations;
}
