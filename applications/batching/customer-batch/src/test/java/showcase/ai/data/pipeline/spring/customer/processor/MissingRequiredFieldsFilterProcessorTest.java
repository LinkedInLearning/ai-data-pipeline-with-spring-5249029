package showcase.ai.data.pipeline.spring.customer.processor;

import nyla.solutions.core.io.csv.CsvWriter;
import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import showcase.ai.data.pipeline.spring.customer.domain.Contact;
import showcase.ai.data.pipeline.spring.customer.domain.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MissingRequiredFieldsFilterProcessorTest {

    private MissingRequiredFieldsFilterProcessor subject;
    private final Contact contact = JavaBeanGeneratorCreator.of(Contact.class).create();
    @Mock
    private CsvWriter csvWriter;
    private final static String id = "id";
    private final static String firstName = "fn";
    private final static String lastName = "ln";
    private final static String email = "email";
    private final static String phone = "phone";

    @BeforeEach
    void setUp() {
        subject = new MissingRequiredFieldsFilterProcessor(csvWriter);
    }

    @Test
    void savedValidCustomer() throws Exception {
        var customer = Customer.builder().id(id)
                .firstName(firstName).lastName(lastName)
                .contact(Contact.builder().email(email).phone(phone).build()).build();

        var actual = subject.process(customer);
        verify(csvWriter,never()).appendRow(any(String[].class));

        assertThat(actual).isEqualTo(customer);
    }

    @Test
    void firstNameRequired() throws Exception {

        var customer = Customer.builder().id(id)
                .lastName(lastName)
                .contact(Contact.builder().email(email).phone(phone).build()).build();

        var actual = subject.process(customer);
        verify(csvWriter).appendRow(any(String[].class));

        assertThat(actual).isNull();
    }

    @Test
    void lastNameRequired() throws Exception {

        var customer = Customer.builder().id(id)
                .firstName(firstName)
                .contact(Contact.builder().email(email).phone(phone).build()).build();

        var actual = subject.process(customer);
        verify(csvWriter).appendRow(any(String[].class));

        assertThat(actual).isNull();
    }




    @Test
    void idRequired() throws Exception {

        var customer = Customer.builder()
                .firstName(firstName).lastName(lastName)
                .contact(Contact.builder().email(email).phone(phone).build()).build();

        var actual = subject.process(customer);
        verify(csvWriter).appendRow(any(String[].class));

        assertThat(actual).isNull();
    }

    @Test
    void emailRequired() throws Exception {

        var customer = Customer.builder()
                .firstName(firstName).lastName(lastName)
                .contact(Contact.builder()
                        .phone(phone).build()).build();

        var actual = subject.process(customer);
        verify(csvWriter).appendRow(any(String[].class));

        assertThat(actual).isNull();
    }

    @Test
    void phoneRequired() throws Exception {

        var customer = Customer.builder().id(id)
                .firstName(firstName).lastName(lastName)
                .contact(Contact.builder().email(email)
                        .build()).build();

        var actual = subject.process(customer);
        verify(csvWriter).appendRow(any(String[].class));

        assertThat(actual).isNull();
    }
}