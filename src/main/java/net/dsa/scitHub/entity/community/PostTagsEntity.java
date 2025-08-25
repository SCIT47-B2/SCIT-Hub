package net.dsa.scitHub.entity.community;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.*;

@Getter @Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post_tags")
public class PostTagsEntity {

    @EmbeddedId
    @EqualsAndHashCode.Include // 이 항목만 기준으로 equals/hashCode의 비교 수행
    @ToString.Include
    private PostTagsId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_pt_post"))
    private PostsEntity post;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_pt_tag"))
    private TagsEntity tag;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
    @Embeddable
    public static class PostTagsId implements Serializable {
        @Column(name = "post_id", columnDefinition = "int unsigned")
        private Integer postId;

        @Column(name = "tag_id", columnDefinition = "int unsigned")
        private Integer tagId;
    }
}

