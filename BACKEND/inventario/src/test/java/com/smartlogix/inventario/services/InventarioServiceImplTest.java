package com.smartlogix.inventario.services;

import com.smartlogix.inventario.entities.Bodega;
import com.smartlogix.inventario.entities.Inventario;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private BodegaRepository bodegaRepository;

    @Mock
    private MovimientoStockRepository movimientoRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private Bodega bodega;
    private Producto producto;
    private Inventario inventario;

    @BeforeEach
    void setUp() {
        bodega = new Bodega();
        bodega.setId(1L);
        bodega.setNombre("Bodega Central");

        producto = new Producto();
        producto.setId(10L);
        producto.setCodigo("PROD-001");
        producto.setNombre("Caja de cartón");

        inventario = new Inventario();
        inventario.setId(100L);
        inventario.setProducto(producto);
        inventario.setBodega(bodega);
        inventario.setCantidad(50);
    }

    @Test
    void findByBodega_bodegaExistente_retornaListaDeInventario() {
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(inventarioRepository.findByBodega(bodega)).thenReturn(List.of(inventario));

        List<Inventario> resultado = inventarioService.findByBodega(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(50, resultado.get(0).getCantidad());
    }

    @Test
    void findByBodega_bodegaNoExistente_lanzaExcepcion() {
        when(bodegaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inventarioService.findByBodega(99L));
        assertTrue(ex.getMessage().contains("Bodega no encontrada"));
    }

    @Test
    void findByProducto_productoExistente_retornaLista() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(inventarioRepository.findByProducto(producto)).thenReturn(List.of(inventario));

        List<Inventario> resultado = inventarioService.findByProducto(10L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    void findByProducto_productoNoExistente_lanzaExcepcion() {
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inventarioService.findByProducto(999L));
        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    @Test
    void sincronizar_stockInsuficiente_lanzaExcepcion() {
        Bodega destino = new Bodega();
        destino.setId(2L);
        destino.setNombre("Bodega Norte");

        inventario.setCantidad(5);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(bodegaRepository.findById(2L)).thenReturn(Optional.of(destino));
        when(inventarioRepository.findByProductoAndBodega(producto, bodega))
                .thenReturn(Optional.of(inventario));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inventarioService.sincronizar(10L, 1L, 2L, 20, "camila"));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void sincronizar_stockSuficiente_descuentaOrigenYAumentaDestino() {
        Bodega destino = new Bodega();
        destino.setId(2L);
        destino.setNombre("Bodega Norte");

        Inventario invDestino = new Inventario();
        invDestino.setId(200L);
        invDestino.setProducto(producto);
        invDestino.setBodega(destino);
        invDestino.setCantidad(10);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(bodegaRepository.findById(1L)).thenReturn(Optional.of(bodega));
        when(bodegaRepository.findById(2L)).thenReturn(Optional.of(destino));
        when(inventarioRepository.findByProductoAndBodega(producto, bodega))
                .thenReturn(Optional.of(inventario));
        when(inventarioRepository.findByProductoAndBodega(producto, destino))
                .thenReturn(Optional.of(invDestino));
        when(inventarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Inventario resultado = inventarioService.sincronizar(10L, 1L, 2L, 15, "camila");

        assertEquals(25, resultado.getCantidad());
        assertEquals(35, inventario.getCantidad());
    }
}
