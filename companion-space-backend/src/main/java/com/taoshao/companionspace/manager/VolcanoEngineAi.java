package com.taoshao.companionspace.manager;

import java.util.ArrayList;
import java.util.List;
import com.taoshao.companionspace.common.ErrorCode;
import com.taoshao.companionspace.exception.BusinessException;
import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.springframework.stereotype.Component;

/**
 * @Author taoshao
 * @Date 2024/9/21
 */
@Component
public class VolcanoEngineAi {

    /**
     * 火山引擎 ai
     *
     * @param message
     * @return
     */
    public String volcanoEngine(String message) {
        ArkService service = ArkService.builder()
                .apiKey("f7d86867-0ef8-421d-b567-881d08f5d4d6")
                .build();

        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = ChatMessage.builder().role(ChatMessageRole.USER).content(message).build();
        messages.add(chatMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("ep-20240921120357-b4hf9")
                .messages(messages)
                .build();

        try {
            return (String) service.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }

        // service.shutdownExecutor();

    }
}
