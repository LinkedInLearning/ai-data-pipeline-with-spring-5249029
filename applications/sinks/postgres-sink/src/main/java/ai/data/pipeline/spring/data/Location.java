package ai.data.pipeline.spring.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private String address;
    private String city;
    private String state;
    private int zip;
}
