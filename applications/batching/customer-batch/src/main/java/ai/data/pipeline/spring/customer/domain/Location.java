package ai.data.pipeline.spring.customer.domain;

import lombok.Builder;

/**
 * Domain data for location information
 * @author Gregory Green
 *
 * @param address the addres line
 * @param city the location city anme
 * @param state the location state
 * @param zip the zip code
 */
@Builder
public record Location(String address, String city, String state, String zip) {
}
