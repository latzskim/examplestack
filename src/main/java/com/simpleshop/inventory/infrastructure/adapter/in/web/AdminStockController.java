package com.simpleshop.inventory.infrastructure.adapter.in.web;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import com.simpleshop.catalog.application.port.in.GetProductUseCase;
import com.simpleshop.catalog.application.port.in.ListProductsUseCase;
import com.simpleshop.catalog.application.query.GetProductQuery;
import com.simpleshop.catalog.application.query.ListProductsQuery;
import com.simpleshop.catalog.application.query.ProductListView;
import com.simpleshop.catalog.application.query.ProductView;
import com.simpleshop.inventory.application.command.ReplenishStockCommand;
import com.simpleshop.inventory.application.port.in.CheckStockAvailabilityUseCase;
import com.simpleshop.inventory.application.port.in.ReplenishStockUseCase;
import com.simpleshop.inventory.application.port.out.StockRepository;
import com.simpleshop.inventory.application.port.out.WarehouseRepository;
import com.simpleshop.inventory.application.query.CheckStockAvailabilityQuery;
import com.simpleshop.inventory.application.query.ProductAvailabilityView;
import com.simpleshop.inventory.application.query.StockView;
import com.simpleshop.inventory.application.query.WarehouseView;
import com.simpleshop.inventory.domain.model.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/inventory")
public class AdminStockController {
    
    private final ReplenishStockUseCase replenishStockUseCase;
    private final CheckStockAvailabilityUseCase checkStockAvailabilityUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final GetProductUseCase getProductUseCase;
    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;
    
    public AdminStockController(
            ReplenishStockUseCase replenishStockUseCase,
            CheckStockAvailabilityUseCase checkStockAvailabilityUseCase,
            ListProductsUseCase listProductsUseCase,
            GetProductUseCase getProductUseCase,
            WarehouseRepository warehouseRepository,
            StockRepository stockRepository) {
        this.replenishStockUseCase = replenishStockUseCase;
        this.checkStockAvailabilityUseCase = checkStockAvailabilityUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.getProductUseCase = getProductUseCase;
        this.warehouseRepository = warehouseRepository;
        this.stockRepository = stockRepository;
    }
    
    @GetMapping
    @WithSpan
    public String listInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Page<ProductListView> products = listProductsUseCase.list(ListProductsQuery.all(page, size));

        List<UUID> productIds = products.getContent().stream()
            .map(ProductListView::id)
            .toList();
        Map<UUID, ProductAvailabilityView> availabilityByProduct =
            checkStockAvailabilityUseCase.checkMany(productIds);

        List<ProductStockInfo> productStocks = products.getContent().stream()
            .map(p -> {
                ProductAvailabilityView availability = availabilityByProduct.get(p.id());
                int totalAvailable = availability != null ? availability.totalAvailable() : 0;
                int totalReserved = availability != null ? availability.totalReserved() : 0;
                return new ProductStockInfo(p, totalAvailable, totalReserved);
            })
            .toList();
        
        model.addAttribute("products", products);
        model.addAttribute("productStocks", productStocks);
        return "admin/inventory/list";
    }
    
    @GetMapping("/products/{productId}")
    @WithSpan
    public String manageProductStock(@PathVariable UUID productId, Model model) {
        ProductView product = getProductUseCase.get(new GetProductQuery(productId))
            .orElse(null);
        
        if (product == null) {
            return "redirect:/admin/inventory";
        }
        
        List<WarehouseView> warehouses = warehouseRepository.findAll().stream()
            .filter(Warehouse::isActive)
            .map(w -> new WarehouseView(w.getId(), w.getName().getValue(),
                w.getAddress().getStreet(), w.getAddress().getCity(),
                w.getAddress().getPostalCode(), w.getAddress().getCountry(), w.isActive()))
            .toList();
        
        List<StockView> stocks = stockRepository.findByProductId(productId).stream()
            .map(s -> new StockView(s.getId(), s.getProductId(), s.getWarehouseId(),
                s.getQuantity().getValue(), s.getReservedQuantity().getValue(),
                s.getAvailableQuantity().getValue()))
            .toList();
        
        ProductAvailabilityView totalAvailability = checkStockAvailabilityUseCase.check(
            new CheckStockAvailabilityQuery(productId, null));
        
        model.addAttribute("product", product);
        model.addAttribute("warehouses", warehouses);
        model.addAttribute("stocks", stocks);
        model.addAttribute("totalAvailability", totalAvailability);
        return "admin/inventory/product";
    }
    
    @PostMapping("/products/{productId}/replenish")
    @WithSpan
    public String replenishStock(
            @PathVariable UUID productId,
            @RequestParam UUID warehouseId,
            @RequestParam int quantity,
            RedirectAttributes redirectAttributes) {
        
        ReplenishStockCommand command = new ReplenishStockCommand(productId, warehouseId, quantity);
        replenishStockUseCase.replenish(command);
        redirectAttributes.addFlashAttribute("success", "Stock replenished successfully");
        return "redirect:/admin/inventory/products/" + productId;
    }
    
    public record ProductStockInfo(ProductListView product, int available, int reserved) {}
}
