package com.yachiyo.UserService.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data @Accessors(chain = true)
public class UserDetailResponse {

    String userName;

    String userIntroduction;

    String userCity;

    String userGender;

    String userPhone;

    Date userBirthday;
}
