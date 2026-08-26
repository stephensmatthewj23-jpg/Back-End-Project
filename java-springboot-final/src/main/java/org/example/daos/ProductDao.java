package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Product;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data access object for products.
 */
@Component
public class ProductDao {
    /**
     * JDBC template used to interact with the products table.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a product DAO using the provided data source.
     *
     * @param dataSource the application data source
     */
    public ProductDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Gets every product ordered by ID.
     *
     * @return a list of products
     */
    public List<Product> getProducts() {
        return jdbcTemplate.query("SELECT * FROM products ORDER BY id;", this::mapToProduct);
    }

    /**
     * Looks up a single product by its primary key.
     *
     * @param id the product ID
     * @return the matching product or null if it does not exist
     */
    public Product getProductById(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM products WHERE id = ?;", this::mapToProduct, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Inserts a new product and returns the persisted row.
     *
     * @param product the product to create
     * @return the created product
     */
    public Product createProduct(Product product) {
        String sql = "INSERT INTO products (name, price) VALUES (?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, product.getName());
            ps.setBigDecimal(2, product.getPrice());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new DaoException("Failed to create product.");
        }
        return getProductById(key.intValue());
    }

    /**
     * Updates an existing product.
     *
     * @param product the product with updated values
     * @return the updated product
     */
    public Product updateProduct(Product product) {
        int rowsAffected = jdbcTemplate.update(
                "UPDATE products SET name = ?, price = ? WHERE id = ?;",
                product.getName(),
                product.getPrice(),
                product.getId()
        );
        if (rowsAffected == 0) {
            throw new DaoException("Zero rows affected, expected at least one.");
        }
        return getProductById(product.getId());
    }

    /**
     * Removes a product by ID.
     *
     * @param id the product ID
     * @return the number of rows affected
     */
    public int deleteProduct(int id) {
        return jdbcTemplate.update("DELETE FROM products WHERE id = ?;", id);
    }

    /**
     * Maps a database row into a Product model.
     *
     * @param resultSet the result set from the query
     * @param rowNumber the current row number
     * @return the mapped Product object
     * @throws SQLException if the row cannot be mapped
     */
    private Product mapToProduct(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Product(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getBigDecimal("price")
        );
    }
}
