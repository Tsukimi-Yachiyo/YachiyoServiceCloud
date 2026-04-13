package com.yachiyo.UserService.config;

import com.github.kwfilter.util.KeyWordFilter;
import com.yachiyo.UserService.entity.UserDetail;
import com.yachiyo.UserService.repository.UserDetailRepository;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

@Configuration
public class FastMethodConfig {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public boolean getBirthday(Long userId) {
        // 从redis 中获取当前日期
        String dayT = Objects.requireNonNull(redisTemplate.opsForHash().get("public:date", "day")).toString();
        String monthT = Objects.requireNonNull(redisTemplate.opsForHash().get("public:date", "month")).toString();
        if (!Objects.isNull(dayT) && !Objects.isNull(monthT)) {
            if (userId != null) {
                UserDetail userDetail = userDetailRepository.findById(userId).block();
                if (userDetail != null) {
                    Date birthday = userDetail.getUserBirthday();
                    if (Objects.nonNull(birthday)) {
                        // 提取日期中的日和月
                        LocalDate localDate = birthday.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        int day = localDate.getDayOfMonth();
                        int month = localDate.getMonthValue();
                        // 比较日期是否相等
                        return day == Integer.parseInt(dayT) && month == Integer.parseInt(monthT);
                    }
                }
            }
        }
        else  {
            return false;
        }
        return false;
    }

    @Bean
    public KeyWordFilter keyWordFilter() {
        return KeyWordFilter.getInstance();
    }

    @Bean
    public SpringFormEncoder feignFormEncoder() {
        return new SpringFormEncoder();
    }
}
