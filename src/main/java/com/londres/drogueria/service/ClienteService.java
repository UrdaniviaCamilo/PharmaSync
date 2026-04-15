package com.londres.drogueria.service;

import com.londres.drogueria.model.Cliente;
import com.londres.drogueria.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    public List<Cliente> buscar(String query) {
        return clienteRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDniContaining(query, query, query);
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> buscarPorDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    @Transactional
    public Cliente guardar(Cliente cliente) {
        if (cliente.getId() == null && cliente.getDni() != null && clienteRepository.existsByDni(cliente.getDni())) {
            throw new RuntimeException("El DNI " + cliente.getDni() + " ya está registrado en su empresa.");
        }
        return clienteRepository.save(cliente);
    }

    @Transactional
    public void eliminar(Long id) {
        clienteRepository.deleteById(id);
    }
}
