import { api } from "../core/api.js";

/** Capa de servicios: un único lugar con todos los contratos HTTP del backend. */
export const unwrapList = (data) => (Array.isArray(data) ? data : data?.contenido ?? []);

export const authService = {
  login: (correo, contrasena) => api.login({ correo, contrasena })
};

export const menuService = {
  categorias: () => api.get("/api/menu/categorias", { tamano: 100 }).then(unwrapList),
  productos: (query = {}) => api.get("/api/menu/productos", { tamano: 100, ...query })
};

export const mesaService = {
  disponibles: () => api.get("/api/mesas/disponibles").then(unwrapList),
  listar: () => api.get("/api/admin/mesas").then(unwrapList),
  crear: (body) => api.post("/api/admin/mesas", body),
  actualizar: (id, body) => api.put(`/api/admin/mesas/${id}`, body),
  desactivar: (id) => api.delete(`/api/admin/mesas/${id}`)
};

export const pedidoService = {
  crear: (body) => api.post("/api/pedidos", body),
  obtener: (id) => api.get(`/api/pedidos/${id}`),
  misPedidos: () => api.get("/api/pedidos/mios").then(unwrapList),
  cola: () => api.get("/api/pedidos/cocina").then(unwrapList),
  historial: (id) => api.get(`/api/pedidos/${id}/historial`).then(unwrapList),
  cambiarEstado: (id, estado) => api.patch(`/api/pedidos/${id}/estado`, { estado }),
  agregarDetalle: (id, body) => api.post(`/api/pedidos/${id}/detalles`, body),
  actualizarDetalle: (id, detalleId, body) => api.put(`/api/pedidos/${id}/detalles/${detalleId}`, body),
  eliminarDetalle: (id, detalleId) => api.delete(`/api/pedidos/${id}/detalles/${detalleId}`)
};

export const adminService = {
  categorias: () => api.get("/api/admin/categorias").then(unwrapList),
  crearCategoria: (body) => api.post("/api/admin/categorias", body),
  actualizarCategoria: (id, body) => api.put(`/api/admin/categorias/${id}`, body),
  desactivarCategoria: (id) => api.delete(`/api/admin/categorias/${id}`),

  productos: () => api.get("/api/admin/productos").then(unwrapList),
  crearProducto: (body) => api.post("/api/admin/productos", body),
  actualizarProducto: (id, body) => api.put(`/api/admin/productos/${id}`, body),
  desactivarProducto: (id) => api.delete(`/api/admin/productos/${id}`),
  subirImagenProducto: (id, file) => {
    const form = new FormData();
    form.append("archivo", file);
    return api.post(`/api/admin/productos/${id}/imagen`, form);
  },
  eliminarImagenProducto: (id) => api.delete(`/api/admin/productos/${id}/imagen`),

  usuarios: (query) => api.get("/api/admin/usuarios", query),
  crearUsuario: (body) => api.post("/api/usuarios", body),
  actualizarUsuario: (id, body) => api.put(`/api/admin/usuarios/${id}`, body),
  cambiarEstadoUsuario: (id, activo) => api.patch(`/api/admin/usuarios/${id}/estado`, { activo }),
  restablecerContrasena: (id, contrasena) => api.patch(`/api/admin/usuarios/${id}/contrasena`, { contrasena }),

  pedidos: (query) => api.get("/api/admin/pedidos", query),
  pedido: (id) => api.get(`/api/admin/pedidos/${id}`),

  configuracionDomicilios: () => api.get("/api/admin/configuracion/domicilios"),
  actualizarConfiguracionDomicilios: (monto) =>
    api.put("/api/admin/configuracion/domicilios", { montoMaximoContraentrega: monto }),

  reporteVentas: (q) => api.get("/api/admin/reportes/ventas", q),
  reporteProductos: (q) => api.get("/api/admin/reportes/productos-mas-vendidos", q),
  reporteCancelaciones: (q) => api.get("/api/admin/reportes/cancelaciones", q),
  reporteTiempos: (q) => api.get("/api/admin/reportes/tiempo-preparacion", q)
};
