import { useEffect, useState } from 'react';
import { usePortalAuth } from '../../context/PortalAuthContext';
import portalApi from '../../api/portalApi';
import './PortalCatalogo.css';

export default function PortalCatalogo() {
  const { user, nombre } = usePortalAuth();
  const [productos, setProductos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [carrito, setCarrito] = useState({});
  const [enviando, setEnviando] = useState(false);
  const [exito, setExito] = useState(null);
  const [errorPedido, setErrorPedido] = useState('');

  useEffect(() => {
    portalApi.get('/api/inventario/public/productos')
      .then((res) => setProductos(res.data))
      .catch(() => setProductos([]))
      .finally(() => setCargando(false));
  }, []);

  const agregar = (p) => setCarrito((c) => ({ ...c, [p.id]: { ...p, cantidad: (c[p.id]?.cantidad || 0) + 1 } }));
  const quitar  = (id) => setCarrito((c) => {
    const next = { ...c };
    if (next[id].cantidad <= 1) delete next[id];
    else next[id] = { ...next[id], cantidad: next[id].cantidad - 1 };
    return next;
  });

  const itemsCarrito = Object.values(carrito);
  const totalCarrito = itemsCarrito.reduce((s, i) => s + i.precio * i.cantidad, 0);

  const confirmarPedido = async () => {
    if (!itemsCarrito.length) return;
    setEnviando(true);
    setErrorPedido('');
    try {
      const detalles = itemsCarrito.map((i) => ({
        productoId: i.id,
        productoCodigo: i.codigo,
        productoNombre: i.nombre,
        cantidad: i.cantidad,
        precioUnitario: i.precio,
      }));
      const { data } = await portalApi.post('/api/pedidos', {
        clienteEmail: user?.sub,
        clienteNombre: nombre,
        detalles,
      });
      setExito(data.numeroPedido);
      setCarrito({});
    } catch (err) {
      setErrorPedido(err.response?.data?.error || 'Error al confirmar el pedido.');
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="catalogo-page">
      <div className="catalogo-izq">
        <h2 className="catalogo-titulo">Catálogo de productos</h2>

        {exito && (
          <div className="catalogo-exito">
            Pedido creado: <strong>{exito}</strong>. Puedes rastrearlo en{' '}
            <a href="/seguimiento">seguimiento</a>.
            <button onClick={() => setExito(null)}>Cerrar</button>
          </div>
        )}

        {cargando ? (
          <p className="catalogo-msg">Cargando productos...</p>
        ) : productos.length === 0 ? (
          <p className="catalogo-msg">No hay productos disponibles en este momento.</p>
        ) : (
          <div className="catalogo-grid">
            {productos.map((p) => (
              <div key={p.id} className="catalogo-card">
                {p.categoria && <span className="card-categoria">{p.categoria}</span>}
                <h3 className="card-nombre">{p.nombre}</h3>
                {p.descripcion && <p className="card-desc">{p.descripcion}</p>}
                <div className="card-footer">
                  <span className="card-precio">${p.precio?.toLocaleString('es-CL')}</span>
                  <span className="card-stock">Stock: {p.stockActual}</span>
                </div>
                {carrito[p.id] ? (
                  <div className="card-qty">
                    <button onClick={() => quitar(p.id)}>−</button>
                    <span>{carrito[p.id].cantidad}</span>
                    <button onClick={() => agregar(p)} disabled={carrito[p.id].cantidad >= p.stockActual}>+</button>
                  </div>
                ) : (
                  <button className="card-agregar" onClick={() => agregar(p)}>
                    Agregar al carrito
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="catalogo-carrito">
        <h3 className="carrito-titulo">Carrito</h3>
        {itemsCarrito.length === 0 ? (
          <p className="carrito-vacio">Aún no has agregado productos.</p>
        ) : (
          <>
            <div className="carrito-items">
              {itemsCarrito.map((i) => (
                <div key={i.id} className="carrito-item">
                  <div className="carrito-item-info">
                    <span className="carrito-item-nombre">{i.nombre}</span>
                    <span className="carrito-item-sub">{i.cantidad} × ${i.precio?.toLocaleString('es-CL')}</span>
                  </div>
                  <span className="carrito-item-total">${(i.precio * i.cantidad).toLocaleString('es-CL')}</span>
                </div>
              ))}
            </div>
            <div className="carrito-total">
              <span>Total</span>
              <strong>${totalCarrito.toLocaleString('es-CL')}</strong>
            </div>
            {errorPedido && <p className="carrito-error">{errorPedido}</p>}
            <button className="carrito-confirmar" onClick={confirmarPedido} disabled={enviando}>
              {enviando ? 'Confirmando...' : 'Confirmar pedido'}
            </button>
          </>
        )}
      </div>
    </div>
  );
}
