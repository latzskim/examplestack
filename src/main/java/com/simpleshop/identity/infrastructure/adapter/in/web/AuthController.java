package com.simpleshop.identity.infrastructure.adapter.in.web;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import com.simpleshop.identity.application.command.RegisterUserCommand;
import com.simpleshop.identity.application.port.in.RegisterUserUseCase;
import com.simpleshop.identity.domain.exception.EmailAlreadyExistsException;
import com.simpleshop.identity.infrastructure.adapter.in.web.dto.RegisterRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    
    private final RegisterUserUseCase registerUserUseCase;
    
    public AuthController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }
    
    @PostMapping("/register")
    @WithSpan
    public String register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        RegisterRequest request = new RegisterRequest(email, password, firstName, lastName);
        model.addAttribute("registerRequest", request);
        
        if (email == null || email.isBlank()) {
            model.addAttribute("emailError", "Email is required");
            return "auth/register";
        }
        
        if (password == null || password.isBlank()) {
            model.addAttribute("passwordError", "Password is required");
            return "auth/register";
        }
        
        if (password.length() < 8) {
            model.addAttribute("passwordError", "Password must be at least 8 characters");
            return "auth/register";
        }
        
        try {
            RegisterUserCommand command = new RegisterUserCommand(email, password, firstName, lastName);
            registerUserUseCase.register(command);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (EmailAlreadyExistsException e) {
            model.addAttribute("emailError", "Email already registered");
            return "auth/register";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
}
