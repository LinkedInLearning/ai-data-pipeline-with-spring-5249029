package showcase.ai.data.pipeline.spring.customer.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import showcase.ai.data.pipeline.spring.customer.domain.Contact;
import showcase.ai.data.pipeline.spring.customer.domain.Customer;
import showcase.ai.data.pipeline.spring.customer.domain.Location;

/**
 * Create a customer record from a Field Set
 * @author  Gregory Green
 */
@Component
public class CustomerFieldMapper implements FieldSetMapper<Customer> {
    @Override
    public Customer mapFieldSet(FieldSet fieldSet) throws BindException {


        return Customer.builder()
                .id(fieldSet.readString(0))
                .firstName(fieldSet.readString(1))
                .lastName(fieldSet.readString(2))
                .contact(Contact.builder()
                        .email(fieldSet.readString(3))
                        .phone(fieldSet.readString(4))
                        .build())
                .location(Location.builder()
                        .address(fieldSet.readString(5))
                        .city(fieldSet.readString(6))
                        .state(fieldSet.readString(7 ))
                        .zip(fieldSet.readString(8)).build())
                .build();
    }
}
