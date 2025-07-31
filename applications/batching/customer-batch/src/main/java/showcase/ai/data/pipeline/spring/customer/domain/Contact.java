package showcase.ai.data.pipeline.spring.customer.domain;

import lombok.Builder;

@Builder
public record Contact(String email, String phone) {
}
