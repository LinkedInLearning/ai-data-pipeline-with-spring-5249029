package showcase.ai.data.pipeline.sentiment.domains;

import lombok.Builder;



@Builder
public record FeedbackSentiment(Sentiment sentiment,CustomerFeedback customerFeedback) {
    public enum Sentiment{
        Positive,
        Negative,
        Neutral
    }
}
