package br.zernis.service;

import br.zernis.dto.product.ProductSalesDTO;
import br.zernis.entity.Order;
import br.zernis.entity.OrderItem;
import br.zernis.entity.Product;
import br.zernis.exception.NotFoundException;
import br.zernis.repository.OrderItemRepository;
import br.zernis.repository.OrderRepository;
import br.zernis.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SellerService {

    @Inject
    ProductRepository productRepository;

    @Inject
    OrderRepository orderRepository;

    @Inject
    OrderItemRepository orderItemRepository;

    public List<Order> listAllOrders() {
        return orderRepository.listAll();
    }

    public ProductSalesDTO sellHistory(UUID id) {
        Product product = productRepository.findByIdOptional(id).orElseThrow(() -> new NotFoundException("Produto não encontrado com id: " + id));

        List<OrderItem> items = orderItemRepository.list("product", product);

        if (items.isEmpty()) {
            return new ProductSalesDTO(
                    product.getId(),
                    product.getName(),
                    0,
                    BigDecimal.ZERO,
                    "Este produto ainda não teve nenhuma venda registrada."
            );
        }

        int totalQuantitySold = items.stream().mapToInt(OrderItem::getQuantity).sum();
        BigDecimal totalRevenue = items.stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ProductSalesDTO(
                product.getId(),
                product.getName(),
                totalQuantitySold,
                totalRevenue,
                null
        );
    }

}
