import { h, mount } from "../../core/dom.js";
import { formatTime, elapsedLabel, TIPO_LABEL } from "../../core/format.js";
import { toast, withButton } from "../../core/ui.js";
import { config } from "../../config.js";
import { imageUrl } from "../../core/format.js";
import { menuService, pedidoService, unwrapList } from "../services.js";
import { pageHeader, statusBadge, shortId, emptyState, errorState, loadingBlock, startPolling } from "../components/common.js";

const COLUMNAS = [
  { estado: "PENDIENTE", titulo: "Pendientes", siguiente: "EN_PREPARACION", accion: "Empezar a preparar" },
  { estado: "EN_PREPARACION", titulo: "En preparación", siguiente: "LISTO", accion: "Marcar listo" }
];

export function renderCocina(container) {
  let conocidos = null;
  let pedidos = [];
  const board = h("div", { class: "kanban" });
  const summary = h("div", { class: "summary" });
  const fotos = new Map();

  async function loadFotos() {
    try {
      unwrapList(await menuService.productos()).forEach((p) => p.imagenUrl && fotos.set(p.id, imageUrl(p.imagenUrl)));
      paint();
    } catch {
      /* las fotos son opcionales */
    }
  }

  const urgencia = (p) => {
    const minutos = (Date.now() - new Date(p.fechaCreacion).getTime()) / 60000;
    return minutos >= 15 ? "late" : minutos >= 8 ? "warn" : "ok";
  };

  function ticket(p) {
    const col = COLUMNAS.find((c) => c.estado === p.estado);
    const button = h("button", { class: "btn btn--lime btn--block", type: "button" }, col.accion);
    button.addEventListener("click", async () => {
      try {
        await withButton(button, () => pedidoService.cambiarEstado(p.id, col.siguiente));
        toast(`${shortId(p.id)} → ${col.siguiente === "LISTO" ? "listo" : "en preparación"}`, "ok");
        load();
      } catch (error) {
        toast(error.message, "error");
        load();
      }
    });
    return h(
      "article",
      { class: `ticket ticket--${urgencia(p)}` },
      h(
        "div",
        { class: "ticket__head" },
        h("div", { class: "ticket__where" }, p.tipo === "SALON" ? `Mesa ${p.numeroMesa ?? "—"}` : TIPO_LABEL[p.tipo]),
        h("div", { class: "ticket__time" }, h("strong", {}, elapsedLabel(p.fechaCreacion)), h("small", {}, `${shortId(p.id)} · ${formatTime(p.fechaCreacion)}`))
      ),
      h(
        "ul",
        { class: "ticket__items" },
        p.detalles.map((d) =>
          h(
            "li",
            {},
            fotos.has(d.productoId) ? h("img", { class: "thumb", src: fotos.get(d.productoId), alt: "" }) : h("span", { class: "thumb thumb--empty" }, "🍔"),
            h("span", { class: "ticket__qty" }, `${d.cantidad}×`),
            h("span", {}, d.nombreProducto, d.notas ? h("em", {}, d.notas) : null)
          )
        )
      ),
      button
    );
  }

  function paint() {
    const retrasados = pedidos.filter((p) => urgencia(p) === "late").length;
    mount(
      summary,
      COLUMNAS.map((c) => h("span", { class: "summary__item" }, h("strong", {}, pedidos.filter((p) => p.estado === c.estado).length), c.titulo)),
      retrasados ? h("span", { class: "summary__item summary__item--late" }, h("strong", {}, retrasados), "Con retraso (+15 min)") : null
    );
    mount(
      board,
      COLUMNAS.map((col) => {
        const items = pedidos.filter((p) => p.estado === col.estado);
        return h(
          "section",
          { class: `kanban__col kanban__col--${col.estado}` },
          h("h2", {}, col.titulo, h("span", { class: "kanban__count" }, items.length)),
          items.length ? items.map(ticket) : emptyState("Todo al día", "Nada por aquí.")
        );
      })
    );
  }

  async function load(initial = false) {
    try {
      pedidos = (await pedidoService.cola()).sort((a, b) => new Date(a.fechaCreacion) - new Date(b.fechaCreacion));
      const ids = new Set(pedidos.map((p) => p.id));
      if (conocidos) {
        const nuevos = pedidos.filter((p) => !conocidos.has(p.id) && p.estado === "PENDIENTE");
        if (nuevos.length) toast(`🔔 ${nuevos.length} pedido(s) nuevo(s) en cola`, "info");
      }
      conocidos = ids;
      paint();
    } catch (error) {
      if (initial) mount(board, errorState(error, () => load(true)));
    }
  }

  mount(container, pageHeader("Cola de cocina", "Los pedidos más antiguos primero."), summary, board);
  mount(board, loadingBlock(2));
  load(true);
  loadFotos();
  const stopPolling = startPolling(load, config.pollIntervalMs);
  const tick = setInterval(() => !document.hidden && pedidos.length && paint(), 30000);
  return () => {
    stopPolling();
    clearInterval(tick);
  };
}
