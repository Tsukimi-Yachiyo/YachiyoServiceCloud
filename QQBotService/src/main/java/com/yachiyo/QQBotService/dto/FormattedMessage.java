package com.yachiyo.QQBotService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormattedMessage {
    private String promptText;
    private List<Long> atList;
    private List<String> fileNames;
    private List<String> relevantUrls;

    public FormattedMessage append(String content, String relevantUrl) {
        this.promptText += " " + content;
        this.relevantUrls.add(relevantUrl);
        return this;
    }
}
