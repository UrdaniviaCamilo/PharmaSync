package com.londres.drogueria.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;


@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String selectedRole = request.getParameter("selectedRole");


        boolean roleMatch = isRoleMatch(selectedRole, roles);

        if (!roleMatch) {

            request.getSession().invalidate();
            response.sendRedirect("/login?error=rol");
            return;
        }


        if (roles.contains("ROLE_SUPER_ADMIN")) {
            response.sendRedirect("/super-admin/dashboard");
        } else if (roles.contains("ROLE_CLIENTE")) {
            response.sendRedirect("/marketplace");
        } else {
            response.sendRedirect("/dashboard");
        }
    }


    private boolean isRoleMatch(String selectedRole, Set<String> roles) {
        if (selectedRole == null || selectedRole.isBlank()) {

            return true;
        }

        switch (selectedRole.toLowerCase()) {
            case "admin":

                return roles.contains("ROLE_SUPER_ADMIN") || roles.contains("ROLE_ADMIN");

            case "negocio":

                boolean isBusinessRole = roles.contains("ROLE_ADMIN")
                        || roles.contains("ROLE_VENDEDOR")
                        || roles.contains("ROLE_ALMACEN");
                boolean isSuperOrClient = roles.contains("ROLE_SUPER_ADMIN")
                        || (roles.contains("ROLE_CLIENTE") && !roles.contains("ROLE_ADMIN"));
                return isBusinessRole && !isSuperOrClient;

            case "cliente":
                return roles.contains("ROLE_CLIENTE");
            default:
                return true;
        }
    }
}
