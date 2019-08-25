package com.tecacet.acl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.tecacet.acl.entity.Customer;
import com.tecacet.acl.entity.Restaurant;
import com.tecacet.acl.framework.AclDataAccessService;
import com.tecacet.acl.repository.CustomerRepository;
import com.tecacet.acl.repository.RestaurantRepository;

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

    @Autowired
    private AclDataAccessService dataAccessService;

    /**
     * This test verifies that an entity not annotated by @AclSecured, does not try to insert eny entitlements
     */
    @Test
    public void testA_createRestaurant() {
        Restaurant restaurant = new Restaurant("Arrabiato", "123 Naples 3");
        restaurantRepository.save(restaurant);
    }

    /**
     * Insert some customers with the manager role
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testB_createCustomersAsAdmin() {
        Customer customer1 = new Customer("Danny", "Kaye", "56719");
        customerRepository.save(customer1);
        Customer customer2 = new Customer("Harry", "Belafonte", "12349");
        customerRepository.save(customer2);

        //verify there is an acl record
        Acl acl = aclService.readAclById(new ObjectIdentityImpl(Customer.class, customer1.getId()));
        Sid sid = acl.getOwner();
        assertEquals("manager", ((PrincipalSid)sid).getPrincipal());
        ObjectIdentity objectIdentity = acl.getObjectIdentity();
        assertEquals("CUSTOMER", objectIdentity.getType());
        assertEquals(customer1.getId(), objectIdentity.getIdentifier());

        List<Customer> customers = customerRepository.findAll();
        assertEquals(2, customers.size());
    }

    /**
     * Query customers as manager
     */
    @Test
    @WithMockUser(username = "manager")
    public void testC_queryCustomersAsManager() {
        List<Customer> customers = customerRepository.findAll();
        assertEquals(2, customers.size());
    }

    /**
     * Query customers as assistant - Can't see anything
     */
    @Test
    @WithMockUser(username = "assistant")
    public void testD_queryCustomersAsAssistant() {
        List<Customer> customers = customerRepository.findAll();
        assertTrue(customers.isEmpty());
    }

    /**
     * The manager cannot save anything
     */
    @Test(expected = AccessDeniedException.class)
    @WithMockUser(username = "manager")
    public void testE_managerHasNoPermissionToCreate() {
        Customer customer = new Customer("Danny", "DeVito", "56719");
        customerRepository.save(customer);
    }

    /**
     * Give the assistant permission to read a specific customer
     */
    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testF_giveAssistantAccess() {
        Customer customer2 = customerRepository.findAll().stream()
                .filter(customer -> customer.getFirstName().equals("Harry"))
                .findAny().get();
        dataAccessService.insertAccessEntry(Customer.class, customer2.getId(), "assistant", BasePermission.READ);
    }

    /**
     * This test is non-transactional, so cleaning up
     */
    @Test
    public void testZ_cleanUp() {
        //aclDao.deleteAllEntitlements();
        restaurantRepository.deleteAll();
        customerRepository.deleteAll();
    }

}
