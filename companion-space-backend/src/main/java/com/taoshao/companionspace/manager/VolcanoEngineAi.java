package com.taoshao.companionspace.manager;

import java.util.ArrayList;
import java.util.List;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.exception.BusinessException;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 火山引擎 AI
 * @Author taoshao
 * @Date 2024/9/21
 */
@Component
public class VolcanoEngineAi {

    @Value("${ai.apiKey}")
    private String apiKey;
    @Value("${ai.model}")
    private String model;

    /**
     * 火山引擎 ai
     *
     * @param message
     * @return
     */
    public String volcanoEngine(String message) {
        ArkService service = ArkService.builder()
                .apiKey(apiKey)
                .build();

        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(message).build();
        messages.add(chatMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .build();

        try {
            return (String) service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }


    }
}
