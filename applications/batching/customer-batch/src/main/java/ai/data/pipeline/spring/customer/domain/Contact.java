package ai.data.pipeline.spring.customer.domain;

import lombok.Builder;

/**
 * @author Gregory Green
 * @param email the contact email
 * @param phone the contact phone
 */
@Builder
public record Contact(String email, String phone) {
}
