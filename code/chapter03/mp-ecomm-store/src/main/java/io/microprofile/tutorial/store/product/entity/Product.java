package io.microprofile.tutorial.store.product.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private Long id;
    private String name;
    private String description;
    private Double price;
}
