package com.tecacet.acl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.tecacet.acl.entity.Customer;
import com.tecacet.acl.entity.Restaurant;
import com.tecacet.acl.repository.CustomerRepository;
import com.tecacet.acl.repository.RestaurantRepository;

//TODO: Non transactional - clean up
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AclEnhancementsDemoTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private AclService aclService;

    @Test
    public void testA_createRestaurant() {
        Restaurant restaurant = new Restaurant("Arrabiato", "123 Naples 3");
        restaurantRepository.save(restaurant);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testB_createCustomerAsAdmin() {
        Customer customer = new Customer("Danny", "Kaye", "56719");
        customerRepository.save(customer);
        Acl acl = aclService.readAclById(new ObjectIdentityImpl(Customer.class, customer.getId()));
        Sid sid = acl.getOwner();
        System.out.println(sid);
        ObjectIdentity objectIdentity = acl.getObjectIdentity();
        assertEquals("CUSTOMER", objectIdentity.getType());
        assertEquals(customer.getId(), objectIdentity.getIdentifier());

        List<Customer> customers = customerRepository.findAll();
        assertEquals(1, customers.size());
    }

    @Test
    @WithMockUser(username = "manager")
    public void testC_queryCustomersAsManager() {
        List<Customer> customers = customerRepository.findAll();
        assertEquals(1, customers.size());
    }
}
