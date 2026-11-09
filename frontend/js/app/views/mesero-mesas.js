import { h, mount } from "../../core/dom.js";
import { formatMoney, elapsedLabel } from "../../core/format.js";
import { config } from "../../config.js";
import { mesaService, pedidoService } from "../services.js";
import { pageHeader, statusBadge, errorState, loadingBlock, startPolling } from "../components/common.js";

const ACTIVOS = ["PENDIENTE", "EN_PREPARACION", "LISTO"];

/** Guarda la intención del mesero para que "Nuevo pedido" abra ya configurado. */
export const NUEVO_KEY = "poli.nuevo";

function iniciarPedido(intencion) {
  sessionStorage.setItem(NUEVO_KEY, JSON.stringify(intencion));
  location.hash = "#/nuevo";
}

export function renderMapaMesas(container) {
  const board = h("div", { class: "tables" });
  const quick = h(
    "div",
    { class: "quick" },
    h("button", { class: "quick__btn", type: "button", onClick: () => iniciarPedido({ tipo: "PARA_LLEVAR" }) }, h("span", {}, "🥡"), "Para llevar"),
    h("button", { class: "quick__btn", type: "button", onClick: () => iniciarPedido({ tipo: "DOMICILIO" }) }, h("span", {}, "🛵"), "Domicilio")
  );

  async function load(initial = false) {
    try {
      const [libres, mios] = await Promise.all([mesaService.disponibles(), pedidoService.misPedidos()]);
      const ocupadas = mios.filter((p) => p.tipo === "SALON" && p.numeroMesa != null && ACTIVOS.includes(p.estado));
      const tiles = [
        ...libres.map((m) => ({ numero: m.numero, libre: true, mesa: m })),
        ...ocupadas.map((p) => ({ numero: p.numeroMesa, libre: false, pedido: p }))
      ].sort((a, b) => a.numero - b.numero);

      mount(
        board,
        tiles.length
          ? tiles.map((t, i) =>
              t.libre
                ? h(
                    "button",
                    { class: "table-tile table-tile--free", type: "button", style: { "--i": i % 12 }, onClick: () => iniciarPedido({ tipo: "SALON", mesaId: t.mesa.id }) },
                    h("span", { class: "table-tile__num" }, t.numero),
                    h("span", { class: "table-tile__state" }, "Libre"),
                    h("span", { class: "table-tile__cta" }, "Tomar pedido +")
                  )
                : h(
                    "a",
                    { class: `table-tile table-tile--busy table-tile--${t.pedido.estado}`, href: "#/pedidos", style: { "--i": i % 12 } },
                    h("span", { class: "table-tile__num" }, t.numero),
                    statusBadge(t.pedido.estado),
                    h("span", { class: "table-tile__meta" }, `${elapsedLabel(t.pedido.fechaCreacion)} · ${formatMoney(t.pedido.total)}`)
                  )
            )
          : h("p", { class: "hint" }, "No hay mesas libres ni pedidos activos en mesa.")
      );
    } catch (error) {
      if (initial) mount(board, errorState(error, () => load(true)));
    }
  }

  mount(container, pageHeader("Mesas", "Toca una mesa libre para tomar su pedido; las ocupadas muestran el estado."), quick, board);
  mount(board, loadingBlock(4));
  load(true);
  return startPolling(load, config.pollIntervalMs);
}
