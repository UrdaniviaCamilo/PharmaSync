package com.londres.drogueria.service;

import com.londres.drogueria.model.Producto;
import com.londres.drogueria.model.MovimientoStock;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.repository.ProductoRepository;
import com.londres.drogueria.repository.MovimientoStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoStockRepository movimientoRepository;

    public List<Producto> listarTodos(Long empresaId) {
        return productoRepository.findFiltered(empresaId, null, null, false);
    }

    public List<Producto> buscar(Long empresaId, String query, Long categoriaId, boolean stockBajo) {
        return productoRepository.findFiltered(empresaId, query, categoriaId, stockBajo);
    }

    public List<Producto> listarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId);
    }

    public List<Producto> listarPorVencimientoProximo(Long empresaId) {
        return productoRepository.findExpiringProducts(empresaId, java.time.LocalDate.now().plusDays(30));
    }

    public List<Producto> listarPorLaboratorio(String laboratorio) {
        return productoRepository.findByLaboratorioContainingIgnoreCase(laboratorio);
    }

    public List<Producto> buscarPredictivo(String query) {
        // Simple search but could be optimized
        return productoRepository.findByNombreContainingIgnoreCaseOrLaboratorioContainingIgnoreCaseOrPrincipioActivoContainingIgnoreCase(query, query, query)
                .stream().limit(10).toList();
    }

    public List<Producto> stockBajo(Long empresaId) {
        return productoRepository.findLowStockProducts(empresaId);
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    @Transactional
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    @Transactional
    public void eliminar(Long id) {
        productoRepository.deleteById(id);
    }

    @Transactional
    public void actualizarStock(Long id, Integer cantidad, String operacion, String motivo, Usuario usuario) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        
        Integer stockAnterior = producto.getStock();
        Integer nuevoStock = stockAnterior;

        if ("INGRESO".equals(operacion)) {
            nuevoStock += cantidad;
        } else if ("SALIDA".equals(operacion)) {
            if (stockAnterior < cantidad) {
                throw new RuntimeException("Stock insuficiente para realizar la salida");
            }
            nuevoStock -= cantidad;
        }

        producto.setStock(nuevoStock);
        productoRepository.save(producto);

        // Registro en Kardex
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProducto(producto);
        movimiento.setCantidad(cantidad);
        movimiento.setTipo(operacion);
        movimiento.setObservaciones(motivo);
        movimiento.setUsuario(usuario);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setEmpresaId(producto.getEmpresaId());
        movimientoRepository.save(movimiento);
    }

    @Transactional
    public void actualizarStock(Long id, Integer cantidad, String operacion) {
        actualizarStock(id, cantidad, operacion, "Ajuste manual / Venta", null);
    }
}
