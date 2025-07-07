package com.oauth2.Drug.Prompt.Dto;

import lombok.Data;

import java.util.List;

//GPT 출력 토큰
@Data
public class GPTResponse {
    private List<Choice> choices;

    @Data
    public static class Choice {
        private Message message;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
