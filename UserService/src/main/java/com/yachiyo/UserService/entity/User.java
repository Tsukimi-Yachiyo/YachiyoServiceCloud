package com.yachiyo.UserService.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data @Table("users")
@AllArgsConstructor @NoArgsConstructor
public class User {
    
    /**
     * 用户ID
     */
    @Id
    @Column("user_id")
    private Long id;
    
    /**
     * 用户名
     */
    @Column("name")
    private String name;

    /**
     * 密码
     */
    @Column("password")
    private String password;

    /**
     * 角色
     */
    @Column("role")
    private String role;

    /**
     * 邮箱
     */
    @Column("email")
    private String email;

    /**
     * 是否在线
     */
    @Column("is_online")
    private Boolean isOnline;

    /**
     * 是否禁用
     */
    @Column("is_locked")
    private Boolean isLocked;
    
    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private LocalDateTime updateTime;
}
