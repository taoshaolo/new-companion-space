package com.taoshao.companionspace.controller;

import com.taoshao.companionspace.assistant.XiaozhiAgent;
import com.taoshao.companionspace.model.request.ChatFromRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

/**
 * @author taoshao
 * @date 2025/4/26
 */
@RestController
@RequestMapping("/xiaozhi")
public class XiaozhiController {

    @Resource
    private XiaozhiAgent xiaozhiAgent;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatFromRequest chatForm) {
        return xiaozhiAgent.chat(chatForm.getMemoryId(), chatForm.getMessage());
    }

    @PostMapping(value = "/stream", produces = "text/stream;charset=utf-8")
    public Flux<String> stream(@RequestBody ChatFromRequest chatForm) {
        return xiaozhiAgent.stream(chatForm.getMemoryId(), chatForm.getMessage());
    }
}
