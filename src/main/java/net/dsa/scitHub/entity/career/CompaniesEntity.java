package net.dsa.scitHub.entity.career;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "companies",
    uniqueConstraints = @UniqueConstraint(name = "uk_companies_name", columnNames = "name")
)
public class CompaniesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id", columnDefinition = "int unsigned")
    private Integer companyId;

    @Size(max = 150)
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Size(max = 500)
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Size(max = 100)
    @Column(name = "sector", length = 100)
    private String sector;

    @Size(max = 150)
    @Column(name = "location_text", length = 150)
    private String locationText;

    @Column(name = "headcount")
    private Integer headcount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // 회사 리뷰 목록
    @Builder.Default
    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY)
    private List<CompanyReviewsEntity> reviews = new ArrayList<>();
}
