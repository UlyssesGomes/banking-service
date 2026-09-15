package com.mybanking.agencies.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Address {

    Address() {

    }

    public Address(Long id, String street, String neighborhood, String complement, Integer number) {
        this.id = id;
        this.street = street;
        this.neighborhood = neighborhood;
        this.complement = complement;
        this.number = number;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String street;
    private String neighborhood;
    private String complement;
    private Integer number;

    public Long getId() {
        return id;
    }

    public String getStreet() {
        return street;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getComplement() {
        return complement;
    }

    public Integer getNumber() {
        return number;
    }
}
