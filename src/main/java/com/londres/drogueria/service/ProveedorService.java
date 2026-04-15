package com.londres.drogueria.service;

import com.londres.drogueria.model.Proveedor;
import com.londres.drogueria.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private com.londres.drogueria.repository.ProductoRepository productoRepository;

    public List<Proveedor> listarTodos() {
        return proveedorRepository.findAll();
    }

    public List<Proveedor> buscar(String nombre) {
        return proveedorRepository.findByNombreContainingIgnoreCase(nombre);
    }

    public Optional<Proveedor> buscarPorId(Long id) {
        return proveedorRepository.findById(id);
    }

    @Transactional
    public Proveedor guardar(Proveedor proveedor) {
        if (proveedor.getId() == null && proveedor.getRuc() != null && proveedorRepository.existsByRuc(proveedor.getRuc())) {
            throw new RuntimeException("El RUC " + proveedor.getRuc() + " ya está registrado en su empresa.");
        }
        

        List<com.londres.drogueria.model.Producto> selectedProducts = proveedor.getProductos();
        

        Proveedor saved = proveedorRepository.save(proveedor);


        List<com.londres.drogueria.model.Producto> currentProducts = productoRepository.findByProveedorId(saved.getId());
        for (com.londres.drogueria.model.Producto p : currentProducts) {
            p.setProveedor(null);
        }
        productoRepository.saveAll(currentProducts);
        
        // Link new products
        if (selectedProducts != null && !selectedProducts.isEmpty()) {
            for (com.londres.drogueria.model.Producto p : selectedProducts) {
                if (p.getId() != null) {
                    com.londres.drogueria.model.Producto dbProduct = productoRepository.findById(p.getId()).orElse(null);
                    if (dbProduct != null) {
                       dbProduct.setProveedor(saved);
                       productoRepository.save(dbProduct);
                    }
                }
            }
        }

        return saved;
    }

    @Transactional
    public void eliminar(Long id) {
        proveedorRepository.deleteById(id);
    }
}
