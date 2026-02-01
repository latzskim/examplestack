package com.simpleshop.cart.infrastructure.adapter.in.web;

import com.simpleshop.cart.application.command.MergeCartCommand;
import com.simpleshop.cart.application.port.in.MergeCartUseCase;
import com.simpleshop.identity.infrastructure.security.ShopUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class CartMergeAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private static final String CART_SESSION_ID = "CART_SESSION_ID";
    
    private final MergeCartUseCase mergeCartUseCase;
    private final AuthenticationSuccessHandler delegate;
    
    public CartMergeAuthenticationSuccessHandler(MergeCartUseCase mergeCartUseCase) {
        this.mergeCartUseCase = mergeCartUseCase;
        SavedRequestAwareAuthenticationSuccessHandler savedRequestHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        savedRequestHandler.setDefaultTargetUrl("/products");
        savedRequestHandler.setAlwaysUseDefaultTargetUrl(false);
        this.delegate = savedRequestHandler;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        
        if (session != null && authentication.getPrincipal() instanceof ShopUserDetails userDetails) {
            String sessionCartId = (String) session.getAttribute(CART_SESSION_ID);
            UUID userId = userDetails.getUserId().getValue();
            
            if (sessionCartId != null) {
                try {
                    MergeCartCommand command = new MergeCartCommand(sessionCartId, userId);
                    mergeCartUseCase.execute(command);
                    session.removeAttribute(CART_SESSION_ID);
                } catch (Exception ignored) {
                }
            }
        }
        
        delegate.onAuthenticationSuccess(request, response, authentication);
    }
}
