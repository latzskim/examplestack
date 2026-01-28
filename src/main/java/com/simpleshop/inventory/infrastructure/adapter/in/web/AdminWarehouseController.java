package com.simpleshop.inventory.infrastructure.adapter.in.web;

import com.simpleshop.inventory.application.command.CreateWarehouseCommand;
import com.simpleshop.inventory.application.port.in.CreateWarehouseUseCase;
import com.simpleshop.inventory.application.port.in.GetWarehouseUseCase;
import com.simpleshop.inventory.application.port.in.ListWarehouseStockUseCase;
import com.simpleshop.inventory.application.port.out.WarehouseRepository;
import com.simpleshop.inventory.application.query.GetWarehouseQuery;
import com.simpleshop.inventory.application.query.GetWarehouseStockQuery;
import com.simpleshop.inventory.application.query.StockView;
import com.simpleshop.inventory.application.query.WarehouseView;
import com.simpleshop.inventory.domain.model.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin/warehouses")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWarehouseController {
    
    private final CreateWarehouseUseCase createWarehouseUseCase;
    private final GetWarehouseUseCase getWarehouseUseCase;
    private final ListWarehouseStockUseCase listWarehouseStockUseCase;
    private final WarehouseRepository warehouseRepository;
    
    public AdminWarehouseController(
            CreateWarehouseUseCase createWarehouseUseCase,
            GetWarehouseUseCase getWarehouseUseCase,
            ListWarehouseStockUseCase listWarehouseStockUseCase,
            WarehouseRepository warehouseRepository) {
        this.createWarehouseUseCase = createWarehouseUseCase;
        this.getWarehouseUseCase = getWarehouseUseCase;
        this.listWarehouseStockUseCase = listWarehouseStockUseCase;
        this.warehouseRepository = warehouseRepository;
    }
    
    @GetMapping
    public String listWarehouses(Model model) {
        List<WarehouseView> warehouses = warehouseRepository.findAll().stream()
            .map(this::toView)
            .toList();
        model.addAttribute("warehouses", warehouses);
        return "admin/warehouses/list";
    }
    
    @GetMapping("/new")
    public String newWarehouseForm() {
        return "admin/warehouses/form";
    }
    
    @PostMapping
    public String createWarehouse(
            @RequestParam String name,
            @RequestParam String street,
            @RequestParam String city,
            @RequestParam String postalCode,
            @RequestParam String country,
            RedirectAttributes redirectAttributes) {
        
        CreateWarehouseCommand command = new CreateWarehouseCommand(name, street, city, postalCode, country);
        createWarehouseUseCase.create(command);
        redirectAttributes.addFlashAttribute("success", "Warehouse created successfully");
        return "redirect:/admin/warehouses";
    }
    
    @GetMapping("/{id}")
    public String viewWarehouse(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        WarehouseView warehouse = getWarehouseUseCase.get(new GetWarehouseQuery(id))
            .orElse(null);
        
        if (warehouse == null) {
            return "redirect:/admin/warehouses";
        }
        
        Page<StockView> stocks = listWarehouseStockUseCase.list(new GetWarehouseStockQuery(id, page, size));
        
        model.addAttribute("warehouse", warehouse);
        model.addAttribute("stocks", stocks);
        return "admin/warehouses/detail";
    }
    
    private WarehouseView toView(Warehouse w) {
        return new WarehouseView(
            w.getId(), w.getName().getValue(),
            w.getAddress().getStreet(), w.getAddress().getCity(),
            w.getAddress().getPostalCode(), w.getAddress().getCountry(),
            w.isActive()
        );
    }
}
