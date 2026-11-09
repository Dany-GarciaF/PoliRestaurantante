import { config } from "../config.js";
const money = new Intl.NumberFormat("es-CO", {
  style: "currency",
  currency: "COP",
  maximumFractionDigits: 0
});

export const formatMoney = (value) => money.format(Number(value ?? 0));

export const formatDateTime = (iso) =>
  iso
    ? new Intl.DateTimeFormat("es-CO", { dateStyle: "short", timeStyle: "short" }).format(new Date(iso))
    : "—";

export const formatTime = (iso) =>
  iso ? new Intl.DateTimeFormat("es-CO", { timeStyle: "short" }).format(new Date(iso)) : "—";

export function elapsedLabel(iso) {
  const seconds = Math.max(0, Math.floor((Date.now() - new Date(iso).getTime()) / 1000));
  const minutes = Math.floor(seconds / 60);
  if (minutes < 1) return "ahora";
  if (minutes < 60) return `${minutes} min`;
  return `${Math.floor(minutes / 60)} h ${minutes % 60} min`;
}

export function durationLabel(seconds) {
  if (seconds === null || seconds === undefined) return "Sin datos";
  const minutes = Math.floor(seconds / 60);
  const rest = seconds % 60;
  return minutes > 0 ? `${minutes} min ${rest} s` : `${rest} s`;
}

export const ESTADO_LABEL = {
  PENDIENTE: "Pendiente",
  EN_PREPARACION: "En preparación",
  LISTO: "Listo",
  ENTREGADO: "Entregado",
  CANCELADO: "Cancelado"
};

export const TIPO_LABEL = {
  SALON: "Salón",
  PARA_LLEVAR: "Para llevar",
  DOMICILIO: "Domicilio"
};

export const ROL_LABEL = {
  ADMINISTRADOR: "Administrador",
  MESERO: "Mesero",
  COCINA: "Cocina"
};

export const METODO_PAGO_LABEL = {
  EFECTIVO_CONTRAENTREGA: "Efectivo contraentrega",
  PAGO_VIRTUAL: "Pago virtual"
};

export const todayIso = () => new Date().toISOString().slice(0, 10);

export const daysAgoIso = (days) => {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date.toISOString().slice(0, 10);
};

export const imageUrl = (path) => (path ? new URL(path, config.apiBaseUrl).href : null);

