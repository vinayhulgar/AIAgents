# Bedrock Model Router (Java)

Routes each query to **Amazon Nova Lite** (simple) or **Claude Opus** (complex).

    export AWS_REGION=ap-south-1
    export SIMPLE_MODEL_ID=apac.amazon.nova-lite-v1:0
    export COMPLEX_MODEL_ID=<your Claude Opus model / inference profile ID>
    mvn -q compile exec:java -Dexec.mainClass=com.example.router.Main

Flow: heuristics (free) -> Nova Lite classifier (cheap) -> fail-safe to Opus.
Requires Bedrock model access enabled for both models and IAM permission `bedrock:InvokeModel`.
