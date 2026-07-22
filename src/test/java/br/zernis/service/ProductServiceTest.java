package br.zernis.service;

import br.zernis.dto.product.UpsertProductDTO;
import br.zernis.entity.Product;
import br.zernis.exception.InvalidOperationException;
import br.zernis.exception.NotFoundException;
import br.zernis.repository.ProductRepository;
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
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductService productService;

    @Captor
    ArgumentCaptor<Product> productCaptor;

    private Product createProduct(UUID id, String name, int quantity, boolean sold) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setDescription("desc");
        p.setPrice(BigDecimal.TEN);
        p.setQuantity(quantity);
        p.setSold(sold);
        return p;
    }

    @Test
    void getAll_returnsAllProducts() {
        when(productRepository.listAll()).thenReturn(List.of(new Product(), new Product()));

        List<Product> result = productService.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void getById_withExistingProduct_returnsProduct() {
        UUID id = UUID.randomUUID();
        Product product = createProduct(id, "Notebook", 10, false);
        when(productRepository.findByIdOptional(id)).thenReturn(Optional.of(product));

        Product result = productService.getById(id);

        assertEquals(id, result.getId());
        assertEquals("Notebook", result.getName());
    }

    @Test
    void getById_withNonExistingProduct_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.getById(id));
    }

    @Test
    void create_persistsAndReturnsProduct() {
        UpsertProductDTO dto = new UpsertProductDTO("Notebook", "16GB RAM", BigDecimal.valueOf(4500), 10);

        Product result = productService.create(dto);

        assertEquals("Notebook", result.getName());
        assertEquals("16GB RAM", result.getDescription());
        assertEquals(BigDecimal.valueOf(4500), result.getPrice());
        assertEquals(10, result.getQuantity());

        verify(productRepository).persist(productCaptor.capture());
        Product saved = productCaptor.getValue();
        assertEquals("Notebook", saved.getName());
    }

    @Test
    void update_withExistingProduct_updatesAndReturns() {
        UUID id = UUID.randomUUID();
        Product existing = createProduct(id, "Old Name", 5, false);
        UpsertProductDTO dto = new UpsertProductDTO("New Name", "New Desc", BigDecimal.valueOf(99), 20);

        when(productRepository.findByIdOptional(id)).thenReturn(Optional.of(existing));

        Product result = productService.update(id, dto);

        assertEquals("New Name", result.getName());
        assertEquals("New Desc", result.getDescription());
        assertEquals(BigDecimal.valueOf(99), result.getPrice());
        assertEquals(20, result.getQuantity());
    }

    @Test
    void update_withNonExistingProduct_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> productService.update(id, new UpsertProductDTO("N", "D", BigDecimal.ONE, 1)));
    }

    @Test
    void delete_withUnsoldProduct_deletesSuccessfully() {
        UUID id = UUID.randomUUID();
        Product product = createProduct(id, "Notebook", 10, false);

        when(productRepository.findByIdOptional(id)).thenReturn(Optional.of(product));

        productService.delete(id);

        verify(productRepository).delete(product);
    }

    @Test
    void delete_withSoldProduct_throwsInvalidOperationException() {
        UUID id = UUID.randomUUID();
        Product product = createProduct(id, "Notebook", 10, true);

        when(productRepository.findByIdOptional(id)).thenReturn(Optional.of(product));

        assertThrows(InvalidOperationException.class, () -> productService.delete(id));
        verify(productRepository, never()).delete(any());
    }

    @Test
    void delete_withNonExistingProduct_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(productRepository.findByIdOptional(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.delete(id));
    }
}
