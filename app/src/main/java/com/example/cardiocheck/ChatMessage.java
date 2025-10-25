package com.example.cardiocheck;

public class ChatMessage {
    public enum Type { USER, AI }
    private final Type type;
    private final String text;

    public ChatMessage(Type type, String text) {
        this.type = type; this.text = text;
    }
    public Type getType() { return type; }
    public String getText() { return text; }
}

