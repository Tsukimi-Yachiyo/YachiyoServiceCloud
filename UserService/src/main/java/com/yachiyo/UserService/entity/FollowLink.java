package com.yachiyo.UserService.entity;

import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Range;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("follow_link")
public class FollowLink implements Persistable<Long> {

    /**
     * 关注者ID
     */
    @Id
    @Column("follower")// 数据库字段是 follower
    private Long followerId;

    /**
     * 被关注者ID
     */
    @Column("followee")     // 数据库字段是 followee
    private Long followeeId;

    @Override
    public @Nullable Long getId() {
        return followerId;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
