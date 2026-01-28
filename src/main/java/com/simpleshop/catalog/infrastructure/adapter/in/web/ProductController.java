package com.simpleshop.catalog.infrastructure.adapter.in.web;

import com.simpleshop.catalog.application.port.in.GetProductUseCase;
import com.simpleshop.catalog.application.port.in.ListCategoriesUseCase;
import com.simpleshop.catalog.application.port.in.ListProductsUseCase;
import com.simpleshop.catalog.application.query.GetProductQuery;
import com.simpleshop.catalog.application.query.ListCategoriesQuery;
import com.simpleshop.catalog.application.query.ListProductsQuery;
import com.simpleshop.catalog.application.query.ProductListView;
import com.simpleshop.catalog.application.query.ProductView;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {
    
    private final ListProductsUseCase listProductsUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    
    public ProductController(ListProductsUseCase listProductsUseCase, 
                            GetProductUseCase getProductUseCase,
                            ListCategoriesUseCase listCategoriesUseCase) {
        this.listProductsUseCase = listProductsUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listCategoriesUseCase = listCategoriesUseCase;
    }
    
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) UUID categoryId,
            Model model) {
        
        ListProductsQuery query = new ListProductsQuery(categoryId, true, page, size);
        Page<ProductListView> products = listProductsUseCase.list(query);
        
        model.addAttribute("products", products);
        model.addAttribute("categories", listCategoriesUseCase.list(ListCategoriesQuery.root()));
        model.addAttribute("selectedCategoryId", categoryId);
        
        return "products/list";
    }
    
    @GetMapping("/{id}")
    public String getProduct(@PathVariable UUID id, Model model) {
        ProductView product = getProductUseCase.get(new GetProductQuery(id))
            .orElse(null);
        
        if (product == null || !product.active()) {
            return "redirect:/products";
        }
        
        model.addAttribute("product", product);
        return "products/detail";
    }
}
