package com.yachiyo.ContentService.controller.internal;

import com.yachiyo.ContentService.dto.AddColumnRequest;
import com.yachiyo.ContentService.result.Result;
import com.yachiyo.ContentService.service.ColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/column")
@RequiredArgsConstructor
@Validated
public class ManageController {

    private final ColumnService columnService;

    @PutMapping
    public Result<Boolean> addColumn(@RequestPart AddColumnRequest addColumnRequest) {
        return columnService.addColumn(addColumnRequest);
    }

    @DeleteMapping
    public Result<Boolean> deleteColumn(@RequestBody Long id) {
        return columnService.deleteColumn(id);
    }
}
