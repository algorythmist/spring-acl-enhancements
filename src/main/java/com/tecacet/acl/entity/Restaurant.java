package com.tecacet.acl.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Restaurant {

    @Id @GeneratedValue
    private long id;
    private String name;
    private String address;

    private Restaurant() {
        //for Hibernate
    }

    public Restaurant(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
