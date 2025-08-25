package net.dsa.scitHub.entity.assignment;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "assignment_submissions",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_submission_once",
        columnNames = {"assignment_id","user_id"}
    ),
    indexes = {
        @Index(name = "idx_sub_asn_updated", columnList = "assignment_id, updated_at")
    }
)
public class AssignmentSubmissionsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id", columnDefinition = "int unsigned")
    private Integer submissionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_sub_assignment"))
    private AssignmentsEntity assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
        foreignKey = @ForeignKey(name = "fk_sub_user"))
    private UsersEntity user;

    @Size(max = 255)
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Size(max = 255)
    @Column(name = "stored_filename", nullable = false, length = 255)
    private String storedFilename;

    @Size(max = 1024)
    @Column(name = "file_url", nullable = false, length = 1024)
    private String fileUrl;

    @CreatedDate
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

}
