package io.ib67.ask.ai;

public record ChatMessage(String role, String content) {
    public static ChatMessage ofSystemMessage(String content){
        return new ChatMessage("system",content);
    }
    public static ChatMessage ofUserMessage(String content){
        return new ChatMessage("user",content);
    }
}
