package com.taoshao.companionspace.config;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class LangChain4jConfig {

    @Value("${langchain4j.community.dashscope.streaming-chat-model.model-name}")
    private String modelName;
    @Value("${langchain4j.community.dashscope.streaming-chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.community.dashscope.chat-model.model-name}")
    private String modelName2;
    @Value("${langchain4j.community.dashscope.chat-model.api-key}")
    private String apiKey2;

    @Value("${langchain4j.community.dashscope.embedding-model.model-name}")
    private String modelName3;
    @Value("${langchain4j.community.dashscope.embedding-model.api-key}")
    private String apiKey3;

    @Bean
    public QwenStreamingChatModel qwenStreamingChatModel() {
        return QwenStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .build();
    }

    @Bean
    public QwenChatModel qwenChatModel() {
        return QwenChatModel.builder()
                .apiKey(apiKey2)
                .modelName(modelName2)
                .build();
    }

    @Bean
    public QwenEmbeddingModel qwenEmbeddingModel() {
        return QwenEmbeddingModel.builder()
                .apiKey(apiKey3)
                .modelName(modelName3)
                .build();
    }

}
