package com.taoshao.companionspace.config;


import com.taoshao.companionspace.assistant.XiaozhiAgent;
import com.taoshao.companionspace.tools.SearchTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @author taoshao
 * @date 2025/4/26
 */
@Configuration
public class XiaozhiAgentConfig {

    @Resource
    private PersistentChatMemoryStore persistentChatMemoryStore;

    @Autowired
    @Qualifier("xiaozhiEmbeddingStore")
    private EmbeddingStore embeddingStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    /**
     * 持久化存储
     *
     * @return
     */
    @Bean
    public ChatMemoryProvider chatMemoryProviderXiaozhi() {
        return memoryId ->
                MessageWindowChatMemory.builder()
                        .maxMessages(20)
                        .id(memoryId)
                        .chatMemoryStore(persistentChatMemoryStore)
                        .build();

    }

    /**
     * 配置并创建 XiaozhiAgent 实例。
     *
     * @param chatLanguageModel     用于处理聊天的语言模型
     * @param streamingChatLanguage 用于处理流式聊天的语言模型
     * @param chatMemoryProvider    提供聊天记忆的提供者
     * @return 配置好的 XiaozhiAgent 实例
     */
    @Bean
    public XiaozhiAgent xiaozhiAgent(ChatLanguageModel chatLanguageModel,
                                     StreamingChatLanguageModel streamingChatLanguage,
                                     @Qualifier("chatMemoryProviderXiaozhi")
                                     ChatMemoryProvider chatMemoryProvider,
                                     SearchTools searchTools,
                                     @Qualifier("contentRetrieverXiaozhiPincone")
                                     ContentRetriever contentRetriever) {

        return AiServices.builder(XiaozhiAgent.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamingChatLanguage)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(searchTools)
                .contentRetriever(contentRetriever)
                .build();
    }


    /**
     * 使用内存向量存储
     * @return
     */
//    @Bean
//    ContentRetriever contentRetrieverXiaozhi() {
//        Document document = ClassPathDocumentLoader.loadDocument("rag/frontend-knowledge.txt", new TextDocumentParser());
//        //使用内存向量存储
//        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
//
//        //使用默认的文档分割器
//        EmbeddingStoreIngestor.ingest(document, embeddingStore);
//
//        //从嵌入存储（EmbeddingStore）里检索和查询内容相关的信息
//        return EmbeddingStoreContentRetriever.from(embeddingStore);
//    }

    /**
     * Pincone 云服务的全托管向量数据库
     * @return
     */
    @Bean(name = "contentRetrieverXiaozhiPincone")
    ContentRetriever contentRetrieverXiaozhiPincone() {
        // 创建一个 EmbeddingStoreContentRetriever 对象，用于从嵌入存储中检索内容
        return EmbeddingStoreContentRetriever
                .builder()
                // 设置用于生成嵌入向量的嵌入模型
                .embeddingModel(embeddingModel)
                // 指定要使用的嵌入存储
                .embeddingStore(embeddingStore)
                // 设置最大检索结果数量，这里表示最多返回 1 条匹配结果
                .maxResults(1)
                // 设置最小得分阈值，只有得分大于等于 0.8 的结果才会被返回
                .minScore(0.8)
                // 构建最终的 EmbeddingStoreContentRetriever 实例
                .build();
    }


}
