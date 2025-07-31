package showcase.ai.data.pipeline.spring.customer.domain;

import lombok.Builder;

@Builder
public record Location(String address, String city, String state, String zip) {
}
