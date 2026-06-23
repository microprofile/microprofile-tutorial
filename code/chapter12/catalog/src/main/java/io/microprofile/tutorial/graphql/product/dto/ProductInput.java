package io.microprofile.tutorial.graphql.product.dto;

import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Input type for creating or updating products
 */
@Input("ProductInput")
@Description("Input for creating or updating a product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductInput {
    
    @NonNull
    @Description("Product name")
    private String name;
    
    @Description("Product description")
    private String description;
    
    @NonNull
    @Description("Product price in USD")
    private Double price;
    
    @Description("Product category")
    private String category;
    
    @Description("Initial stock quantity")
    private Integer stockQuantity;
}