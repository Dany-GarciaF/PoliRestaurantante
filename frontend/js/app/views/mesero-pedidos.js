import { h, mount } from "../../core/dom.js";
import { formatMoney, formatTime, formatDateTime, elapsedLabel, TIPO_LABEL } from "../../core/format.js";
import { toast, openModal, confirmDialog, withButton } from "../../core/ui.js";
import { config } from "../../config.js";
import { menuService, pedidoService, unwrapList } from "../services.js";
import { pageHeader, statusBadge, shortId, emptyState, errorState, loadingBlock, startPolling } from "../components/common.js";

const FILTROS = [
  ["ACTIVOS", "Activos"],
  ["LISTO", "Listos"],
  ["ENTREGADO", "Entregados"],
  ["CANCELADO", "Cancelados"]
];

export function renderMisPedidos(container) {
  const state = { filtro: "ACTIVOS", pedidos: [], conocidosListos: null };
  const list = h("div", { class: "grid-cards" });
  const chips = h("div", { class: "chips" });

  const visibles = () =>
    state.pedidos.filter((p) =>
      state.filtro === "ACTIVOS" ? ["PENDIENTE", "EN_PREPARACION", "LISTO"].includes(p.estado) : p.estado === state.filtro
    );

  function renderChips() {
    mount(
      chips,
      FILTROS.map(([value, label]) =>
        h("button", { class: "chip", type: "button", "aria-pressed": String(state.filtro === value), onClick: () => { state.filtro = value; renderChips(); renderList(); } }, label)
      )
    );
  }

  function renderList() {
    const items = visibles();
    if (!items.length) return mount(list, emptyState("Sin pedidos", "No hay pedidos en esta vista."));
    mount(
      list,
      items.map((p, i) =>
        h(
          "article",
          { class: `card order-card order-card--${p.estado}`, style: { "--i": i % 8 } },
          h("div", { class: "order-card__top" }, h("strong", {}, shortId(p.id)), statusBadge(p.estado)),
          h("p", { class: "order-card__meta" }, p.tipo === "SALON" ? `Mesa ${p.numeroMesa ?? "—"}` : TIPO_LABEL[p.tipo], " · ", formatTime(p.fechaCreacion), " · ", elapsedLabel(p.fechaCreacion)),
          h("p", {}, `${p.detalles.length} producto(s) · `, h("strong", {}, formatMoney(p.total))),
          h(
            "div",
            { class: "order-card__actions" },
            h("button", { class: "btn btn--cream btn--sm", type: "button", onClick: () => openDetail(p.id) }, "Ver / editar"),
            p.estado === "LISTO"
              ? h("button", { class: "btn btn--lime btn--sm", type: "button", onClick: (e) => cambiar(e.currentTarget, p.id, "ENTREGADO", "¡Pedido entregado!") }, "Marcar entregado")
              : null
          )
        )
      )
    );
  }

  async function load(initial = false) {
    try {
      const pedidos = await pedidoService.misPedidos();
      pedidos.sort((a, b) => new Date(b.fechaCreacion) - new Date(a.fechaCreacion));
      const listos = new Set(pedidos.filter((p) => p.estado === "LISTO").map((p) => p.id));
      if (state.conocidosListos) {
        for (const id of listos) {
          if (!state.conocidosListos.has(id)) toast(`¡Pedido ${shortId(id)} listo para recoger!`, "ok");
        }
      }
      state.conocidosListos = listos;
      state.pedidos = pedidos;
      renderList();
    } catch (error) {
      if (initial) mount(list, errorState(error, () => load(true)));
    }
  }

  async function cambiar(button, id, estado, mensaje) {
    try {
      await withButton(button, () => pedidoService.cambiarEstado(id, estado));
      toast(mensaje, "ok");
      load();
    } catch (error) {
      toast(error.message, "error");
    }
  }

  async function openDetail(id) {
    const body = h("div", {}, loadingBlock(1));
    const modal = openModal({ title: `Pedido ${shortId(id)}`, content: body, wide: true, onClose: () => load() });
    let productos = null;

    async function refresh() {
      try {
        const [pedido, historial] = await Promise.all([pedidoService.obtener(id), pedidoService.historial(id)]);
        paint(pedido, historial);
      } catch (error) {
        mount(body, errorState(error, refresh));
      }
    }

    async function run(action, okMessage) {
      try {
        await action();
        if (okMessage) toast(okMessage, "ok");
        await refresh();
      } catch (error) {
        toast(error.message, "error");
      }
    }

    function paint(pedido, historial) {
      const editable = pedido.estado === "PENDIENTE";
      const cancelable = ["PENDIENTE", "EN_PREPARACION"].includes(pedido.estado);
      mount(
        body,
        h("div", { class: "order-card__top" }, statusBadge(pedido.estado), h("span", {}, `${TIPO_LABEL[pedido.tipo]}${pedido.numeroMesa ? ` · Mesa ${pedido.numeroMesa}` : ""}`)),
        h(
          "ul",
          { class: "cart__lines" },
          pedido.detalles.map((d) =>
            h(
              "li",
              {},
              h("div", { class: "cart__row" }, h("strong", {}, `${d.cantidad} × ${d.nombreProducto}`), h("span", {}, formatMoney(Number(d.precioUnitario) * d.cantidad))),
              d.notas ? h("small", {}, `Nota: ${d.notas}`) : null,
              editable
                ? h(
                    "div",
                    { class: "order-card__actions" },
                    h("button", { class: "btn btn--cream btn--sm", type: "button", onClick: () => run(() => pedidoService.actualizarDetalle(id, d.id, { cantidad: d.cantidad + 1, notas: d.notas }), null) }, "+1"),
                    d.cantidad > 1
                      ? h("button", { class: "btn btn--cream btn--sm", type: "button", onClick: () => run(() => pedidoService.actualizarDetalle(id, d.id, { cantidad: d.cantidad - 1, notas: d.notas }), null) }, "−1")
                      : null,
                    h("button", { class: "btn btn--red btn--sm", type: "button", onClick: () => run(() => pedidoService.eliminarDetalle(id, d.id), "Producto quitado") }, "Quitar")
                  )
                : null
            )
          )
        ),
        h("div", { class: "cart__total" }, h("span", {}, "Total"), h("strong", {}, formatMoney(pedido.total))),
        editable ? addProductBox() : null,
        h("h3", {}, "Historial"),
        h(
          "ol",
          { class: "timeline" },
          historial.map((e) => h("li", {}, statusBadge(e.estado), h("span", {}, `${formatDateTime(e.fecha)} · ${e.usuarioNombre ?? "Sistema"}`)))
        ),
        h(
          "div",
          { class: "modal__foot", style: { padding: "1rem 0 0" } },
          cancelable
            ? h("button", { class: "btn btn--red", type: "button", onClick: async () => {
                const ok = await confirmDialog({ title: "Cancelar pedido", message: "Esta acción no se puede deshacer.", confirmLabel: "Cancelar pedido", danger: true });
                if (ok) { await run(() => pedidoService.cambiarEstado(id, "CANCELADO"), "Pedido cancelado"); }
              } }, "Cancelar pedido")
            : null,
          pedido.estado === "LISTO"
            ? h("button", { class: "btn btn--lime", type: "button", onClick: () => run(() => pedidoService.cambiarEstado(id, "ENTREGADO"), "¡Entregado!") }, "Marcar entregado")
            : null
        )
      );
    }

    function addProductBox() {
      const select = h("select", { class: "select", "aria-label": "Producto a agregar" }, h("option", { value: "" }, "Cargando…"));
      const qty = h("input", { class: "input input--sm", type: "number", min: 1, max: 50, value: 1, "aria-label": "Cantidad" });
      (async () => {
        productos ??= unwrapList(await menuService.productos()).filter((p) => p.disponible !== false);
        mount(select, h("option", { value: "" }, "Agregar producto…"), productos.map((p) => h("option", { value: p.id }, `${p.nombre} · ${formatMoney(p.precio)}`)));
      })().catch(() => {});
      return h(
        "div",
        { class: "add-product" },
        select,
        qty,
        h("button", { class: "btn btn--lime btn--sm", type: "button", onClick: () => select.value && run(() => pedidoService.agregarDetalle(id, { productoId: select.value, cantidad: Number(qty.value) || 1 }), "Producto agregado") }, "Agregar")
      );
    }

    refresh();
    return modal;
  }

  mount(container, pageHeader("Mis pedidos", "Sigue el estado de tus pedidos en vivo."), chips, list);
  renderChips();
  mount(list, loadingBlock());
  load(true);
  return startPolling(load, config.pollIntervalMs);
}
