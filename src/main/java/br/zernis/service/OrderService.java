package br.zernis.service;

import br.zernis.dto.orderitem.OrderItemRequestDTO;
import br.zernis.entity.*;
import br.zernis.exception.InvalidOperationException;
import br.zernis.exception.NotFoundException;
import br.zernis.repository.OrderRepository;
import br.zernis.repository.ProductRepository;
import br.zernis.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OrderService {

    @Inject
    JsonWebToken jwt;

    @Inject
    OrderRepository orderRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    ProductRepository productRepository;

    @Transactional
    public List<Order> listByBuyer() {
        UUID buyerId = UUID.fromString(jwt.getSubject());
        User buyer = userRepository.findByIdOptional(buyerId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return orderRepository.list("buyer", buyer);
    }

    @Transactional
    public Order buy(List<OrderItemRequestDTO> itemsList) {
        UUID buyerId = UUID.fromString(jwt.getSubject());
        User user = userRepository.findByIdOptional(buyerId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (user.getRole() != UserRole.USER) {
            throw new InvalidOperationException("Apenas clientes podem realizar compras");
        }

        Order order = new Order();
        order.setBuyer(user);
        order.setTotalPrice(BigDecimal.ZERO);

        List<Product> loadedProducts = new ArrayList<>();
        for (OrderItemRequestDTO item : itemsList) {
            Product product = productRepository.findByIdOptional(item.productId()).orElseThrow(() -> new NotFoundException("Produto não encontrado com id: " + item.productId()));

            if (product.getQuantity() < item.quantity()) {
                throw new InvalidOperationException("Quantidade insuficiente do produto: " + product.getName());
            }

            loadedProducts.add(product);
        }

        for (int i = 0; i < itemsList.size(); i++) {
            OrderItemRequestDTO dto = itemsList.get(i);
            Product product = loadedProducts.get(i);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(dto.quantity());
            item.setUnitPrice(product.getPrice());

            order.getItems().add(item);
            order.setTotalPrice((order.getTotalPrice().add(product.getPrice().multiply(BigDecimal.valueOf(dto.quantity())))));

            product.setQuantity(product.getQuantity() - dto.quantity());
            product.markAsSold();
        }

        orderRepository.persist(order);
        return order;
    }

}
