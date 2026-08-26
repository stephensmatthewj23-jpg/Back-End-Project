package org.example.daos;

import org.example.exceptions.DaoException;
import org.example.models.OrderItem;
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
 * Data access object for order items.
 */
@Component
public class OrderItemDao {
    /**
     * JDBC template used for order item data access.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates an order item DAO using the application data source.
     *
     * @param dataSource the application data source
     */
    public OrderItemDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Retrieves all order items sorted by ID.
     *
     * @return the full list of order items
     */
    public List<OrderItem> getOrderItems() {
        return jdbcTemplate.query("SELECT * FROM order_items ORDER BY id;", this::mapToOrderItem);
    }

    /**
     * Retrieves all order items for a specific order.
     *
     * @param orderId the order ID to filter by
     * @return matching order items
     */
    public List<OrderItem> getOrderItemsByOrderId(int orderId) {
        return jdbcTemplate.query("SELECT * FROM order_items WHERE order_id = ? ORDER BY id;", this::mapToOrderItem, orderId);
    }

    /**
     * Finds a single order item by its ID.
     *
     * @param id the order item ID
     * @return the matching order item or null if it does not exist
     */
    public OrderItem getOrderItemById(int id) {
        try {
            return jdbcTemplate.queryForObject("SELECT * FROM order_items WHERE id = ?;", this::mapToOrderItem, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Creates a new order item and returns the persisted row.
     *
     * @param orderItem the order item to create
     * @return the created order item
     */
    public OrderItem createOrderItem(OrderItem orderItem) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
            ps.setInt(1, orderItem.getOrderId());
            ps.setInt(2, orderItem.getProductId());
            ps.setInt(3, orderItem.getQuantity());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new DaoException("Failed to create order item.");
        }
        return getOrderItemById(key.intValue());
    }

    /**
     * Updates an existing order item.
     *
     * @param orderItem the updated order item values
     * @return the updated order item
     */
    public OrderItem updateOrderItem(OrderItem orderItem) {
        int rowsAffected = jdbcTemplate.update(
                "UPDATE order_items SET order_id = ?, product_id = ?, quantity = ? WHERE id = ?;",
                orderItem.getOrderId(),
                orderItem.getProductId(),
                orderItem.getQuantity(),
                orderItem.getId()
        );
        if (rowsAffected == 0) {
            throw new DaoException("Zero rows affected, expected at least one.");
        }
        return getOrderItemById(orderItem.getId());
    }

    /**
     * Removes an order item by ID.
     *
     * @param id the order item ID
     * @return the number of rows deleted
     */
    public int deleteOrderItem(int id) {
        return jdbcTemplate.update("DELETE FROM order_items WHERE id = ?;", id);
    }

    /**
     * Maps a database row into an OrderItem model.
     *
     * @param resultSet the result set from the query
     * @param rowNumber the current row number
     * @return the mapped OrderItem object
     * @throws SQLException if the row cannot be mapped
     */
    private OrderItem mapToOrderItem(ResultSet resultSet, int rowNumber) throws SQLException {
        return new OrderItem(
                resultSet.getInt("id"),
                resultSet.getInt("order_id"),
                resultSet.getInt("product_id"),
                resultSet.getInt("quantity")
        );
    }
}
