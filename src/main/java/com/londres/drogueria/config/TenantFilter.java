package com.londres.drogueria.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Autowired
    private com.londres.drogueria.repository.EmpresaRepository empresaRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long empresaId = userDetails.getEmpresaId();
            

            if (!request.isUserInRole("ROLE_SUPER_ADMIN") && empresaId != null) {
                com.londres.drogueria.model.Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
                if (empresa != null && !empresa.isActivo()) {
                    SecurityContextHolder.clearContext();
                    request.getSession().invalidate();
                    response.sendRedirect(request.getContextPath() + "/login?error=suspension");
                    return;
                }
                TenantContext.setCurrentTenant(empresaId);
            } else {
                TenantContext.setCurrentTenant(null);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
