package com.yachiyo.ColumnService.service;

import com.yachiyo.ColumnService.dto.ColumnResponse;
import com.yachiyo.ColumnService.dto.InteractionRequest;
import com.yachiyo.ColumnService.dto.InteractionResponse;
import com.yachiyo.ColumnService.dto.SearchRequest;
import com.yachiyo.ColumnService.result.Result;

import java.util.List;

public interface ColumnService {

    Result<List<ColumnResponse>> searchColumn(SearchRequest searchRequest);

    Result<InteractionResponse> getInteraction(Long columnId);

    Result<Boolean> interactionColumn(InteractionRequest interactionRequest);
}
