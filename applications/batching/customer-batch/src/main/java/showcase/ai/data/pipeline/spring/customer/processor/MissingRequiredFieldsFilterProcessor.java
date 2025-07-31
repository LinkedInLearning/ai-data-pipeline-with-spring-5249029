package showcase.ai.data.pipeline.spring.customer.processor;

import lombok.RequiredArgsConstructor;
import nyla.solutions.core.io.csv.CsvWriter;
import nyla.solutions.core.util.Text;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import showcase.ai.data.pipeline.spring.customer.domain.Customer;

/**
 * Filter customer records with missing required fields
 * @author  Gregory Green
 */
@RequiredArgsConstructor
@Component
public class MissingRequiredFieldsFilterProcessor implements ItemProcessor<Customer,Customer> {

    private final CsvWriter csvWriter;

    /**
     *
     * @param customer to be processed, never {@code null}.
     * @return Null if customer does not have all required fields, else return customer
     * @throws Exception
     */
    @Override
    public Customer process(Customer customer) throws Exception {
        if(customer == null )
            return null;

        if(
                customer.id() == null || customer.id().isBlank() ||
                customer.firstName() == null || customer.firstName().isBlank() ||
                customer.lastName() == null || customer.lastName().isBlank() ||
                customer.contact() == null ||
                        customer.contact().phone() == null || customer.contact().phone().isBlank() ||
                        customer.contact().email() == null || customer.contact().email().isBlank())
        {
            csvWriter.appendRow(
                    customer.id(),
                    customer.firstName(),
                    customer.lastName(),
                    customer.contact() != null ? customer.contact().phone() : "",
                    customer.contact() != null ? customer.contact().email() : "",
                    customer.contact() != null ? customer.contact().phone() : "",
                    customer.location() != null ? customer.location().address() : "",
                    customer.location() != null ? customer.location().city() : "",
                    customer.location() != null ? customer.location().state() : "",
                    customer.location() != null ? customer.location().zip() : "");
            return null;
        }

        return customer;
    }
}
