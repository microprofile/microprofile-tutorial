package io.microprofile.tutorial.store.product.repository;

import java.util.List;

import io.microprofile.tutorial.store.product.entity.Product;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@RequestScoped
public class ProductRepository {

    @PersistenceContext(unitName = "product-unit")
    private EntityManager em;

    @Transactional
    public void createProduct(Product product) {
        em.persist(product);
    }

    @Transactional
    public Product updateProduct(Product product) {
        return em.merge(product);
    }

    @Transactional
    public void deleteProduct(Product product) {
        Product mergedProduct = em.merge(product);
        em.remove(mergedProduct);
    }

    @Transactional
    public List<Product> findAllProducts() {
        return em.createNamedQuery("Product.findAllProducts", 
        Product.class).getResultList();
    }

    @Transactional
    public Product findProductById(Long id) {
        // Accessing an entity. JPA automatically uses the cache when possible.
        return em.find(Product.class, id);
    }

    @Transactional
    public List<Product> findProduct(String name, String description, Double price) {
        return em.createNamedQuery("Event.findProduct", Product.class)
            .setParameter("name", name)
            .setParameter("description", description)
            .setParameter("price", price).getResultList();
    }

}
