package com.taoshao.companionspace.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * @author taoshao
 * @date 2025/4/26
 */
public interface XiaozhiAgent {

    @SystemMessage(fromResource = "prompt/xiaozhi-prompt.txt")
    String chat(@MemoryId String memoryId, @UserMessage String message);

    @SystemMessage(fromResource = "prompt/xiaozhi-prompt.txt")
    Flux<String> stream(@MemoryId String memoryId, @UserMessage String message);
}