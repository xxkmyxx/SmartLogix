import { useState } from 'react';
import api from '../../api/axios';
import './Seguimiento.css';

const PASOS_PEDIDO = ['PENDIENTE', 'CONFIRMADO', 'EN_PREPARACION', 'EN_TRANSITO', 'ENTREGADO'];
const PASOS_ENVIO  = ['PENDIENTE', 'RECOGIDO', 'EN_TRANSITO', 'ENTREGADO'];

const labelPedido = { PENDIENTE: 'Pendiente', CONFIRMADO: 'Confirmado', EN_PREPARACION: 'En preparación', EN_TRANSITO: 'En tránsito', ENTREGADO: 'Entregado', CANCELADO: 'Cancelado' };
const labelEnvio  = { PENDIENTE: 'Pendiente', RECOGIDO: 'Recogido', EN_TRANSITO: 'En tránsito', ENTREGADO: 'Entregado', CANCELADO: 'Cancelado' };

function Timeline({ pasos, estadoActual, labels, cancelado }) {
  const idxActual = pasos.indexOf(estadoActual);
  return (
    <div className="timeline">
      {pasos.map((paso, i) => {
        const completado = cancelado ? false : i <= idxActual;
        const activo     = !cancelado && i === idxActual;
        return (
          <div key={paso} className={`timeline-step ${completado ? 'completado' : ''} ${activo ? 'activo' : ''}`}>
            <div className="timeline-circulo">{completado ? '✓' : i + 1}</div>
            <span className="timeline-label">{labels[paso]}</span>
            {i < pasos.length - 1 && <div className={`timeline-linea ${i < idxActual && !cancelado ? 'completada' : ''}`} />}
          </div>
        );
      })}
    </div>
  );
}

export default function Seguimiento() {
  const [numeroPedido, setNumeroPedido] = useState('');
  const [resultado, setResultado]       = useState(null);
  const [buscando, setBuscando]         = useState(false);
  const [error, setError]               = useState('');

  const buscar = async (e) => {
    e.preventDefault();
    if (!numeroPedido.trim()) return;
    setBuscando(true);
    setError('');
    setResultado(null);
    try {
      const res = await api.get(`/api/public/seguimiento/${numeroPedido.trim()}`);
      setResultado(res.data);
    } catch (err) {
      setError(err.response?.status === 404 ? 'No se encontró el pedido. Verifica el número ingresado.' : 'Error al consultar. Intenta nuevamente.');
    } finally {
      setBuscando(false);
    }
  };

  const canceladoPedido = resultado?.estadoPedido === 'CANCELADO';
  const canceladoEnvio  = resultado?.estadoEnvio  === 'CANCELADO';

  return (
    <div className="seguimiento-page">
      <div className="seguimiento-card">
        <div className="seguimiento-header">
          <h1>SmartLogix</h1>
          <p>Seguimiento de pedido</p>
        </div>

        <form onSubmit={buscar} className="seguimiento-form">
          <input
            value={numeroPedido}
            onChange={(e) => setNumeroPedido(e.target.value)}
            placeholder="Número de pedido (ej: PED-000001)"
            className="seguimiento-input"
          />
          <button type="submit" className="seguimiento-btn" disabled={buscando}>
            {buscando ? 'Buscando...' : 'Consultar'}
          </button>
        </form>

        {error && <p className="seguimiento-error">{error}</p>}

        {resultado && (
          <div className="seguimiento-resultado">
            <div className="seguimiento-info">
              <div>
                <span className="info-label">Pedido</span>
                <span className="info-valor">{resultado.numeroPedido}</span>
              </div>
              <div>
                <span className="info-label">Cliente</span>
                <span className="info-valor">{resultado.clienteNombre}</span>
              </div>
              <div>
                <span className="info-label">Total</span>
                <span className="info-valor">${resultado.total?.toLocaleString('es-CL')}</span>
              </div>
              <div>
                <span className="info-label">Fecha</span>
                <span className="info-valor">
                  {resultado.fechaCreacion ? new Date(resultado.fechaCreacion).toLocaleDateString('es-CL') : '—'}
                </span>
              </div>
            </div>

            {canceladoPedido && (
              <div className="seguimiento-cancelado">Pedido cancelado</div>
            )}

            <h3 className="seguimiento-subtitulo">Estado del pedido</h3>
            <Timeline pasos={PASOS_PEDIDO} estadoActual={resultado.estadoPedido} labels={labelPedido} cancelado={canceladoPedido} />

            {resultado.envioId ? (
              <>
                <h3 className="seguimiento-subtitulo" style={{ marginTop: 32 }}>Estado del envío</h3>
                <div className="envio-info">
                  {resultado.nombreTransportista && resultado.nombreTransportista !== '—' && (
                    <span>Transportista: <strong>{resultado.nombreTransportista}</strong></span>
                  )}
                  {resultado.fechaEstimada && (
                    <span>Entrega estimada: <strong>{new Date(resultado.fechaEstimada + 'T00:00:00').toLocaleDateString('es-CL')}</strong></span>
                  )}
                </div>
                <Timeline pasos={PASOS_ENVIO} estadoActual={resultado.estadoEnvio} labels={labelEnvio} cancelado={canceladoEnvio} />
              </>
            ) : (
              <p className="seguimiento-sin-envio">El envío aún no ha sido generado.</p>
            )}
          </div>
        )}

        <a href="/login" className="seguimiento-login-link">Acceso empleados →</a>
      </div>
    </div>
  );
}
