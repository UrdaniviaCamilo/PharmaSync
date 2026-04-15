package com.londres.drogueria.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final TenantFilter tenantFilter;

    @Autowired
    private CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(UserDetailsService userDetailsService, TenantFilter tenantFilter) {
        this.userDetailsService = userDetailsService;
        this.tenantFilter = tenantFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/registro", "/registro-empresa", "/registro-cliente", "/super-admin/registro",
                                "/login", "/css/**", "/js/**", "/img/**",
                                "/images/**", "/webjars/**", "/revivir", "/api/ruc", "/api/sunat/**", "/reclamaciones")
                        .permitAll()
                        // Super Admin
                        .requestMatchers("/super-admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/admin/recetas/**").hasAnyRole("ADMIN", "VENDEDOR")

                        .requestMatchers("/dashboard", "/perfil/**", "/perfil", "/admin/rendimiento/**", "/admin/rendimiento", "/marketplace/**")
                        .authenticated()

                        .requestMatchers("/admin/usuarios/**", "/admin/reportes/**").hasAnyRole("ADMIN", "SUPER_ADMIN")

                        .requestMatchers("/admin/ajustes/**").hasRole("ADMIN")

                        .requestMatchers("/admin/ventas/**", "/admin/clientes/**", "/admin/caja/**")
                        .hasAnyRole("ADMIN", "VENDEDOR")

                        .requestMatchers("/admin/productos/**")
                        .hasAnyRole("ADMIN", "ALMACEN", "VENDEDOR")
                        .requestMatchers("/admin/categorias/**", "/admin/proveedores/**",
                                "/admin/codigos/**", "/almacen/**")
                        .hasAnyRole("ADMIN", "ALMACEN")

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/vendedor/**").hasRole("VENDEDOR")
                        .requestMatchers("/buscar/**").hasAnyRole("ADMIN", "VENDEDOR", "ALMACEN")
                        .requestMatchers("/ventas/**").hasAnyRole("ADMIN", "VENDEDOR")

                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
