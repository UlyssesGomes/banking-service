package com.mybanking.agencies.domain;

import jakarta.persistence.*;

@Entity
public class Agency {

    Agency() {

    }

    public Agency(Long id, String name, String companyName, String cnpj, Address address) {
        this.id = id;
        this.name = name;
        this.companyName = companyName;
        this.cnpj = cnpj;
        this.address = address;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(name = "company_name")
    private String companyName;
    private String cnpj;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCnpj() {
        return cnpj;
    }

    public Address getAddress() {
        return address;
    }
}
