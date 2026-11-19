package com.example.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Routes a query to Nova Lite (simple) or Claude Opus (complex).
 *
 * Flow: heuristics -> LLM classifier -> fail-safe to COMPLEX.
 */
public class ModelRouter {

    private static final Logger log = LoggerFactory.getLogger(ModelRouter.class);

    private final RouterConfig config;
    private final BedrockInvoker invoker;
    private final HeuristicClassifier heuristics = new HeuristicClassifier();
    private final LlmClassifier llmClassifier;

    public ModelRouter(RouterConfig config) {
        this.config = config;
        this.invoker = new BedrockInvoker(config.region());
        this.llmClassifier = new LlmClassifier(invoker, config.classifierModelId());
    }

    public RouteDecision route(String query) {
        Optional<HeuristicClassifier.Verdict> fast = heuristics.classify(query);
        if (fast.isPresent()) {
            return decision(fast.get(), "HEURISTIC");
        }
        try {
            return decision(llmClassifier.classify(query), "LLM_CLASSIFIER");
        } catch (Exception e) {
            // Misrouting a hard query to a weak model is worse than overspending, so fail toward quality.
            log.warn("Classifier failed, defaulting to COMPLEX: {}", e.getMessage());
            return decision(new HeuristicClassifier.Verdict(Complexity.COMPLEX, "classifier failure"), "FALLBACK");
        }
    }

    /** Route and answer in one call. */
    public Response handle(String query) {
        RouteDecision d = route(query);
        log.info("route={} model={} source={} reason={}", d.complexity(), d.modelId(), d.source(), d.reason());

        int maxTokens = d.complexity() == Complexity.SIMPLE ? 512 : 4096;
        String answer = invoker.converse(d.modelId(), null, query, maxTokens, 0.3f);
        return new Response(answer, d);
    }

    private RouteDecision decision(HeuristicClassifier.Verdict v, String source) {
        String modelId = v.complexity() == Complexity.SIMPLE ? config.simpleModelId() : config.complexModelId();
        return new RouteDecision(v.complexity(), modelId, v.reason(), source);
    }

    public record Response(String answer, RouteDecision decision) {}
}
