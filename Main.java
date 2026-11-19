package com.example.router;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        ModelRouter router = new ModelRouter(RouterConfig.fromEnv());

        List<String> queries = args.length > 0 ? List.of(args) : List.of(
                "What is the capital of Australia?",
                "Translate 'good morning' to Kannada",
                "Design a multi-tenant entitlement service handling 50k devices with per-tenant rate limits",
                "Why does my Spring Boot app leak connections under load? Here's the pool config...",
                "Give me some ideas for naming my newsletter"   // ambiguous -> LLM classifier
        );

        for (String q : queries) {
            ModelRouter.Response r = router.handle(q);
            System.out.printf("%n[%s via %s | %s] %s%n> %s%n",
                    r.decision().complexity(), r.decision().source(), r.decision().modelId(),
                    r.decision().reason(), q);
            System.out.println(r.answer());
        }
    }
}
