package io.microprofile.tutorial.store.payment.client;

import io.microprofile.tutorial.store.payment.dto.product.Product;
import jakarta.json.JsonArray;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ProductClientJson {
   public static Product[] getProductsWithJsonb(String targetUrl) {
       // This method would typically make a REST call to fetch products.
       // For now, we return an empty array as a placeholder.
       Client client = ClientBuilder.newClient();
       Response response = client.target(targetUrl)
               .request(MediaType.APPLICATION_JSON)
               .get();
      
       Product[] products = response.readEntity(Product[].class);
       response.close();
       client.close();


       return products;
   }


   public static Product[] getProductsWithJsonp(String targetUrl) {
       // Default URL for product service
       String defaultUrl = "http://localhost:5050/products";
       Client client = ClientBuilder.newClient();
       Response response = client.target(targetUrl != null ? targetUrl : defaultUrl)
               .request(MediaType.APPLICATION_JSON)
               .get();

       JsonArray jsonArray = response.readEntity(JsonArray.class);
       response.close();
       client.close();
  
       return collectProducts(jsonArray);
   }
  
   private static Product[] collectProducts(JsonArray jsonArray) {
       Product[] products = new Product[jsonArray.size()];
       for (int i = 0; i < jsonArray.size(); i++) {
           Product product = new Product();
           product.setId(jsonArray.getJsonObject(i).getJsonNumber("id").longValue());
           product.setName(jsonArray.getJsonObject(i).getString("name"));
           product.setPrice(jsonArray.getJsonObject(i).getJsonNumber("price").doubleValue());
           products[i] = product;
       }
       return products;
   }
}

