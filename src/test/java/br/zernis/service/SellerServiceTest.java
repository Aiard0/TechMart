package br.zernis.service;

import br.zernis.dto.product.ProductSalesDTO;
import br.zernis.entity.*;
import br.zernis.exception.NotFoundException;
import br.zernis.repository.OrderItemRepository;
import br.zernis.repository.OrderRepository;
import br.zernis.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderItemRepository orderItemRepository;

    @InjectMocks
    SellerService sellerService;

    private Product createProduct(UUID id, String name) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(BigDecimal.TEN);
        return p;
    }

    private OrderItem createOrderItem(Product product, int quantity, BigDecimal unitPrice) {
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        return item;
    }

    @Test
    void listAllOrders_returnsAllOrders() {
        when(orderRepository.listAll()).thenReturn(List.of(new Order(), new Order()));

        List<Order> result = sellerService.listAllOrders();

        assertEquals(2, result.size());
        verify(orderRepository).listAll();
    }

    @Test
    void sellHistory_withSales_returnsAggregatedData() {
        UUID productId = UUID.randomUUID();
        Product product = createProduct(productId, "Notebook");
        OrderItem item1 = createOrderItem(product, 2, BigDecimal.valueOf(4500));
        OrderItem item2 = createOrderItem(product, 3, BigDecimal.valueOf(4500));

        when(productRepository.findByIdOptional(productId)).thenReturn(Optional.of(product));
        when(orderItemRepository.list("product", product)).thenReturn(List.of(item1, item2));

        ProductSalesDTO result = sellerService.sellHistory(productId);

        assertEquals(productId, result.productId());
        assertEquals("Notebook", result.productName());
        assertEquals(5, result.soldQuantity());
        assertEquals(BigDecimal.valueOf(22500), result.totalRevenue());
        assertNull(result.message());
    }

    @Test
    void sellHistory_withNoSales_returnsEmptyMessage() {
        UUID productId = UUID.randomUUID();
        Product product = createProduct(productId, "Mouse");

        when(productRepository.findByIdOptional(productId)).thenReturn(Optional.of(product));
        when(orderItemRepository.list("product", product)).thenReturn(List.of());

        ProductSalesDTO result = sellerService.sellHistory(productId);

        assertEquals(productId, result.productId());
        assertEquals("Mouse", result.productName());
        assertEquals(0, result.soldQuantity());
        assertEquals(BigDecimal.ZERO, result.totalRevenue());
        assertEquals("Este produto ainda não teve nenhuma venda registrada.", result.message());
    }

    @Test
    void sellHistory_withNonExistingProduct_throwsNotFoundException() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdOptional(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sellerService.sellHistory(productId));
    }
}
