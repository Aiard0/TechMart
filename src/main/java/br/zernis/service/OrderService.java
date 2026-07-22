package br.zernis.service;

import br.zernis.dto.orderitem.OrderItemRequestDTO;
import br.zernis.entity.*;
import br.zernis.repository.OrderRepository;
import br.zernis.repository.ProductRepository;
import br.zernis.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OrderService {

    @Inject
    OrderRepository orderRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    ProductRepository productRepository;

    @Transactional
    public List<Order> listByBuyer(UUID buyerId) {
        User buyer = userRepository.findByIdOptional(buyerId).orElseThrow(() -> new br.zernis.exception.NotFoundException("Usuário não encontrado com id: " + buyerId));
        return orderRepository.list("buyer", buyer);
    }

    @Transactional
    public Order buy(UUID buyerId, List<OrderItemRequestDTO> itemsList) {
        User user = userRepository.findByIdOptional(buyerId).orElseThrow(() -> new br.zernis.exception.NotFoundException("Usuário não encontrado com id: " + buyerId));

        if (user.getRole() != UserRole.SELLER) {
            throw new br.zernis.exception.InvalidOperationException("Apenas usuários vendedores podem realizar compras");
        }

        Order order = new Order();
        order.setBuyer(user);
        order.setTotalPrice(BigDecimal.ZERO);

        List<Product> loadedProducts = new ArrayList<>();
        for (OrderItemRequestDTO item : itemsList) {
            Product product = productRepository.findByIdOptional(item.productId()).orElseThrow(() -> new br.zernis.exception.NotFoundException("Produto não encontrado com id: " + item.productId()));

            if (product.getQuantity() < item.quantity()) {
                throw new br.zernis.exception.InvalidOperationException("Quantidade insuficiente do produto: " + product.getName());
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
