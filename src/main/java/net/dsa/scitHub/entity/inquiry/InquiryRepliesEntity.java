package net.dsa.scitHub.entity.inquiry;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.*;

import net.dsa.scitHub.entity.user.UsersEntity;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "inquiry_replies",
    indexes = {
        @Index(name = "idx_ir_inquiry_created", columnList = "inquiry_id, created_at")
    }
)
public class InquiryRepliesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id", columnDefinition = "int unsigned")
    private Integer replyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "inquiry_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_ir_inq")
    )
    private InquiriesEntity inquiry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "responder_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_ir_user")
    )
    private UsersEntity responder;

    @Lob
    @Column(name = "body", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String body;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}
