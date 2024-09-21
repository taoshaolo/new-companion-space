package com.taoshao.companionspace.manager;

import com.volcengine.ark.runtime.model.completion.chat.ChatCompletionRequest;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @Author taoshao
 * @Date 2024/9/21
 */
//@SpringBootTest
public class AITest {


    @Test
    void testAi() {
        ArkService service = ArkService.builder().apiKey(your key).build();
        System.out.println("\n----- multiple rounds request -----");
        final List<ChatMessage> messages = Arrays.asList(
                ChatMessage.builder().role(ChatMessageRole.SYSTEM).content("你是豆包，是由字节跳动开发的 AI 人工智能助手").build(),
                ChatMessage.builder().role(ChatMessageRole.USER).content("请用java实现一个二分查找？").build()
        );

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("your model")
                .messages(messages)
                .build();

        service.createChatCompletion(chatCompletionRequest).getChoices().forEach(choice -> System.out.println(choice.getMessage().getContent()));

        // shutdown service
        service.shutdownExecutor();
    }
}

