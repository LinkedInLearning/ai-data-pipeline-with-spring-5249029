package showcase.ai.data.pipeline.sentiment.processor;

import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import showcase.ai.data.pipeline.sentiment.domains.CustomerFeedback;
import showcase.ai.data.pipeline.sentiment.domains.FeedbackSentiment;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerFeedbackSentimentProcessorTest {

    private CustomerFeedbackSentimentProcessor subject;

    @Mock
    private ChatClient chatClient;

    private final CustomerFeedback customerFeedback = JavaBeanGeneratorCreator.of(CustomerFeedback.class).create();
    @Mock
    private ChatClient.ChatClientRequestSpec prompt;
    @Mock
    private ChatClient.ChatClientRequestSpec user;
    @Mock
    private ChatClient.CallResponseSpec callResponse;
    @Mock
    private Advisor advisor;

    @Mock
    private ChatClient.ChatClientRequestSpec advisors;

    @BeforeEach
    void setUp() {
        subject = new CustomerFeedbackSentimentProcessor(chatClient,advisor);
    }

    @Test
    void positiveSentiment() {

        FeedbackSentiment expected = FeedbackSentiment
                .builder().customerFeedback(customerFeedback).sentiment(FeedbackSentiment.Sentiment.Positive).build();

        when(chatClient.prompt()).thenReturn(prompt);
        when(prompt.user(any(Consumer.class))).thenReturn(user);
        when(user.advisors(any(Advisor.class))).thenReturn(advisors);
        when(advisors.call()).thenReturn(callResponse);
        when(callResponse.entity(any(Class.class))).thenReturn(expected.sentiment());

        var actual = subject.apply(customerFeedback);

        assertThat(actual).isEqualTo(expected);
    }
}