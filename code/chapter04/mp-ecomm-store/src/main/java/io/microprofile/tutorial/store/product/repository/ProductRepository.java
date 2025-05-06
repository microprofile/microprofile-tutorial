package io.microprofile.tutorial.store.product.repository;

import java.util.List;

import io.microprofile.tutorial.store.product.entity.Product;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@RequestScoped
public class ProductRepository {

    @PersistenceContext(unitName = "product-unit")
    private EntityManager em;

    public void createProduct(Product product) {
        em.persist(product);
    }

    public Product updateProduct(Product product) {
        return em.merge(product);
    }

    public void deleteProduct(Product product) {
        em.remove(product);
    }

    public List<Product> findAllProducts() {
        return em.createNamedQuery("Product.findAllProducts", 
        Product.class).getResultList();
    }

    public Product findProductById(Long id) {
        return em.find(Product.class, id);
    }

    public List<Product> findProduct(String name, String description, Double price) {
        return em.createNamedQuery("Event.findProduct", Product.class)
            .setParameter("name", name)
            .setParameter("description", description)
            .setParameter("price", price).getResultList();
    }

}
