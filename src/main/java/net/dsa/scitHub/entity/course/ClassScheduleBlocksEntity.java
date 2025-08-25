package net.dsa.scitHub.entity.course;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "class_schedule_blocks",
    indexes = @Index(name = "idx_csb_scope", columnList = "cohort_no, class_section, label, is_active")
)
public class ClassScheduleBlocksEntity {

    public enum ClassSection { A, B }
    public enum Label { IT, JP, LUNCH, REVIEW, SELF_STUDY }
    public enum Day { MON, TUE, WED, THU, FRI, SAT, SUN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "block_id", columnDefinition = "int unsigned")
    private Integer blockId;

    @Column(name = "cohort_no")
    private Integer cohortNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_section", nullable = false, length = 1)
    private ClassSection classSection;

    @Enumerated(EnumType.STRING)
    @Column(name = "label", nullable = false, length = 12)
    private Label label;

    // MySQL SET('MON','TUE','WED','THU','FRI','SAT','SUN') ↔ EnumSet<Day>
    @Convert(converter = DaysSetConverter.class)
    @Column(name = "days", nullable = false) // DB는 SET 타입 — JPA는 문자열로 저장/로드
    private java.util.Set<Day> days;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint default 1")
    private Boolean isActive;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (isActive == null) isActive = true;
    }

    // ===== Converter: EnumSet<Day> <-> "MON,TUE,..." =====
    @Converter
    public static class DaysSetConverter implements AttributeConverter<Set<Day>, String> {
        @Override
        public String convertToDatabaseColumn(Set<Day> attribute) {
            if (attribute == null || attribute.isEmpty()) return "";
            return attribute.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
        }
        @Override
        public Set<Day> convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.isBlank()) return EnumSet.noneOf(Day.class);
            Set<Day> set = EnumSet.noneOf(Day.class);
            for (String s : dbData.split(",")) {
                set.add(Day.valueOf(s.trim()));
            }
            return set;
        }
    }
}
