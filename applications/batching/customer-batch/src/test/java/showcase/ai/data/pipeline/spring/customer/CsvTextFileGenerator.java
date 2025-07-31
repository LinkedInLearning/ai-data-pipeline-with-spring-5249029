package showcase.ai.data.pipeline.spring.customer;

import nyla.solutions.core.io.IO;
import nyla.solutions.core.io.csv.CsvWriter;
import nyla.solutions.core.patterns.creational.generator.JavaBeanGeneratorCreator;
import nyla.solutions.core.util.Config;
import showcase.ai.data.pipeline.spring.customer.domain.Customer;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Generate random customer records in CSV
 *
 * @author Gregory Green
 */
public class CsvTextFileGenerator {

    public static void main(String[] args) throws IOException {

        var customerCreator = JavaBeanGeneratorCreator.of(Customer.class);

        var settings = Config.settings();

        var file = Paths.get(settings.getProperty("file","applications/batching/customer-batch/src/test/resources/sources/customers.csv")).toFile();
        IO.delete(file);
        var csvWriter = new CsvWriter(file);

        //id,first_name,last_nm,email,phone,address line,city,state,zip
        var count = settings.getPropertyInteger("count",100);

        var customer = customerCreator.create();
        //missing firstname and lastname and email
        //seconds missing phone and email
        csvWriter.appendRow(
                "",
                "",
                "",
                "",
                "",
                customer.location().address(),
                customer.location().city(),
                customer.location().state(),
                customer.location().zip()
        );


        customer = customerCreator.create();
        //seconds missing phone and email
        csvWriter.appendRow(
                "",
                "",
                "",
                "",
                "",
                customer.location().address(),
                customer.location().city(),
                customer.location().state(),
                customer.location().zip()
        );

        //all records have all required fields
        for (int i = 2; i < count; i++) {
            customer = customerCreator.create();
                csvWriter.appendRow(
                        customer.contact().email(),
                        customer.firstName(),
                        customer.lastName(),
                        customer.contact().email(),
                        customer.contact().phone(),
                        customer.location().address(),
                        customer.location().city(),
                        customer.location().state(),
                        customer.location().zip()
                );
            }
    }
}
