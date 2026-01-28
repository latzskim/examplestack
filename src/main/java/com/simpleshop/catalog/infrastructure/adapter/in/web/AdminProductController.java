package com.simpleshop.catalog.infrastructure.adapter.in.web;

import com.simpleshop.catalog.application.command.CreateProductCommand;
import com.simpleshop.catalog.application.command.UpdateProductCommand;
import com.simpleshop.catalog.application.port.in.*;
import com.simpleshop.catalog.application.query.*;
import com.simpleshop.inventory.application.port.in.CheckStockAvailabilityUseCase;
import com.simpleshop.inventory.application.query.CheckStockAvailabilityQuery;
import com.simpleshop.inventory.application.query.ProductAvailabilityView;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.UUID;

@Controller
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    
    private final ListProductsUseCase listProductsUseCase;
    private final GetProductUseCase getProductUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final DeactivateProductUseCase deactivateProductUseCase;
    private final ActivateProductUseCase activateProductUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final CheckStockAvailabilityUseCase checkStockAvailabilityUseCase;
    
    public AdminProductController(
            ListProductsUseCase listProductsUseCase,
            GetProductUseCase getProductUseCase,
            CreateProductUseCase createProductUseCase,
            UpdateProductUseCase updateProductUseCase,
            DeactivateProductUseCase deactivateProductUseCase,
            ActivateProductUseCase activateProductUseCase,
            ListCategoriesUseCase listCategoriesUseCase,
            CheckStockAvailabilityUseCase checkStockAvailabilityUseCase) {
        this.listProductsUseCase = listProductsUseCase;
        this.getProductUseCase = getProductUseCase;
        this.createProductUseCase = createProductUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.deactivateProductUseCase = deactivateProductUseCase;
        this.activateProductUseCase = activateProductUseCase;
        this.listCategoriesUseCase = listCategoriesUseCase;
        this.checkStockAvailabilityUseCase = checkStockAvailabilityUseCase;
    }
    
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        ListProductsQuery query = ListProductsQuery.all(page, size);
        Page<ProductListView> products = listProductsUseCase.list(query);
        
        java.util.Map<java.util.UUID, Integer> stockMap = new java.util.HashMap<>();
        for (ProductListView product : products.getContent()) {
            ProductAvailabilityView availability = checkStockAvailabilityUseCase.check(
                new CheckStockAvailabilityQuery(product.id(), null));
            stockMap.put(product.id(), availability.totalAvailable());
        }
        
        model.addAttribute("products", products);
        model.addAttribute("stockMap", stockMap);
        return "admin/products/list";
    }
    
    @GetMapping("/new")
    public String newProductForm(Model model) {
        model.addAttribute("categories", listCategoriesUseCase.list(ListCategoriesQuery.root()));
        return "admin/products/form";
    }
    
    @PostMapping
    public String createProduct(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam String sku,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "USD") String currency,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String imageUrl,
            RedirectAttributes redirectAttributes) {
        
        CreateProductCommand command = new CreateProductCommand(
            name, description, sku, price, currency, categoryId, imageUrl
        );
        
        createProductUseCase.create(command);
        redirectAttributes.addFlashAttribute("success", "Product created successfully");
        return "redirect:/admin/products";
    }
    
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable UUID id, Model model) {
        ProductView product = getProductUseCase.get(new GetProductQuery(id))
            .orElse(null);
        
        if (product == null) {
            return "redirect:/admin/products";
        }
        
        model.addAttribute("product", product);
        model.addAttribute("categories", listCategoriesUseCase.list(ListCategoriesQuery.root()));
        return "admin/products/form";
    }
    
    @PostMapping("/{id}")
    public String updateProduct(
            @PathVariable UUID id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "USD") String currency,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String imageUrl,
            RedirectAttributes redirectAttributes) {
        
        UpdateProductCommand command = new UpdateProductCommand(
            id, name, description, price, currency, categoryId, imageUrl
        );
        
        updateProductUseCase.update(command);
        redirectAttributes.addFlashAttribute("success", "Product updated successfully");
        return "redirect:/admin/products";
    }
    
    @PostMapping("/{id}/deactivate")
    public String deactivateProduct(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        deactivateProductUseCase.deactivate(id);
        redirectAttributes.addFlashAttribute("success", "Product deactivated");
        return "redirect:/admin/products";
    }
    
    @PostMapping("/{id}/activate")
    public String activateProduct(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        activateProductUseCase.activate(id);
        redirectAttributes.addFlashAttribute("success", "Product activated");
        return "redirect:/admin/products";
    }
}
