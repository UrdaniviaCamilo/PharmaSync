package com.londres.drogueria.config;

import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Aspect
@Configuration
public class HibernateTenantFilterConfig {

    @Autowired
    private EntityManager entityManager;

    @Before("execution(* com.londres.drogueria.repository.*.*(..))")
    public void setTenantFilter() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            session.enableFilter("empresaFilter").setParameter("empresaId", tenantId);
        }
    }
}
