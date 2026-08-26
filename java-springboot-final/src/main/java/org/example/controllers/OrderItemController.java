package org.example.controllers;

import org.example.daos.OrderItemDao;
import org.example.models.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller for order items.
 */
@RestController
@RequestMapping("/api/order-items")
@PreAuthorize("isAuthenticated()")
public class OrderItemController {
    /**
     * DAO used to read and write order-item records.
     */
    @Autowired
    private OrderItemDao orderItemDao;

    /**
     * Lists order items, optionally filtered by order ID.
     *
     * @param orderId optional order ID used to filter the result set
     * @return the matching order items
     */
    @GetMapping
    public List<OrderItem> getAll(@RequestParam(required = false) Integer orderId) {
        if (orderId != null) {
            return orderItemDao.getOrderItemsByOrderId(orderId);
        }
        return orderItemDao.getOrderItems();
    }

    /**
     * Finds one order item by ID.
     *
     * @param id the order item ID
     * @return the matching order item
     */
    @GetMapping("/{id}")
    public OrderItem get(@PathVariable int id) {
        OrderItem orderItem = orderItemDao.getOrderItemById(id);
        if (orderItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found");
        }
        return orderItem;
    }

    /**
     * Creates a new order item.
     *
     * @param orderItem the request payload for the new order item
     * @return the created order item
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public OrderItem create(@RequestBody OrderItem orderItem) {
        return orderItemDao.createOrderItem(orderItem);
    }

    /**
     * Updates an existing order item.
     *
     * @param id the order item ID to update
     * @param orderItem the updated order item payload
     * @return the updated order item
     */
    @PutMapping("/{id}")
    public OrderItem update(@PathVariable int id, @RequestBody OrderItem orderItem) {
        OrderItem existingOrderItem = orderItemDao.getOrderItemById(id);
        if (existingOrderItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found");
        }
        orderItem.setId(id);
        return orderItemDao.updateOrderItem(orderItem);
    }

    /**
     * Removes an order item by ID.
     *
     * @param id the order item ID to delete
     * @return the number of deleted rows
     */
    @DeleteMapping("/{id}")
    public int delete(@PathVariable int id) {
        int affectedRows = orderItemDao.deleteOrderItem(id);
        if (affectedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order item not found");
        }
        return affectedRows;
    }
}
