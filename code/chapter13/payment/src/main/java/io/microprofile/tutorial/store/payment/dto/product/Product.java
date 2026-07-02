package io.microprofile.tutorial.store.payment.dto.product;

import lombok.Data;

@Data
public class Product {
    public Long id;
    public String name;
    public Double price;
}