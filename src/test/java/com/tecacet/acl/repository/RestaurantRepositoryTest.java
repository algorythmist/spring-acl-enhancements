package com.tecacet.acl.repository;

import static org.junit.Assert.*;

import javax.transaction.Transactional;
import java.util.Optional;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.tecacet.acl.entity.Restaurant;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Test
    public void testA_insertRestaurant() {
        Restaurant restaurant = new Restaurant("Arrabiato", "123 Naples 3");
        restaurantRepository.save(restaurant);
    }

    @Test
    public void testB_queryRestaurant() {
        Optional<Restaurant> opt = restaurantRepository.findByName("Arrabiato");
        assertTrue(opt.isPresent());
        assertEquals("123 Naples 3", opt.get().getAddress());
    }
}