import { h, mount } from "../../core/dom.js";
import { formatMoney, formatDateTime, durationLabel, TIPO_LABEL, ESTADO_LABEL, todayIso, daysAgoIso } from "../../core/format.js";
import { openModal } from "../../core/ui.js";
import { adminService } from "../services.js";
import { pageHeader, statusBadge, shortId, emptyState, errorState, loadingBlock, paginationBar } from "../components/common.js";

function stat(label, value, accent = "lime") {
  return h("div", { class: `stat-card stat-card--${accent}` }, h("span", {}, label), h("strong", {}, value));
}

export function renderReportes(container) {
  const range = { fechaDesde: daysAgoIso(30), fechaHasta: todayIso() };
  const out = h("div", {});

  async function load() {
    mount(out, loadingBlock(4));
    try {
      const [ventas, top, cancel, tiempos] = await Promise.all([
        adminService.reporteVentas(range),
        adminService.reporteProductos({ ...range, limite: 5 }),
        adminService.reporteCancelaciones(range),
        adminService.reporteTiempos(range)
      ]);
      const max = Math.max(1, ...top.map((p) => p.unidadesVendidas));
      mount(
        out,
        h(
          "div",
          { class: "stats-grid" },
          stat("Ventas totales", formatMoney(ventas.ventasTotales), "lime"),
          stat("Pedidos entregados", ventas.pedidosEntregados, "cream"),
          stat("Ticket promedio", formatMoney(ventas.ticketPromedio), "pink"),
          stat("Tiempo promedio de preparación", tiempos.pedidosAnalizados ? durationLabel(tiempos.tiempoPromedioSegundos) : "—", "yellow"),
          stat("Pedidos cancelados", cancel.pedidosCancelados, "red"),
          stat("Valor cancelado", formatMoney(cancel.valorTotalCancelado), "red")
        ),
        h(
          "section",
          { class: "card" },
          h("h2", {}, "Lo más vendido"),
          top.length
            ? h(
                "ol",
                { class: "bars" },
                top.map((p) =>
                  h(
                    "li",
                    {},
                    h("div", { class: "bars__label" }, h("strong", {}, p.nombreProducto), h("span", {}, `${p.unidadesVendidas} uds · ${formatMoney(p.ingresosGenerados)}`)),
                    h("div", { class: "bars__track" }, h("div", { class: "bars__fill", style: { width: `${(p.unidadesVendidas / max) * 100}%` } }))
                  )
                )
              )
            : emptyState("Sin ventas", "No hay pedidos entregados en este rango.")
        )
      );
    } catch (error) {
      mount(out, errorState(error, load));
    }
  }

  const desde = h("input", { class: "input", type: "date", value: range.fechaDesde, max: todayIso(), "aria-label": "Desde" });
  const hasta = h("input", { class: "input", type: "date", value: range.fechaHasta, max: todayIso(), "aria-label": "Hasta" });
  const apply = () => {
    if (desde.value && hasta.value && desde.value <= hasta.value) {
      range.fechaDesde = desde.value;
      range.fechaHasta = hasta.value;
      load();
    }
  };
  desde.addEventListener("change", apply);
  hasta.addEventListener("change", apply);

  mount(container, pageHeader("Reportes", "Ventas y tiempos del restaurante.", h("div", { class: "toolbar" }, desde, hasta)), out);
  load();
}

export function renderPedidosAdmin(container) {
  const filtros = { estado: "", tipo: "", fechaDesde: "", fechaHasta: "", pagina: 0 };
  const out = h("div", {});

  async function load() {
    mount(out, loadingBlock(1));
    try {
      const page = await adminService.pedidos({ ...filtros, tamano: 15 });
      mount(
        out,
        page.contenido.length
          ? h(
              "div",
              { class: "table-wrap" },
              h(
                "table",
                { class: "table" },
                h("thead", {}, h("tr", {}, ["Pedido", "Tipo", "Mesa", "Mesero", "Estado", "Total", "Creado", ""].map((t) => h("th", {}, t)))),
                h(
                  "tbody",
                  {},
                  page.contenido.map((p) =>
                    h(
                      "tr",
                      {},
                      h("td", {}, shortId(p.id)),
                      h("td", {}, TIPO_LABEL[p.tipo]),
                      h("td", {}, p.numeroMesa ?? "—"),
                      h("td", {}, p.nombreMesero ?? "—"),
                      h("td", {}, statusBadge(p.estado)),
                      h("td", {}, formatMoney(p.total)),
                      h("td", {}, formatDateTime(p.fechaCreacion)),
                      h("td", {}, h("button", { class: "btn btn--cream btn--sm", type: "button", onClick: () => detalle(p.id) }, "Ver"))
                    )
                  )
                )
              )
            )
          : emptyState("Sin resultados", "Prueba con otros filtros."),
        paginationBar(page, (n) => { filtros.pagina = n; load(); })
      );
    } catch (error) {
      mount(out, errorState(error, load));
    }
  }

  async function detalle(id) {
    const body = h("div", {}, loadingBlock(1));
    openModal({ title: `Pedido ${shortId(id)}`, content: body, wide: true });
    try {
      const p = await adminService.pedido(id);
      mount(
        body,
        h("div", { class: "order-card__top" }, statusBadge(p.estado), h("span", {}, TIPO_LABEL[p.tipo])),
        h("ul", { class: "cart__lines" }, (p.detalles ?? []).map((d) =>
          h("li", {}, h("div", { class: "cart__row" }, h("strong", {}, `${d.cantidad} × ${d.nombreProducto}`), h("span", {}, formatMoney(Number(d.precioUnitario) * d.cantidad))), d.notas ? h("small", {}, `Nota: ${d.notas}`) : null)
        )),
        h("div", { class: "cart__total" }, h("span", {}, "Total"), h("strong", {}, formatMoney(p.total))),
        p.historial?.length
          ? h("ol", { class: "timeline" }, p.historial.map((e) => h("li", {}, statusBadge(e.estado), h("span", {}, `${formatDateTime(e.fecha)} · ${e.usuarioNombre ?? "Sistema"}`))))
          : null
      );
    } catch (error) {
      mount(body, errorState(error));
    }
  }

  const select = (name, label, options) =>
    h("select", { class: "select", "aria-label": label, onChange: (e) => { filtros[name] = e.target.value; filtros.pagina = 0; load(); } },
      h("option", { value: "" }, label), options.map(([v, l]) => h("option", { value: v }, l)));
  const date = (name, label) =>
    h("input", { class: "input", type: "date", "aria-label": label, title: label, onChange: (e) => { filtros[name] = e.target.value; filtros.pagina = 0; load(); } });

  mount(
    container,
    pageHeader("Pedidos", "Consulta y filtra todos los pedidos."),
    h("div", { class: "toolbar" },
      select("estado", "Todos los estados", Object.entries(ESTADO_LABEL)),
      select("tipo", "Todos los tipos", Object.entries(TIPO_LABEL)),
      date("fechaDesde", "Desde"),
      date("fechaHasta", "Hasta")),
    out
  );
  load();
}
