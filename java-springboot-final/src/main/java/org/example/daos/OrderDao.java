package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.Order;
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
 * Data access object for orders.
 */
@Component
public class OrderDao {
    /**
     * JDBC template used for order database access.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates an order DAO using the application data source.
     *
     * @param dataSource the application data source
     */
    public OrderDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Gets all orders, ordered by ID.
     *
     * @return a list of orders
     */
    public List<Order> getOrders() {
        return jdbcTemplate.query("SELECT * FROM orders ORDER BY id;", this::mapToOrder);
    }

    /**
     * Gets all orders for a specific username.
     *
     * @param username the username associated with the orders
     * @return the matching orders
     */
    public List<Order> getOrdersByUsername(String username) {
        return jdbcTemplate.query("SELECT * FROM orders WHERE username = ? ORDER BY id;", this::mapToOrder, username);
    }

    /**
     * Finds a single order using its ID.
     *
     * @param id the order ID
     * @return the matching order or null if it does not exist
     */
    public Order getOrderById(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM orders WHERE id = ?;", this::mapToOrder, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Creates a new order and returns the saved copy.
     *
     * @param order the order to store
     * @return the created order
     */
    public Order createOrder(Order order) {
        String sql = "INSERT INTO orders (username) VALUES (?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setString(1, order.getUsername());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new DaoException("Failed to create order.");
        }
        return getOrderById(key.intValue());
    }

    /**
     * Updates an existing order's username.
     *
     * @param order the updated order data
     * @return the updated order
     */
    public Order updateOrder(Order order) {
        int rowsAffected = jdbcTemplate.update(
                "UPDATE orders SET username = ? WHERE id = ?;",
                order.getUsername(),
                order.getId()
        );
        if (rowsAffected == 0) {
            throw new DaoException("Zero rows affected, expected at least one.");
        }
        return getOrderById(order.getId());
    }

    /**
     * Deletes an order by ID.
     *
     * @param id the order ID
     * @return the number of rows deleted
     */
    public int deleteOrder(int id) {
        return jdbcTemplate.update("DELETE FROM orders WHERE id = ?;", id);
    }

    /**
     * Maps a database row into an Order model.
     *
     * @param resultSet the result set from the query
     * @param rowNumber the current row number
     * @return the mapped Order object
     * @throws SQLException if the row cannot be mapped
     */
    private Order mapToOrder(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Order(
                resultSet.getInt("id"),
                resultSet.getString("username")
        );
    }
}
