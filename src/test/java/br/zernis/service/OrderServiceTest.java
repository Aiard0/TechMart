package br.zernis.service;

import br.zernis.dto.orderitem.OrderItemRequestDTO;
import br.zernis.entity.*;
import br.zernis.exception.InvalidOperationException;
import br.zernis.exception.NotFoundException;
import br.zernis.repository.OrderRepository;
import br.zernis.repository.ProductRepository;
import br.zernis.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    OrderService orderService;

    @Captor
    ArgumentCaptor<Order> orderCaptor;

    private User createUser(UUID id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setEmail("user@email.com");
        u.setRole(role);
        return u;
    }

    private Product createProduct(UUID id, String name, int quantity, BigDecimal price) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setQuantity(quantity);
        p.setPrice(price);
        p.setSold(false);
        return p;
    }

    @Test
    void listByBuyer_withExistingBuyer_returnsOrders() {
        UUID buyerId = UUID.randomUUID();
        User buyer = createUser(buyerId, UserRole.USER);
        Order order = new Order();
        order.setBuyer(buyer);

        when(userRepository.findByIdOptional(buyerId)).thenReturn(Optional.of(buyer));
        when(orderRepository.list("buyer", buyer)).thenReturn(List.of(order));

        List<Order> result = orderService.listByBuyer(buyerId);

        assertEquals(1, result.size());
        assertEquals(buyer, result.getFirst().getBuyer());
    }

    @Test
    void listByBuyer_withNonExistingBuyer_throwsNotFoundException() {
        UUID buyerId = UUID.randomUUID();
        when(userRepository.findByIdOptional(buyerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.listByBuyer(buyerId));
    }

    @Test
    void buy_withSellerRole_createsOrder() {
        UUID buyerId = UUID.randomUUID();
        User seller = createUser(buyerId, UserRole.SELLER);

        UUID productId = UUID.randomUUID();
        Product product = createProduct(productId, "Notebook", 10, BigDecimal.valueOf(4500));

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO(productId, 2);

        when(userRepository.findByIdOptional(buyerId)).thenReturn(Optional.of(seller));
        when(productRepository.findByIdOptional(productId)).thenReturn(Optional.of(product));

        Order result = orderService.buy(buyerId, List.of(itemDTO));

        assertEquals(seller, result.getBuyer());
        assertEquals(BigDecimal.valueOf(9000), result.getTotalPrice());
        assertEquals(1, result.getItems().size());

        OrderItem item = result.getItems().getFirst();
        assertEquals(product, item.getProduct());
        assertEquals(2, item.getQuantity());
        assertEquals(BigDecimal.valueOf(4500), item.getUnitPrice());

        assertEquals(8, product.getQuantity());
        assertTrue(product.isSold());

        verify(orderRepository).persist(orderCaptor.capture());
        assertSame(result, orderCaptor.getValue());
    }

    @Test
    void buy_withMultipleItems_calculatesTotalCorrectly() {
        UUID buyerId = UUID.randomUUID();
        User seller = createUser(buyerId, UserRole.SELLER);

        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();
        Product p1 = createProduct(p1Id, "Notebook", 10, BigDecimal.valueOf(4500));
        Product p2 = createProduct(p2Id, "Mouse", 50, BigDecimal.valueOf(150));

        OrderItemRequestDTO item1 = new OrderItemRequestDTO(p1Id, 2);
        OrderItemRequestDTO item2 = new OrderItemRequestDTO(p2Id, 3);

        when(userRepository.findByIdOptional(buyerId)).thenReturn(Optional.of(seller));
        when(productRepository.findByIdOptional(p1Id)).thenReturn(Optional.of(p1));
        when(productRepository.findByIdOptional(p2Id)).thenReturn(Optional.of(p2));

        Order result = orderService.buy(buyerId, List.of(item1, item2));

        assertEquals(BigDecimal.valueOf(4500 * 2 + 150 * 3), result.getTotalPrice());
        assertEquals(2, result.getItems().size());
    }

    @Test
    void buy_withNonExistingUser_throwsNotFoundException() {
        UUID buyerId = UUID.randomUUID();
        when(userRepository.findByIdOptional(buyerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.buy(buyerId, List.of()));
    }

    @Test
    void buy_withUserRole_throwsInvalidOperationException() {
        UUID buyerId = UUID.randomUUID();
        User user = createUser(buyerId, UserRole.USER);

        when(userRepository.findByIdOptional(buyerId)).thenReturn(Optional.of(user));

        assertThrows(InvalidOperationException.class,
                () -> orderService.buy(buyerId, List.of()));
    }

    @Test
    void buy_withAdminRole_throwsInvalidOperationException() {
        UUID buyerId = UUID.randomUUID();
        User admin = createUser(buyerId, UserRole.ADMIN);

        when(userRepository.findByIdOptional(buyerId)).thenReturn(Optional.of(admin));

        assertThrows(InvalidOperationException.class,
                () -> orderService.buy(buyerId, List.of()));
    }

    @Test
    void buy_withNonExistingProduct_throwsNotFoundException() {
        UUID buyerId = UUID.randomUUID();
        User seller = createUser(buyerId, UserRole.SELLER);
        UUID productId = UUID.randomUUID();

        when(userRepository.findByIdOptional(buyerId)).thenReturn(Optional.of(seller));
        when(productRepository.findByIdOptional(productId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> orderService.buy(buyerId, List.of(new OrderItemRequestDTO(productId, 1))));
    }

    @Test
    void buy_withInsufficientQuantity_throwsInvalidOperationException() {
        UUID buyerId = UUID.randomUUID();
        User seller = createUser(buyerId, UserRole.SELLER);
        UUID productId = UUID.randomUUID();
        Product product = createProduct(productId, "Notebook", 2, BigDecimal.valueOf(100));

        when(userRepository.findByIdOptional(buyerId)).thenReturn(Optional.of(seller));
        when(productRepository.findByIdOptional(productId)).thenReturn(Optional.of(product));

        assertThrows(InvalidOperationException.class,
                () -> orderService.buy(buyerId, List.of(new OrderItemRequestDTO(productId, 5))));
    }
}
