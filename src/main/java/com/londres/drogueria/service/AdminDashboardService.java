package com.londres.drogueria.service;

import com.londres.drogueria.model.*;
import com.londres.drogueria.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import com.londres.drogueria.model.Usuario;

@Service
public class AdminDashboardService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private CajaRepository cajaRepository;

    @Autowired
    private MovimientoStockRepository movimientoRepository;

    public Map<String, Object> getDashboardStats(Long empresaId) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);
        LocalDateTime sevenDaysAgo = now.minusDays(7).with(LocalTime.MIN);
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate next30Days = today.plusDays(30);

        // Core KPIs
        stats.put("ventasHoy", ventaRepository.sumTotalByFechaAfter(empresaId, startOfDay));
        stats.put("cantidadVentasHoy", ventaRepository.countByFechaAfter(empresaId, startOfDay));
        stats.put("ventasMes", ventaRepository.sumTotalByFechaAfter(empresaId, startOfMonth));
        stats.put("cantidadVentasMes", ventaRepository.countByFechaAfter(empresaId, startOfMonth));
        
        stats.put("totalProductos", productoRepository.countByEmpresaId(empresaId));
        List<Producto> lowStock = productoRepository.findLowStockProducts(empresaId);
        stats.put("cantidadStockBajo", lowStock.size());
        
        // Expiration logic
        List<Producto> expiring = productoRepository.findExpiringProducts(empresaId, next30Days);
        stats.put("listaPorVencer", expiring);
        stats.put("cantidadPorVencer", expiring.size());
        
        stats.put("totalClientes", clienteRepository.countByEmpresaId(empresaId));
        stats.put("clientesNuevosMes", clienteRepository.countByFechaCreacionAfter(empresaId, startOfMonth));
        
        stats.put("usuariosActivos", usuarioRepository.countByEmpresaIdAndActivoTrue(empresaId));
        stats.put("totalProveedores", proveedorRepository.countByEmpresaId(empresaId));
        stats.put("totalCodigos", productoRepository.countByEmpresaId(empresaId));
        
        // Caja balance
        stats.put("cajaAbierta", cajaRepository.findFirstByEmpresaIdAndAbiertaTrue(empresaId).isPresent());
        stats.put("saldoCaja", cajaRepository.findFirstByEmpresaIdAndAbiertaTrue(empresaId)
                .map(c -> c.getMontoInicial().add(c.getTotalVentas()).subtract(c.getTotalGastos()))
                .orElse(BigDecimal.ZERO));

        // Listas para tablas
        stats.put("ultimasVentas", ventaRepository.findTop10ByOrderByFechaDesc(empresaId));
        stats.put("listaStockBajo", productoRepository.findTop10LowStockProducts(empresaId));


        stats.put("ventasPorDia", formatChartData(ventaRepository.findSalesByDayAfter(empresaId, sevenDaysAgo)));
        stats.put("ventasPorMetodoPago", formatChartData(ventaRepository.findSalesByMetodoPago(empresaId)));
        stats.put("topProductos", formatChartData(ventaRepository.findTop5MostSoldProducts(empresaId)));
        stats.put("productosPorCategoria", formatChartData(productoRepository.countProductsByCategory(empresaId)));
        

        stats.put("totalNotificaciones", (long) lowStock.size() + expiring.size());

        return stats;
    }

    public Map<String, Object> getAlmacenStats(Long empresaId) {
        Map<String, Object> stats = new HashMap<>();
        java.time.LocalDate next30Days = java.time.LocalDate.now().plusDays(30);

        stats.put("totalProductos", productoRepository.countByEmpresaId(empresaId));
        List<Producto> lowStock = productoRepository.findLowStockProducts(empresaId);
        stats.put("cantidadStockBajo", lowStock.size());
        stats.put("listaStockBajo", productoRepository.findTop10LowStockProducts(empresaId));

        List<Producto> expiring = productoRepository.findExpiringProducts(empresaId, next30Days);
        stats.put("cantidadPorVencer", expiring.size());
        stats.put("listaPorVencer", expiring);

        stats.put("totalProveedores", proveedorRepository.countByEmpresaId(empresaId));
        stats.put("movimientosRecientes", movimientoRepository.findTop10ByEmpresaIdOrderByFechaDesc(empresaId));
        stats.put("totalNotificaciones", (long) lowStock.size() + expiring.size());

        // Chart data for almacen dashboard
        stats.put("productosPorCategoria", formatChartData(productoRepository.countProductsByCategory(empresaId)));
        stats.put("stockChartData", formatStockChartData(lowStock));

        return stats;
    }

    public Map<String, Object> getVendedorStats(Long empresaId, Usuario usuario) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7).with(LocalTime.MIN);

        stats.put("misVentasHoy", ventaRepository.sumTotalByUsuarioAndFechaAfter(usuario.getId(), startOfDay));
        stats.put("cantidadMisVentas", ventaRepository.countByUsuarioAndFechaAfter(usuario.getId(), startOfDay));
        
        stats.put("ventasHoyGlobal", ventaRepository.sumTotalByFechaAfter(empresaId, startOfDay));
        
        stats.put("cajaAbierta", cajaRepository.findFirstByEmpresaIdAndAbiertaTrue(empresaId).isPresent());
        stats.put("saldoCaja", cajaRepository.findFirstByEmpresaIdAndAbiertaTrue(empresaId)
                .map(c -> c.getMontoInicial().add(c.getTotalVentas()).subtract(c.getTotalGastos()))
                .orElse(BigDecimal.ZERO));

        stats.put("misUltimasVentas", ventaRepository.findTop10ByOrderByFechaDesc(empresaId));
        

        stats.put("ventasPorDia", formatChartData(ventaRepository.findSalesByDayAfter(empresaId, sevenDaysAgo)));
        stats.put("ventasPorMetodoPago", formatChartData(ventaRepository.findSalesByMetodoPago(empresaId)));

        List<Producto> lowStock = productoRepository.findLowStockProducts(empresaId);
        java.time.LocalDate next30Days = java.time.LocalDate.now().plusDays(30);
        List<Producto> expiring = productoRepository.findExpiringProducts(empresaId, next30Days);
        stats.put("totalNotificaciones", (long) lowStock.size() + expiring.size());

        return stats;
    }

    private List<List<Object>> formatStockChartData(List<Producto> products) {
        List<List<Object>> formatted = new ArrayList<>();
        if (products == null) return formatted;
        
        for (Producto p : products) {
            List<Object> row = new ArrayList<>();
            row.add(p.getNombre());
            row.add(p.getStock());
            row.add(p.getStockMinimo());
            formatted.add(row);
        }
        return formatted;
    }

    private List<List<Object>> formatChartData(List<Object[]> raw) {
        List<List<Object>> formatted = new ArrayList<>();
        if (raw == null) return formatted;
        
        for (Object[] row : raw) {
            List<Object> refinedList = new ArrayList<>();
            for (Object obj : row) {
                if (obj instanceof MetodoPago) {
                    refinedList.add(((MetodoPago) obj).getDisplayName());
                } else if (obj instanceof BigDecimal || obj instanceof Long || obj instanceof Integer) {
                    refinedList.add(obj);
                } else if (obj != null) {
                    refinedList.add(obj.toString());
                } else {
                    refinedList.add(null);
                }
            }
            formatted.add(refinedList);
        }
        return formatted;
    }
}
