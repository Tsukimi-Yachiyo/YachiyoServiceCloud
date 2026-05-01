package com.yachiyo.QQBotService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yachiyo.QQBotService.dto.game.FortuneResult;
import com.yachiyo.QQBotService.entity.Fortune;
import com.yachiyo.QQBotService.mapper.FortuneMapper;
import com.yachiyo.QQBotService.result.Result;
import com.yachiyo.QQBotService.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Random;

@Service
public class GameServiceImpl implements GameService {
    public static final String[] FORTUNES = {
            "大吉", "中吉", "小吉", "末吉", "凶", "大凶"
    };

    @Autowired
    private FortuneMapper fortuneMapper;

    @Override
    public Result<FortuneResult> fortune(Long userId) {
        try {
            // 确保同一个用户每天的抽签结果一致
            LocalDate today = LocalDate.now();

            QueryWrapper<Fortune> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("qq", userId)
                    .eq("date", today); // 确保只查询今天的抽签记录

            Fortune fortune = fortuneMapper.selectOne(queryWrapper);
            if (fortune == null) {
                // 如果今天没有抽签记录，则创建一个新的抽签记录
                Random random = new Random(userId + today.toEpochDay());
                int result = random.nextInt(FORTUNES.length);

                fortune = new Fortune();
                fortune.setQq(userId);
                fortune.setResult(result);
                fortune.setDate(today);
                fortuneMapper.insertOrUpdate(fortune);
                return Result.success(new FortuneResult(false, FORTUNES[result]));
            } else {
                // 如果今天已经有抽签记录，则直接返回结果
                return Result.success(new FortuneResult(true, FORTUNES[fortune.getResult()]));
            }
        } catch (Exception e) {
            return Result.error("抽签失败: ", e.getMessage());
        }
    }
}
