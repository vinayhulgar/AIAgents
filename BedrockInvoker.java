package com.example.router;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;

/** Thin wrapper over the Bedrock Converse API (same call shape for Nova and Claude). */
public class BedrockInvoker {

    private final BedrockRuntimeClient client;

    public BedrockInvoker(String region) {
        this.client = BedrockRuntimeClient.builder().region(Region.of(region)).build();
    }

    public String converse(String modelId, String systemPrompt, String userPrompt,
                           int maxTokens, float temperature) {

        ConverseRequest.Builder req = ConverseRequest.builder()
                .modelId(modelId)
                .messages(Message.builder()
                        .role(ConversationRole.USER)
                        .content(ContentBlock.fromText(userPrompt))
                        .build())
                .inferenceConfig(InferenceConfiguration.builder()
                        .maxTokens(maxTokens)
                        .temperature(temperature)
                        .build());

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            req.system(SystemContentBlock.builder().text(systemPrompt).build());
        }

        ConverseResponse resp = client.converse(req.build());
        return resp.output().message().content().stream()
                .filter(b -> b.text() != null)
                .map(ContentBlock::text)
                .reduce("", String::concat);
    }
}
