package com.tecacet.acl;

import static org.junit.Assert.assertEquals;

import com.github.javafaker.Faker;
import com.tecacet.acl.entity.Customer;
import com.tecacet.acl.framework.AclDataAccessService;
import com.tecacet.acl.repository.CustomerRepository;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.stream.IntStream;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AclPerformanceTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AclDataAccessService dataAccessService;

    private final Faker faker = Faker.instance();

    //Change this to a large number
    private int numberOfCustomers = 100;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testA_populateData() {
        IntStream.range(0, numberOfCustomers).forEach(i -> {
            Customer customer = new Customer(faker.name().name(),
                    faker.funnyName().name(), faker.phoneNumber().phoneNumber());
            customerRepository.save(customer);
        });
    }

    /**
     * Give the assistant permission to read a specific customer
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testB_giveAssistantAccess() {
        List<Customer> customers = customerRepository.findAll();
        logger.info("Retrieved {} customers", customers.size());
        for (int i = 0; i < customers.size(); i++) {
            if (i % 3 == 0) {
                //give permission to every 3rd customer
                Customer customer = customers.get(i);
                dataAccessService.insertAccessEntry(Customer.class, customer.getId(), "assistant", BasePermission.READ);
            }
        }
        logger.info("Gave assistant permissiont to some customers");
    }

    /**
     * Query customers as assistant
     */
    @Test
    @WithMockUser(username = "assistant")
    public void testC_queryCustomersAsAssistant() {
        long start = System.currentTimeMillis();
        List<Customer> customers = customerRepository.findAll();
        long time = System.currentTimeMillis() - start;
        logger.info("Executed customer query in {} milliseconds.", time);
        assertEquals(numberOfCustomers/3 + 1, customers.size());
    }

    /**
     * This test is non-transactional, so cleaning up
     */
    @Test
    public void testZ_cleanUp() {
        customerRepository.deleteAll();
    }

}
