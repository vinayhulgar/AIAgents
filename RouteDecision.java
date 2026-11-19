package com.example.router;

/**
 * Result of routing a query.
 *
 * @param complexity SIMPLE or COMPLEX
 * @param modelId    Bedrock model / inference-profile ID that will serve the query
 * @param reason     short human-readable explanation (useful for logs and offline evaluation)
 * @param source     who decided: HEURISTIC, LLM_CLASSIFIER or FALLBACK
 */
public record RouteDecision(Complexity complexity, String modelId, String reason, String source) {}
