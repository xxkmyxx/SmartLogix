package com.smartlogix.inventario.services;

import com.smartlogix.inventario.entities.Producto;
import com.smartlogix.inventario.repositories.BodegaRepository;
import com.smartlogix.inventario.repositories.InventarioRepository;
import com.smartlogix.inventario.repositories.MovimientoStockRepository;
import com.smartlogix.inventario.repositories.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private BodegaRepository bodegaRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private MovimientoStockRepository movimientoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setCodigo("PROD-001");
        producto.setNombre("Caja de cartón");
        producto.setDescripcion("Caja estándar");
        producto.setCategoria("Embalaje");
        producto.setPrecio(500.0);
        producto.setStockActual(100);
        producto.setStockMinimo(10);
        producto.setActivo(true);
        producto.setFechaCreacion(LocalDateTime.now());
    }

    @Test
    void findAll_retornaProductosActivos() {
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(producto));
        List<Producto> resultado = productoService.findAll();
        assertEquals(1, resultado.size());
        assertEquals("PROD-001", resultado.get(0).getCodigo());
    }

    @Test
    void findById_existente_retornaProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        Optional<Producto> resultado = productoService.findById(1L);
        assertTrue(resultado.isPresent());
        assertEquals("Caja de cartón", resultado.get().getNombre());
    }

    @Test
    void findById_noExistente_retornaVacio() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<Producto> resultado = productoService.findById(99L);
        assertFalse(resultado.isPresent());
    }

    @Test
    void crear_conCodigo_guardaProducto() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        Producto resultado = productoService.crear("PROD-001", "Caja de cartón",
                "Descripción", "Embalaje", 500.0, 100, 10);
        assertNotNull(resultado);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crear_sinCodigo_generaCodigoAutomatico() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        Producto resultado = productoService.crear(null, "Producto sin código",
                "Desc", "General", 100.0, 50, 5);
        assertNotNull(resultado);
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void actualizar_productoExistente_actualizaDatos() {
        Producto datosNuevos = new Producto();
        datosNuevos.setNombre("Caja Grande");
        datosNuevos.setDescripcion("Caja actualizada");
        datosNuevos.setCategoria("Embalaje");
        datosNuevos.setPrecio(750.0);
        datosNuevos.setStockActual(200);
        datosNuevos.setStockMinimo(20);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto resultado = productoService.actualizar(1L, datosNuevos);

        assertEquals("Caja Grande", resultado.getNombre());
        assertEquals(750.0, resultado.getPrecio());
    }

    @Test
    void actualizar_productoNoExistente_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.actualizar(99L, new Producto()));
        assertTrue(ex.getMessage().contains("no encontrado"));
    }

    @Test
    void desactivar_productoExistente_marcaComoInactivo() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        productoService.desactivar(1L);

        assertFalse(producto.getActivo());
        verify(productoRepository).save(producto);
    }

    @Test
    void desactivar_productoNoExistente_lanzaExcepcion() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.desactivar(99L));
        assertTrue(ex.getMessage().contains("no encontrado"));
    }

    @Test
    void findBajoStockMinimo_retornaProductosBajoStock() {
        when(productoRepository.findProductosBajoStock()).thenReturn(List.of(producto));
        List<Producto> resultado = productoService.findBajoStockMinimo();
        assertEquals(1, resultado.size());
    }

    @Test
    void ajustarStockInterno_stockSuficiente_actualizaStock() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        productoService.ajustarStockInterno(1L, -10);

        assertEquals(90, producto.getStockActual());
    }

    @Test
    void ajustarStockInterno_stockInsuficiente_lanzaExcepcion() {
        producto.setStockActual(5);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.ajustarStockInterno(1L, -10));
        assertTrue(ex.getMessage().contains("insuficiente"));
    }

    @Test
    void historialMovimientos_productoExistente_retornaLista() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(movimientoRepository.findByProductoOrderByFechaMovimientoDesc(producto))
                .thenReturn(List.of());

        var resultado = productoService.historialMovimientos(1L);
        assertNotNull(resultado);
    }
}
