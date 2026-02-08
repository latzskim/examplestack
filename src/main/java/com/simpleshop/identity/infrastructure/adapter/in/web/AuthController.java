package com.simpleshop.identity.infrastructure.adapter.in.web;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import com.simpleshop.identity.application.command.RegisterUserCommand;
import com.simpleshop.identity.application.port.in.RegisterUserUseCase;
import com.simpleshop.identity.domain.exception.EmailAlreadyExistsException;
import com.simpleshop.identity.infrastructure.adapter.in.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
    public String register(@Valid @ModelAttribute RegisterRequest registerRequest,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            if (bindingResult.hasFieldErrors("email")) {
                model.addAttribute("emailError", bindingResult.getFieldError("email").getDefaultMessage());
            }
            if (bindingResult.hasFieldErrors("password")) {
                model.addAttribute("passwordError", bindingResult.getFieldError("password").getDefaultMessage());
            }
            return "auth/register";
        }
        
        try {
            RegisterUserCommand command = new RegisterUserCommand(
                registerRequest.email(),
                registerRequest.password(),
                registerRequest.firstName(),
                registerRequest.lastName()
            );
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
