 package com.londres.drogueria.service;

import com.londres.drogueria.model.*;
import com.londres.drogueria.repository.VentaRepository;
import com.londres.drogueria.repository.ProductoRepository;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CajaService cajaService;

    @Autowired
    private com.londres.drogueria.repository.ConfiguracionRepository configuracionRepository;

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    public List<Venta> buscarVentasFiltradas(Long empresaId, String numero, String cliente, MetodoPago metodo) {
        return ventaRepository.findFiltered(empresaId, numero, cliente, metodo);
    }

    @Transactional
    public Venta registrarVenta(Venta venta, Usuario usuario) {
        // 1. Verify open box (Only for POS sales with a physical user)
        Caja caja = null;
        if (usuario != null) {
            caja = cajaService.obtenerCajaAbierta(usuario.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("No hay una caja abierta para registrar la venta."));
        }

        // 2. Generate sale number and status
        if (venta.getNumeroVenta() == null || venta.getNumeroVenta().isEmpty()) {
            venta.setNumeroVenta((usuario != null ? "V-" : "MP-") + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        if (venta.getEstado() == null) {
            venta.setEstado(VentaEstado.NUEVA);
        }

        venta.setUsuario(usuario);
        venta.setFecha(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            
            if (producto.getStock() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            // Only deduct stock if the sale is completed, approved, or paid (not pending validation)
            if (venta.getEstado() != VentaEstado.VALIDACION_PENDIENTE) {
                productoService.actualizarStock(
                    producto.getId(), 
                    detalle.getCantidad(), 
                    "SALIDA", 
                    "Venta " + (venta.getNumeroVenta()), 
                    usuario
                );
            }

            // Calculate item detail
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(producto.getPrecioVenta().multiply(new BigDecimal(detalle.getCantidad())));
            total = total.add(detalle.getSubtotal());
            detalle.setVenta(venta);
            detalle.setEmpresaId(producto.getEmpresaId());
        }

        venta.setTotal(total);
        if (venta.getEmpresaId() == null && !venta.getDetalles().isEmpty()) {
            venta.setEmpresaId(venta.getDetalles().get(0).getEmpresaId());
        }
        
        Venta savedVenta = ventaRepository.save(venta);

        // 3. Register movement in box if applicable
        if (caja != null && (venta.getEstado() == VentaEstado.PAGADA || venta.getEstado() == VentaEstado.NUEVA)) {
            cajaService.registrarMovimiento(caja, "VENTA", "Venta N° " + savedVenta.getNumeroVenta(), total, usuario);
        }

        return savedVenta;
    }

    @Transactional
    public Venta actualizarEstado(Long ventaId, VentaEstado nuevoEstado, String observacion, Usuario usuario) {
        Venta v = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        
        VentaEstado anterior = v.getEstado();
        v.setEstado(nuevoEstado);
        if (observacion != null) v.setObservaciones(observacion);

        // Marketplace con receta: el stock se descuenta al pagar (PAGADA), no al aprobar la receta.

        return ventaRepository.save(v);
    }

    /**
     * Cliente confirma pago tras receta aprobada. Descuenta stock solo si hay receta asociada.
     */
    @Transactional
    public Venta marcarPagadaMarketplace(Long ventaId, MetodoPago metodo, Usuario comprador) {
        Venta v = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
        if (v.getMarketplaceUsuario() == null
                || comprador == null
                || !v.getMarketplaceUsuario().getId().equals(comprador.getId())) {
            throw new RuntimeException("No autorizado para pagar este pedido.");
        }
        if (v.getEstado() != VentaEstado.APROBADA) {
            throw new RuntimeException("Este pedido no está listo para pago.");
        }
        v.setMetodoPago(metodo);
        v.setEstado(VentaEstado.PAGADA);
        if (v.getRecetaPath() != null && !v.getRecetaPath().isBlank()) {
            for (DetalleVenta d : v.getDetalles()) {
                productoService.actualizarStock(
                        d.getProducto().getId(),
                        d.getCantidad(),
                        "SALIDA",
                        "Pago marketplace " + v.getNumeroVenta(),
                        comprador);
            }
        }
        return ventaRepository.save(v);
    }

    public byte[] generarBoletaPdf(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Logo handle
        String logoPath = System.getProperty("user.dir") + "/uploads/logo.png";
        if (Files.exists(Paths.get(logoPath))) {
            try {
                Image logo = new Image(ImageDataFactory.create(logoPath));
                logo.setWidth(80);
                logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                document.add(logo);
            } catch (Exception e) {
                // Skip logo if error
            }
        }

        // Styling and Content
        Configuracion config = configuracionRepository.findByEmpresaId(venta.getEmpresaId()).orElse(new Configuracion());
        
        document.add(new Paragraph(config.getNombreFarmacia().toUpperCase())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(20)
                .setBold()
                .setMarginBottom(0));
        
        document.add(new Paragraph(config.getEslogan() != null ? config.getEslogan() : "PharmaSync - Innovación en Salud")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setItalic()
                .setMarginBottom(10));

        document.add(new Paragraph("DOC. ELECTRÓNICO: " + venta.getNumeroVenta())
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(14));
        
        document.add(new Paragraph("----------------------------------------------------------------------------------------------------")
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));

        document.add(new Paragraph("Fecha: " + venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        document.add(new Paragraph("Cliente: " + (venta.getCliente() != null && venta.getCliente().getId() != null ? (venta.getCliente().getNombres() + " " + venta.getCliente().getApellidos()) : "Cliente General (Consumidor Final)")));
        document.add(new Paragraph("Método de Pago: " + venta.getMetodoPago()));
        document.add(new Paragraph("\n"));

        // Table
        Table table = new Table(UnitValue.createPointArray(new float[]{3, 1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        
        table.addHeaderCell(new Cell().add(new Paragraph("Producto").setBold().setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().add(new Paragraph("Cant.").setBold().setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().add(new Paragraph("P. Unit").setBold().setTextAlignment(TextAlignment.CENTER)));
        table.addHeaderCell(new Cell().add(new Paragraph("Subtotal").setBold().setTextAlignment(TextAlignment.CENTER)));

        for (DetalleVenta d : venta.getDetalles()) {
            table.addCell(new Cell().add(new Paragraph(d.getProducto().getNombre())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(d.getCantidad())).setTextAlignment(TextAlignment.CENTER)));
            table.addCell(new Cell().add(new Paragraph("S/ " + d.getPrecioUnitario().setScale(2, RoundingMode.HALF_UP)).setTextAlignment(TextAlignment.RIGHT)));
            table.addCell(new Cell().add(new Paragraph("S/ " + d.getSubtotal().setScale(2, RoundingMode.HALF_UP)).setTextAlignment(TextAlignment.RIGHT)));
        }

        document.add(table);

        // Summary
        BigDecimal total = venta.getTotal();
        BigDecimal subtotal = total.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotal);

        document.add(new Paragraph("\nSubtotal: S/ " + subtotal).setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("IGV (18%): S/ " + igv).setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("TOTAL: S/ " + total).setTextAlignment(TextAlignment.RIGHT).setBold().setFontSize(14));
        
        document.add(new Paragraph("\n\nGracias por su preferencia.")
                .setTextAlignment(TextAlignment.CENTER)
                .setItalic()
                .setFontSize(9));

        document.close();
        return baos.toByteArray();
    }
}
