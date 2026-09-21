package com.harmony.chatbot.ai;

import java.util.List;

public interface AiClient {
    double[] embedQuery(String text);

    List<double[]> embedPassages(List<String> texts);

    String chat(List<AiMessage> messages);
}
