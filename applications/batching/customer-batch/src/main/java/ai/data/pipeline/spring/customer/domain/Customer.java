package ai.data.pipeline.spring.customer.domain;

import lombok.Builder;

@Builder
public record Customer(String id, String firstName, String lastName, Contact contact,Location location) {
}
