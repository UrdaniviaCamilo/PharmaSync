package com.londres.drogueria.service;

import com.londres.drogueria.model.Caja;
import com.londres.drogueria.model.MovimientoCaja;
import com.londres.drogueria.model.Usuario;
import com.londres.drogueria.repository.CajaRepository;
import com.londres.drogueria.repository.MovimientoCajaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CajaService {

    @Autowired
    private CajaRepository cajaRepository;

    @Autowired
    private MovimientoCajaRepository movimientoCajaRepository;

    public Optional<Caja> obtenerCajaAbierta(Long empresaId) {
        return cajaRepository.findFirstByEmpresaIdAndAbiertaTrue(empresaId);
    }

    @Transactional
    public Caja abrirCaja(BigDecimal montoInicial, Usuario usuario) {
        if (obtenerCajaAbierta(usuario.getEmpresaId()).isPresent()) {
            throw new RuntimeException("Ya existe una caja abierta para esta empresa.");
        }
        Caja caja = new Caja();
        caja.setMontoInicial(montoInicial);
        caja.setUsuario(usuario);
        caja.setEmpresaId(usuario.getEmpresaId());
        return cajaRepository.save(caja);
    }

    @Transactional
    public void cerrarCaja(Long empresaId) {
        Caja caja = obtenerCajaAbierta(empresaId)
                .orElseThrow(() -> new RuntimeException("No hay ninguna caja abierta para cerrar."));
        
        caja.setAbierta(false);
        caja.setFechaCierre(LocalDateTime.now());
        caja.setMontoFinal(caja.getMontoInicial().add(caja.getTotalVentas()).subtract(caja.getTotalGastos()));
        cajaRepository.save(caja);
    }

    @Transactional
    public void registrarMovimiento(Caja caja, String tipo, String descripcion, BigDecimal monto, Usuario usuario) {
        MovimientoCaja movimiento = new MovimientoCaja();
        movimiento.setCaja(caja);
        movimiento.setTipo(tipo);
        movimiento.setDescripcion(descripcion);
        movimiento.setMonto(monto);
        movimiento.setUsuario(usuario);
        movimientoCajaRepository.save(movimiento);

        if ("VENTA".equals(tipo) || "INGRESO".equals(tipo)) {
            caja.setTotalVentas(caja.getTotalVentas().add(monto));
        } else {
            caja.setTotalGastos(caja.getTotalGastos().add(monto));
        }
        cajaRepository.save(caja);
    }
}
