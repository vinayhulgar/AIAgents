package com.example.router;

/**
 * Model IDs come from env vars so you can swap models without a rebuild.
 * Verify the exact IDs / inference profiles available to your account in the
 * Bedrock console (Model access + Cross-region inference) for your region.
 */
public record RouterConfig(String region, String simpleModelId, String complexModelId, String classifierModelId) {

    public static RouterConfig fromEnv() {
        String simple = env("SIMPLE_MODEL_ID", "apac.amazon.nova-lite-v1:0");
        return new RouterConfig(
                env("AWS_REGION", "ap-south-1"),
                simple,
                env("COMPLEX_MODEL_ID", "us.anthropic.claude-opus-4-1-20250805-v1:0"),
                env("CLASSIFIER_MODEL_ID", simple)   // classifier uses the cheap model by default
        );
    }

    private static String env(String key, String defaultValue) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? defaultValue : v;
    }
}
