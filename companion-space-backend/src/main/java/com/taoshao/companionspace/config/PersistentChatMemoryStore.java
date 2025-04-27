package com.taoshao.companionspace.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

import com.taoshao.companionspace.mapper.ChatMemoryMapper;
import com.taoshao.companionspace.model.entity.ChatMemory;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

/**
 * @author taoshao
 * @date 2025/4/11
 */
@Component
@RequiredArgsConstructor
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryMapper chatMemoryMapper;


    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        ChatMemory entity = chatMemoryMapper.selectOne(
                new QueryWrapper<ChatMemory>().eq("memoryId", memoryId)
        );
        if (entity != null) {
            List<ChatMessage> chatMessages = messagesFromJson(entity.getMessages());
            return chatMessages;
        }
        return new ArrayList<>();
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = messagesToJson(messages);
        ChatMemory entity = new ChatMemory();
        entity.setMemoryId((String) memoryId);
        entity.setMessages(json);
        chatMemoryMapper.update(entity, new UpdateWrapper<ChatMemory>().eq("memoryId", memoryId));
    }

    @Override
    public void deleteMessages(Object memoryId) {
        chatMemoryMapper.delete(new QueryWrapper<ChatMemory>().eq("memoryId", memoryId));
    }
}
