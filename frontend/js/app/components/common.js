import { h } from "../../core/dom.js";
import { ESTADO_LABEL } from "../../core/format.js";

export const statusBadge = (estado) =>
  h("span", { class: `badge badge--${estado}` }, ESTADO_LABEL[estado] ?? estado);

export const shortId = (id) => `#${String(id).slice(-5).toUpperCase()}`;

export function pageHeader(title, subtitle, ...actions) {
  return h(
    "div",
    { class: "page-head" },
    h("div", {}, h("h1", {}, title), subtitle ? h("p", {}, subtitle) : null),
    actions.length ? h("div", { class: "page-head__actions" }, actions) : null
  );
}

export const emptyState = (title, text = "") =>
  h("div", { class: "empty" }, h("span", { class: "hand" }, title), text);

export const loadingBlock = (count = 3) =>
  h("div", { class: "grid-cards" }, Array.from({ length: count }, () => h("div", { class: "skeleton" })));

export function errorState(error, retry) {
  return h(
    "div",
    { class: "empty" },
    h("span", { class: "hand" }, "Algo salió mal"),
    h("p", {}, error?.message ?? "Error desconocido"),
    retry ? h("button", { class: "btn btn--lime btn--sm", type: "button", onClick: retry }, "Reintentar") : null
  );
}

export function paginationBar({ pagina, totalPaginas, totalElementos }, onChange) {
  if (!totalPaginas || totalPaginas <= 1) {
    return h("p", { class: "pagination" }, `${totalElementos ?? 0} resultado(s)`);
  }
  return h(
    "div",
    { class: "pagination" },
    h("button", { class: "btn btn--cream btn--sm", type: "button", disabled: pagina === 0, onClick: () => onChange(pagina - 1) }, "← Anterior"),
    h("span", {}, `Página ${pagina + 1} de ${totalPaginas} · ${totalElementos} resultados`),
    h("button", { class: "btn btn--cream btn--sm", type: "button", disabled: pagina + 1 >= totalPaginas, onClick: () => onChange(pagina + 1) }, "Siguiente →")
  );
}

/** Ejecuta fn cada ms mientras la pestaña esté visible. Devuelve la función de parada. */
export function startPolling(fn, ms) {
  const timer = setInterval(() => {
    if (!document.hidden) fn();
  }, ms);
  return () => clearInterval(timer);
}
