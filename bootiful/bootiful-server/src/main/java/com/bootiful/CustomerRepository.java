package com.bootiful;

import org.springframework.data.annotation.Id;
import org.springframework.data.repository.CrudRepository;

interface CustomerRepository extends CrudRepository<Customer, Id> {

    Iterable<Customer> findByName(String name);
}
