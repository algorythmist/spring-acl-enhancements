package com.tecacet.acl.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import com.tecacet.acl.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @PostFilter("hasPermission(filterObject, 'READ') or hasRole('ROLE_ADMIN')")
    List<Customer> findAll();

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    @Query("from Customer where id = :id")
    Customer findByPrimaryKey(@Param("id") Long id);

    @PreAuthorize("hasPermission(#customer, 'WRITE') or hasRole('ROLE_ADMIN')")
    Customer save(@Param("customer") Customer customer);
}
