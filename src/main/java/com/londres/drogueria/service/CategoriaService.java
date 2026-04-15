package com.londres.drogueria.service;

import com.londres.drogueria.model.Categoria;
import com.londres.drogueria.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional
    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        
        if (!categoria.getProductos().isEmpty()) {
            throw new RuntimeException("No se puede eliminar una categoría que contiene productos.");
        }
        
        categoriaRepository.deleteById(id);
    }
}
