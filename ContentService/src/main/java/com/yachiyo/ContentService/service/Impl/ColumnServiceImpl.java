package com.yachiyo.ContentService.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yachiyo.ContentService.client.CoinClient;
import com.yachiyo.ContentService.client.FileClient;
import com.yachiyo.ContentService.dto.*;
import com.yachiyo.ContentService.entity.Column;
import com.yachiyo.ContentService.enumeration.InteractionType;
import com.yachiyo.ContentService.enumeration.TradeType;
import com.yachiyo.ContentService.mapper.ColumnMapper;
import com.yachiyo.ContentService.result.Result;
import com.yachiyo.ContentService.service.ColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ColumnServiceImpl implements ColumnService {

    @Autowired
    private ColumnMapper columnMapper;

    @Autowired
    private FileClient fileClient;

    @Autowired
    private CoinClient coinClient;

    private static final String COLUMN_CACHE_NAME = "cache:column";

    @Override
    @Cacheable(value = COLUMN_CACHE_NAME, key = "#searchRequest.keyword + ':' + #searchRequest.pageNum + ':' + #searchRequest.pageSize")
    public Result<List<ColumnResponse>> searchColumn(SearchRequest searchRequest) {
        Page<Column> page = new Page<>(searchRequest.getPageNum(), searchRequest.getPageSize() );
        LambdaQueryWrapper<Column> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Column::getTitle, searchRequest.getKeyword());
        queryWrapper.orderByDesc(Column::getId);

        List<Column> columnList = columnMapper.selectPage(page, queryWrapper).getRecords();

        List<ColumnResponse> columnResponseList = new ArrayList<>();
        for (Column column : columnList) {
            String essayUrl = fileClient.getUrl(column.getFileName(), 24*60*60, "save");
            columnResponseList.add(new ColumnResponse(column.getId(), column.getTitle(), column.getIntroduction(), column.getType(), column.getWriter(),essayUrl, column.getCreateTime()));
        }
        return Result.success(columnResponseList);
    }

    @Override
    @Cacheable(value = COLUMN_CACHE_NAME, key = "#columnId")
    public Result<InteractionResponse> getInteraction(Long columnId) {

        Column column = columnMapper.selectById(columnId);
        if (column == null) {
            return Result.error("404", "专栏不存在");
        }

        return Result.success(new InteractionResponse(column.getCoin(), column.getLike()));
    }

    @Override
    @CacheEvict(value = COLUMN_CACHE_NAME, key = "#interactionRequest.postingId")
    public Result<Boolean> interactionColumn(InteractionRequest interactionRequest) {
        Long userId = (Long) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        Column column = columnMapper.selectById(interactionRequest.getPostingId());
        if (column == null) {
            return Result.error("404", "专栏不存在");
        }

        if (!coinClient.changeCoin(userId, new CoinChangeRequest(userId, column.getWriter(), TradeType.TIP, 1.0)).getData()) {
            return Result.error("500", "扣除积分失败");
        }

        if (interactionRequest.getType() == InteractionType.COIN) {
            column.setCoin(column.getCoin() + 1);
        } else if (interactionRequest.getType() == InteractionType.LIKE) {
            column.setLike(column.getLike() + 1);
        }
        columnMapper.updateById(column);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> addColumn(AddColumnRequest addColumnRequest) {
        if (!fileClient.save(addColumnRequest.getFile().getName(),addColumnRequest.getFile())) {
            return Result.error("500", "保存文件失败");
        }
        Column column = new Column();
        column.setTitle(addColumnRequest.getName());
        column.setIntroduction(addColumnRequest.getDescription());
        column.setType(addColumnRequest.getType());
        column.setWriter(addColumnRequest.getWriterId());

        return Result.success(columnMapper.insert(column) > 0);
    }

    @Override
    public Result<Boolean> deleteColumn(Long id) {
        return Result.success(columnMapper.deleteById(id) > 0);
    }
}
