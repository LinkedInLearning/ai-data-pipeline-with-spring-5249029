package showcase.ai.data.pipeline.spring.customer.mapper;

import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;
import showcase.ai.data.pipeline.spring.customer.domain.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerFieldMapperTest {

    private CustomerFieldMapper subject;

    @Mock
    private FieldSet fieldSet;
    private Customer customer = JavaBeanGeneratorCreator.of(Customer.class).create();

    @BeforeEach
    void setUp() {
        subject = new CustomerFieldMapper();
    }

    @Test
    void map() throws BindException {

        when(fieldSet.readString(anyInt()))
                .thenReturn(customer.id())
                .thenReturn(customer.firstName())
                .thenReturn(customer.lastName())
                .thenReturn(customer.contact().email())
                .thenReturn(customer.contact().phone())
                .thenReturn(customer.location().address())
                .thenReturn(customer.location().city())
                .thenReturn(customer.location().state())
                .thenReturn(customer.location().zip());

        var actual = subject.mapFieldSet(fieldSet);

        assertThat(actual).isEqualTo(customer);

    }
}