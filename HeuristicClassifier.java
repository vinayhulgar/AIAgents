package com.example.router;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Tier 1: zero-cost, zero-latency rules. Only returns a result when it is confident;
 * ambiguous queries return empty and fall through to the LLM classifier.
 */
public class HeuristicClassifier {

    private static final int LONG_QUERY_CHARS = 1500;
    private static final int SHORT_QUERY_CHARS = 200;

    private static final List<String> COMPLEX_HINTS = List.of(
            "architecture", "design a system", "system design", "trade-off", "tradeoff",
            "refactor", "debug", "root cause", "optimi", "step by step", "prove",
            "analy", "compare and contrast", "migration", "concurrency", "distributed",
            "algorithm", "implement", "write a program", "multi-step", "evaluate",
            "strategy", "review this code", "security", "scalab");

    private static final List<String> SIMPLE_HINTS = List.of(
            "what is", "who is", "define", "translate", "capital of", "spell",
            "convert", "fix grammar", "rephrase", "synonym", "tl;dr", "say hi",
            "format this", "summarize in one line", "yes or no");

    private static final Pattern CODE_BLOCK = Pattern.compile("```|\\bclass\\s+\\w+|\\bpublic\\s+static\\b|\\bdef\\s+\\w+\\(");

    public Optional<Verdict> classify(String query) {
        String q = query.toLowerCase();

        if (query.length() > LONG_QUERY_CHARS) {
            return Optional.of(new Verdict(Complexity.COMPLEX, "long input (" + query.length() + " chars)"));
        }
        if (CODE_BLOCK.matcher(query).find()) {
            return Optional.of(new Verdict(Complexity.COMPLEX, "contains code"));
        }
        for (String hint : COMPLEX_HINTS) {
            if (q.contains(hint)) {
                return Optional.of(new Verdict(Complexity.COMPLEX, "complexity keyword: '" + hint + "'"));
            }
        }
        if (query.length() < SHORT_QUERY_CHARS) {
            for (String hint : SIMPLE_HINTS) {
                if (q.contains(hint)) {
                    return Optional.of(new Verdict(Complexity.SIMPLE, "short + simple pattern: '" + hint + "'"));
                }
            }
        }
        return Optional.empty(); // ambiguous
    }

    public record Verdict(Complexity complexity, String reason) {}
}
