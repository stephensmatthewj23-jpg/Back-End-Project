package org.example.controllers;

import org.example.daos.OrderDao;
import org.example.models.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

/**
 * Controller for orders.
 */
@RestController
@RequestMapping("/api/orders")
@PreAuthorize("isAuthenticated()")
public class OrderController {
    /**
     * DAO used to read and write order records.
     */
    @Autowired
    private OrderDao orderDao;

    /**
     * Lists orders, optionally filtered by username.
     *
     * @param username optional username used to filter the results
     * @return matching orders
     */
    @GetMapping
    public List<Order> getAll(@RequestParam(required = false) String username) {
        if (username != null) {
            return orderDao.getOrdersByUsername(username);
        }
        return orderDao.getOrders();
    }

    /**
     * Gets a single order by its ID.
     *
     * @param id the order ID
     * @return the matching order
     */
    @GetMapping("/{id}")
    public Order get(@PathVariable int id) {
        Order order = orderDao.getOrderById(id);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return order;
    }

    /**
     * Creates a new order for the authenticated user.
     *
     * @param order the incoming order payload
     * @param principal the currently authenticated user
     * @return the newly created order
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Order create(@RequestBody Order order, Principal principal) {
        String username = principal.getName();
        order.setUsername(username);
        return orderDao.createOrder(order);
    }

    /**
     * Updates an existing order while forcing the username to the current user.
     *
     * @param id the order ID to update
     * @param order the updated order payload
     * @param principal the currently authenticated user
     * @return the updated order
     */
    @PutMapping("/{id}")
    public Order update(@PathVariable int id, @RequestBody Order order, Principal principal) {
        Order existingOrder = orderDao.getOrderById(id);
        if (existingOrder == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        order.setId(id);
        order.setUsername(principal.getName());
        return orderDao.updateOrder(order);
    }

    /**
     * Removes an order by ID.
     *
     * @param id the order ID to delete
     * @return the number of deleted rows
     */
    @DeleteMapping("/{id}")
    public int delete(@PathVariable int id) {
        int affectedRows = orderDao.deleteOrder(id);
        if (affectedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return affectedRows;
    }
}
