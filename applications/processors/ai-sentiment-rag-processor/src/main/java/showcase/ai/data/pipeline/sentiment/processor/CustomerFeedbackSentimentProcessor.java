package showcase.ai.data.pipeline.sentiment.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.stereotype.Component;
import showcase.ai.data.pipeline.sentiment.domains.CustomerFeedback;
import showcase.ai.data.pipeline.sentiment.domains.FeedbackSentiment;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Determine the sentiment based on customer feedback
 * @author Gregory Green
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerFeedbackSentimentProcessor implements Function<CustomerFeedback, FeedbackSentiment> {
    private final ChatClient chatClient;

    private final Advisor advisor;

    private final String prompt = """
            Analyze the sentiment of this text: "{text}".
            Respond with only one word: Positive or Negative.
            """;


    /**
     * Determine the sentiment of the feedback
     * @param customerFeedback the function argument
     * @return the sentiment of the feedback summary
     */
    @Override
    public FeedbackSentiment apply(CustomerFeedback customerFeedback) {

        log.info("customerFeedback: {}",customerFeedback);
        var sentiment = chatClient.prompt()
                .user(u -> u.text(prompt)
                        .param("text", customerFeedback.summary()))
                .advisors(advisor)
                .call()
                .entity(FeedbackSentiment.Sentiment.class);

        log.info("sentiment: {}",sentiment);

        return FeedbackSentiment.builder()
                .customerFeedback(customerFeedback)
                .sentiment(sentiment).build();
    }
}
