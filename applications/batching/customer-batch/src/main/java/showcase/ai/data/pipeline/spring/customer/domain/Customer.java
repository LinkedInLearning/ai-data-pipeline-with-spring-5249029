package showcase.ai.data.pipeline.spring.customer.domain;

import lombok.Builder;
import lombok.Data;

@Builder
public record Customer(String id, String firstName, String lastName, Contact contact,Location location) {
}
