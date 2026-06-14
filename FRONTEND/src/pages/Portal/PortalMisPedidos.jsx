import { useEffect, useState } from 'react';
import portalApi from '../../api/portalApi';
import './PortalMisPedidos.css';

const ESTADO_LABEL = {
  PENDIENTE: 'Pendiente',
  CONFIRMADO: 'Confirmado',
  EN_PREPARACION: 'En preparación',
  EN_TRANSITO: 'En tránsito',
  ENTREGADO: 'Entregado',
  CANCELADO: 'Cancelado',
};

const ESTADO_CLASS = {
  PENDIENTE: 'estado-pendiente',
  CONFIRMADO: 'estado-confirmado',
  EN_PREPARACION: 'estado-preparacion',
  EN_TRANSITO: 'estado-transito',
  ENTREGADO: 'estado-entregado',
  CANCELADO: 'estado-cancelado',
};

export default function PortalMisPedidos() {
  const [pedidos, setPedidos]   = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError]       = useState('');
  const [abierto, setAbierto]   = useState(null);

  useEffect(() => {
    portalApi.get('/api/pedidos/mis-pedidos')
      .then((res) => setPedidos(res.data))
      .catch(() => setError('No se pudieron cargar tus pedidos.'))
      .finally(() => setCargando(false));
  }, []);

  if (cargando) return <p className="mispedidos-msg">Cargando pedidos...</p>;
  if (error)    return <p className="mispedidos-msg mispedidos-error">{error}</p>;

  return (
    <div className="mispedidos-page">
      <h2 className="mispedidos-titulo">Mis pedidos</h2>

      {pedidos.length === 0 ? (
        <p className="mispedidos-msg">Aún no tienes pedidos. Ve al catálogo para hacer el primero.</p>
      ) : (
        <div className="mispedidos-lista">
          {pedidos.map((p) => (
            <div key={p.id} className="mispedidos-card">
              <div className="mispedidos-card-header">
                <div>
                  <span className="mispedidos-numero">{p.numeroPedido}</span>
                  <span className={`estado-badge ${ESTADO_CLASS[p.estado]}`}>{ESTADO_LABEL[p.estado]}</span>
                </div>
                <div className="mispedidos-meta">
                  <span>{p.fechaCreacion ? new Date(p.fechaCreacion).toLocaleDateString('es-CL') : '—'}</span>
                  <strong>${p.total?.toLocaleString('es-CL')}</strong>
                </div>
              </div>

              <div className="mispedidos-acciones">
                <button
                  className="btn-detalle"
                  onClick={() => setAbierto(abierto === p.id ? null : p.id)}
                >
                  {abierto === p.id ? 'Ocultar detalle' : 'Ver detalle'}
                </button>
                <a href="/seguimiento" className="btn-seguimiento">Rastrear →</a>
              </div>

              {abierto === p.id && (
                <div className="mispedidos-detalle">
                  <table className="detalle-tabla">
                    <thead>
                      <tr>
                        <th>Producto</th>
                        <th>Cant.</th>
                        <th>Precio unit.</th>
                        <th>Subtotal</th>
                      </tr>
                    </thead>
                    <tbody>
                      {p.detalles?.map((d, i) => (
                        <tr key={i}>
                          <td>{d.productoNombre}</td>
                          <td>{d.cantidad}</td>
                          <td>${d.precioUnitario?.toLocaleString('es-CL')}</td>
                          <td>${d.subtotal?.toLocaleString('es-CL')}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
