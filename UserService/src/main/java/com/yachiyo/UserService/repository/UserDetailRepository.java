package com.yachiyo.UserService.repository;

import com.yachiyo.UserService.entity.UserDetail;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserDetailRepository extends ReactiveCrudRepository<UserDetail, Long> {
}
