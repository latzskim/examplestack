package com.simpleshop.catalog.infrastructure.adapter.in.web;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import com.simpleshop.catalog.application.command.CreateCategoryCommand;
import com.simpleshop.catalog.application.port.in.CreateCategoryUseCase;
import com.simpleshop.catalog.application.port.in.ListCategoriesUseCase;
import com.simpleshop.catalog.application.query.ListCategoriesQuery;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;

@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {
    
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    
    public AdminCategoryController(ListCategoriesUseCase listCategoriesUseCase, 
                                   CreateCategoryUseCase createCategoryUseCase) {
        this.listCategoriesUseCase = listCategoriesUseCase;
        this.createCategoryUseCase = createCategoryUseCase;
    }
    
    @GetMapping
    @WithSpan
    public String listCategories(Model model) {
        model.addAttribute("categories", listCategoriesUseCase.list(ListCategoriesQuery.root()));
        return "admin/categories/list";
    }
    
    @GetMapping("/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("parentCategories", listCategoriesUseCase.list(ListCategoriesQuery.root()));
        return "admin/categories/form";
    }
    
    @PostMapping
    @WithSpan
    public String createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) UUID parentId,
            @RequestParam(defaultValue = "0") Integer sortOrder,
            RedirectAttributes redirectAttributes) {
        
        CreateCategoryCommand command = new CreateCategoryCommand(name, description, parentId, sortOrder);
        createCategoryUseCase.create(command);
        redirectAttributes.addFlashAttribute("success", "Category created successfully");
        return "redirect:/admin/categories";
    }
}
