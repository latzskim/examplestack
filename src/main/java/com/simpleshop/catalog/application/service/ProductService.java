package com.simpleshop.catalog.application.service;

import com.simpleshop.catalog.application.command.CreateProductCommand;
import com.simpleshop.catalog.application.command.UpdateProductCommand;
import com.simpleshop.catalog.application.port.in.*;
import com.simpleshop.catalog.application.port.out.CategoryRepository;
import com.simpleshop.catalog.application.port.out.ProductRepository;
import com.simpleshop.catalog.application.query.*;
import com.simpleshop.catalog.domain.exception.DuplicateSkuException;
import com.simpleshop.catalog.domain.exception.ProductNotFoundException;
import com.simpleshop.catalog.domain.model.Category;
import com.simpleshop.catalog.domain.model.Product;
import com.simpleshop.catalog.domain.model.vo.CategoryId;
import com.simpleshop.catalog.domain.model.vo.Money;
import com.simpleshop.catalog.domain.model.vo.ProductId;
import com.simpleshop.catalog.domain.model.vo.Sku;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProductService implements CreateProductUseCase, UpdateProductUseCase, 
        DeactivateProductUseCase, ActivateProductUseCase, GetProductUseCase, ListProductsUseCase {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    
    @Override
    @NewSpan("catalog.createProduct")
    public ProductView create(CreateProductCommand command) {
        Sku sku = Sku.of(command.sku());
        if (productRepository.existsBySku(sku)) {
            throw new DuplicateSkuException(command.sku());
        }
        
        Money price = Money.of(command.price(), command.currency());
        Product product = Product.create(
            command.name(),
            command.description(),
            sku,
            price,
            command.categoryId(),
            command.imageUrl()
        );
        
        product = productRepository.save(product);
        return toProductView(product);
    }
    
    @Override
    @NewSpan("catalog.updateProduct")
    public ProductView update(UpdateProductCommand command) {
        Product product = productRepository.findById(ProductId.of(command.productId()))
            .orElseThrow(() -> new ProductNotFoundException(command.productId()));
        
        Money price = Money.of(command.price(), command.currency());
        product.update(
            command.name(),
            command.description(),
            price,
            command.categoryId(),
            command.imageUrl()
        );
        
        product = productRepository.save(product);
        return toProductView(product);
    }
    
    @Override
    @NewSpan("catalog.deactivateProduct")
    public void deactivate(@SpanTag("productId") UUID productId) {
        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ProductNotFoundException(productId));
        
        product.deactivate();
        productRepository.save(product);
    }
    
    @Override
    @NewSpan("catalog.activateProduct")
    public void activate(@SpanTag("productId") UUID productId) {
        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ProductNotFoundException(productId));
        
        product.activate();
        productRepository.save(product);
    }
    
    @Override
    @Transactional(readOnly = true)
    @NewSpan("catalog.getProduct")
    public Optional<ProductView> get(GetProductQuery query) {
        return productRepository.findById(ProductId.of(query.productId()))
            .map(this::toProductView);
    }
    
    @Override
    @Transactional(readOnly = true)
    @NewSpan("catalog.listProducts")
    public Page<ProductListView> list(ListProductsQuery query) {
        PageRequest pageable = PageRequest.of(query.page(), query.size());
        return productRepository.findAll(query.categoryId(), query.activeOnly(), pageable)
            .map(this::toProductListView);
    }
    
    private ProductView toProductView(Product product) {
        String categoryName = null;
        if (product.getCategoryId() != null) {
            categoryName = categoryRepository.findById(CategoryId.of(product.getCategoryId()))
                .map(Category::getName)
                .orElse(null);
        }
        
        return new ProductView(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getSku().getValue(),
            product.getPrice().getAmount(),
            product.getPrice().getCurrency(),
            product.getCategoryId(),
            categoryName,
            product.getImageUrl(),
            product.isActive(),
            product.getCreatedAt()
        );
    }
    
    private ProductListView toProductListView(Product product) {
        return new ProductListView(
            product.getId(),
            product.getName(),
            product.getSku().getValue(),
            product.getPrice().getAmount(),
            product.getPrice().getCurrency(),
            product.getImageUrl(),
            product.isActive()
        );
    }
}
