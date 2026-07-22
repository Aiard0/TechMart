package br.zernis.service;

import br.zernis.dto.product.UpsertProductDTO;
import br.zernis.entity.Product;
import br.zernis.exception.InvalidOperationException;
import br.zernis.exception.NotFoundException;
import br.zernis.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ProductService {

    @Inject
    ProductRepository productRepository;

    public List<Product> getAll() {
        return productRepository.listAll();
    }

    public Product getById(UUID id) {
        return productRepository.findByIdOptional(id).orElseThrow(() -> new NotFoundException("Produto não encontrado com id: " + id));
    }

    @Transactional
    public Product create(UpsertProductDTO dto) {
        Product product = new Product();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setQuantity(dto.quantity());
        productRepository.persist(product);
        return product;
    }

    @Transactional
    public Product update(UUID id, UpsertProductDTO dto) {
        Product product = productRepository.findByIdOptional(id).orElseThrow(() -> new NotFoundException("Produto não encontrado com id: " + id));

        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        product.setQuantity(dto.quantity());

        return product;
    }

    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findByIdOptional(id).orElseThrow(() -> new NotFoundException("Produto não encontrado com id: " + id));

        if (product.isSold()) {
            throw new InvalidOperationException("Produto com id " + id + " já foi vendido e não pode ser excluído");
        }

        productRepository.delete(product);
    }

}