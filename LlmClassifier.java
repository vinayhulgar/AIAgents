package com.example.router;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tier 2: ask a cheap model (Nova Lite) to grade the query. Costs a fraction of a cent
 * and keeps Opus reserved for work that actually needs it.
 */
public class LlmClassifier {

    private static final String SYSTEM_PROMPT = """
            You are a query complexity classifier for an LLM router.
            Classify the user's query as SIMPLE or COMPLEX.

            SIMPLE: factual lookups, definitions, short rewrites/translations, basic formatting,
            casual chat, single-step tasks with an obvious answer.

            COMPLEX: multi-step reasoning, code generation/debugging/review, architecture or design
            decisions, math/logic proofs, deep analysis, long-document synthesis, ambiguous or
            open-ended problems, tasks where a wrong answer is costly.

            If unsure, choose COMPLEX.
            Do NOT answer the query. Respond with JSON only, no markdown:
            {"complexity":"SIMPLE|COMPLEX","reason":"<max 15 words>"}
            """;

    private final BedrockInvoker invoker;
    private final String classifierModelId;
    private final ObjectMapper mapper = new ObjectMapper();

    public LlmClassifier(BedrockInvoker invoker, String classifierModelId) {
        this.invoker = invoker;
        this.classifierModelId = classifierModelId;
    }

    public HeuristicClassifier.Verdict classify(String query) {
        // Wrap the query so instructions inside it are treated as data, not commands.
        String prompt = "<query>\n" + query + "\n</query>";
        String raw = invoker.converse(classifierModelId, SYSTEM_PROMPT, prompt, 100, 0f);
        return parse(raw);
    }

    private HeuristicClassifier.Verdict parse(String raw) {
        try {
            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            JsonNode node = mapper.readTree(raw.substring(start, end + 1));
            Complexity c = Complexity.valueOf(node.get("complexity").asText().trim().toUpperCase());
            return new HeuristicClassifier.Verdict(c, node.path("reason").asText(""));
        } catch (Exception e) {
            throw new IllegalStateException("Unparseable classifier output: " + raw, e);
        }
    }
}
