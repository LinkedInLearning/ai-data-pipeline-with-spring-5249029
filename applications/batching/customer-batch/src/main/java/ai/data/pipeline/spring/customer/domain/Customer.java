package ai.data.pipeline.spring.customer.domain;

import lombok.Builder;


/**
 *
 * Domain for the customer information
 * @param id the customer id
 * @param firstName the customer first name
 * @param lastName the customer last name
 * @param contact the customer contact
 * @param location the customer location
 *
 * @author Gregory Green
 */
@Builder
public record Customer(String id, String firstName, String lastName, Contact contact,Location location) {
}
