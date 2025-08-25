package net.dsa.scitHub.entity.community;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "postTags")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "tags",
    uniqueConstraints = @UniqueConstraint(name = "uk_tags_name", columnNames = "name")
)
public class TagsEntity {

    @Id
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id", columnDefinition = "int unsigned")
    private Integer tagId;

    @Size(max = 50)
    @Column(name = "name", nullable = false, length = 50)
    private String name;


    /*
     * 연관관계 매핑
     * 자주 호출할 것 같은 것만 리스트로 매핑하고, 나머지는 그때그때
     * 쿼리로 불러오는 것이 좋음
     */

    @Builder.Default
    @OneToMany(mappedBy = "tag", fetch = FetchType.LAZY)
    private List<PostTagsEntity> postTags = new ArrayList<>();
}
