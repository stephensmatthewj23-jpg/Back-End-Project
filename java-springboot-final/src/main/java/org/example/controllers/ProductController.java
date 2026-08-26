package org.example.controllers;

import org.example.daos.ProductDao;
import org.example.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller for products.
 */
@RestController
@RequestMapping("/api/products")
@PreAuthorize("isAuthenticated()")
public class ProductController {
    /**
     * DAO used to read and write product records.
     */
    @Autowired
    private ProductDao productDao;

    /**
     * Lists all products.
     *
     * @return all product records in the database
     */
    @GetMapping
    public List<Product> getAll() {
        return productDao.getProducts();
    }

    /**
     * Finds one product by ID.
     *
     * @param id the product ID
     * @return the matching product
     */
    @GetMapping("/{id}")
    public Product get(@PathVariable int id) {
        Product product = productDao.getProductById(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return product;
    }

    /**
     * Creates a new product.
     *
     * @param product the product payload from the request
     * @return the newly created product
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Product create(@RequestBody Product product) {
        return productDao.createProduct(product);
    }

    /**
     * Updates an existing product record.
     *
     * @param id the product ID to update
     * @param product the updated product request body
     * @return the updated product
     */
    @PutMapping("/{id}")
    public Product update(@PathVariable int id, @RequestBody Product product) {
        Product existingProduct = productDao.getProductById(id);
        if (existingProduct == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        product.setId(id);
        return productDao.updateProduct(product);
    }

    /**
     * Removes a product by ID.
     *
     * @param id the product ID to delete
     * @return the number of deleted rows
     */
    @DeleteMapping("/{id}")
    public int delete(@PathVariable int id) {
        int affectedRows = productDao.deleteProduct(id);
        if (affectedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return affectedRows;
    }
}
