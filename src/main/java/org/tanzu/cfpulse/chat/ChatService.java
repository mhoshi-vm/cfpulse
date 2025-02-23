package org.tanzu.cfpulse.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
public class ChatService {

    private final ChatClient chatClient;

    @Value("classpath:/prompts/pulse-system.st")
    private Resource systemChatPrompt;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, List<ToolCallback> toolCallbacks) {
        this.chatClient = chatClientBuilder.
                defaultTools(toolCallbacks).
                defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory), new SimpleLoggerAdvisor()).
                defaultAdvisors(a -> a.param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10)).
                build();
    }

    public String chat(String chat, String org, String space) {
        return chatClient
                .prompt(chat)
                .toolContext(Map.of("org", org, "space", space))
                .call()
                .content();
    }
}
