package com.yachiyo.ContentService.controller;

import com.yachiyo.ContentService.dto.ColumnResponse;
import com.yachiyo.ContentService.dto.InteractionRequest;
import com.yachiyo.ContentService.dto.InteractionResponse;
import com.yachiyo.ContentService.dto.SearchRequest;
import com.yachiyo.ContentService.result.Result;
import com.yachiyo.ContentService.service.ColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2/column")
@RequiredArgsConstructor
@Validated
public class ColumnController {

    private final ColumnService columnService;

    @GetMapping("/search")
    public Result<List<ColumnResponse>> searchColumn(@RequestBody SearchRequest searchRequest) {
        return columnService.searchColumn(searchRequest);
    }

    @PutMapping("/interaction")
    public Result<Boolean> interactionColumn(@RequestBody InteractionRequest interactionRequest) {
        return columnService.interactionColumn(interactionRequest);
    }
    
    @GetMapping("/interaction")
    public Result<InteractionResponse> getInteraction(@RequestParam Long columnId) {
        return columnService.getInteraction(columnId);
    }
}
