import { h, mount, debounce } from "../../core/dom.js";
import { confetti } from "../../core/fx.js";
import { formatMoney, imageUrl, TIPO_LABEL, METODO_PAGO_LABEL } from "../../core/format.js";
import { toast, withButton } from "../../core/ui.js";
import { ApiError } from "../../core/api.js";
import { menuService, mesaService, pedidoService, unwrapList } from "../services.js";
import { pageHeader, errorState, emptyState } from "../components/common.js";
import { NUEVO_KEY } from "./mesero-mesas.js";

const TIPOS = ["SALON", "PARA_LLEVAR", "DOMICILIO"];
const METODOS = ["EFECTIVO_CONTRAENTREGA", "PAGO_VIRTUAL"];

export function renderNuevoPedido(container) {
  const state = {
    tipo: "SALON",
    mesaId: "",
    metodoPago: "EFECTIVO_CONTRAENTREGA",
    categoriaId: "",
    busqueda: "",
    categorias: [],
    productos: [],
    mesas: [],
    carrito: new Map(),
    entrega: { nombreCliente: "", telefonoCliente: "", direccion: "", barrio: "", referencias: "" },
    error: ""
  };

  const menuBox = h("div", { class: "product-grid" });
  const cartBox = h("aside", { class: "card cart" });
  const chipsBox = h("div", { class: "chips" });
  const typeBox = h("div", { class: "chips" });
  const extraBox = h("div", { class: "order-extra" });

  async function loadMenu() {
    mount(menuBox, h("div", { class: "skeleton" }), h("div", { class: "skeleton" }));
    try {
      const page = await menuService.productos({
        categoriaId: state.categoriaId,
        busqueda: state.busqueda
      });
      state.productos = unwrapList(page).filter((p) => p.disponible !== false);
      renderMenu();
    } catch (error) {
      mount(menuBox, errorState(error, loadMenu));
    }
  }

  function renderMenu() {
    if (!state.productos.length) {
      mount(menuBox, emptyState("Nada por aquí", "No hay productos con ese filtro."));
      return;
    }
    mount(
      menuBox,
      state.productos.map((p, i) =>
        h(
          "button",
          { class: "product", type: "button", style: { "--i": i % 10 }, onClick: () => add(p) },
          h(
            "div",
            { class: "product__media" },
            p.imagenUrl ? h("img", { src: imageUrl(p.imagenUrl), alt: "", loading: "lazy" }) : h("span", { class: "product__ph", "aria-hidden": "true" }, "🍔")
          ),
          h(
            "div",
            { class: "product__body" },
            h("strong", { class: "product__name" }, p.nombre),
            p.descripcion ? h("small", {}, p.descripcion) : null,
            h("div", { class: "product__foot" }, h("span", { class: "product__price" }, formatMoney(p.precio)), h("span", { class: "product__add", "aria-hidden": "true" }, "+"))
          )
        )
      )
    );
  }

  function renderChips() {
    const all = [{ id: "", nombre: "Todo" }, ...state.categorias];
    mount(
      chipsBox,
      all.map((c) =>
        h(
          "button",
          {
            class: "chip",
            type: "button",
            "aria-pressed": String(state.categoriaId === c.id),
            onClick: () => {
              state.categoriaId = c.id;
              renderChips();
              loadMenu();
            }
          },
          c.nombre
        )
      )
    );
  }

  function renderTipo() {
    mount(
      typeBox,
      TIPOS.map((t) =>
        h(
          "button",
          {
            class: "chip",
            type: "button",
            "aria-pressed": String(state.tipo === t),
            onClick: () => {
              state.tipo = t;
              state.error = "";
              renderTipo();
              renderExtra();
              renderCart();
            }
          },
          TIPO_LABEL[t]
        )
      )
    );
  }

  function input(label, key, attrs = {}) {
    return h(
      "div",
      { class: "field" },
      h("label", {}, label),
      h("input", {
        class: "input",
        value: state.entrega[key],
        onInput: (e) => (state.entrega[key] = e.target.value),
        ...attrs
      })
    );
  }

  function renderExtra() {
    if (state.tipo === "SALON") {
      mount(
        extraBox,
        h(
          "div",
          { class: "field" },
          h("label", {}, "Mesa"),
          h(
            "select",
            { class: "select", onChange: (e) => (state.mesaId = e.target.value) },
            h("option", { value: "" }, state.mesas.length ? "Elige una mesa…" : "No hay mesas libres"),
            state.mesas.map((m) =>
              h("option", { value: m.id, selected: m.id === state.mesaId }, `Mesa ${m.numero}`)
            )
          )
        )
      );
    } else if (state.tipo === "PARA_LLEVAR") {
      mount(extraBox, h("p", { class: "hint" }, "Pedido para recoger: no requiere mesa."));
    } else {
      mount(
        extraBox,
        input("Nombre del cliente", "nombreCliente", { maxLength: 120 }),
        input("Teléfono", "telefonoCliente", { type: "tel", maxLength: 20 }),
        input("Dirección", "direccion", { maxLength: 200 }),
        h("div", { class: "form-row" }, input("Barrio", "barrio"), input("Referencias", "referencias")),
        h(
          "div",
          { class: "field" },
          h("label", {}, "Método de pago"),
          h(
            "select",
            { class: "select", onChange: (e) => (state.metodoPago = e.target.value) },
            METODOS.map((m) => h("option", { value: m, selected: m === state.metodoPago }, METODO_PAGO_LABEL[m] ?? m))
          )
        )
      );
    }
  }

  function add(producto) {
    const line = state.carrito.get(producto.id);
    if (line) line.cantidad += 1;
    else state.carrito.set(producto.id, { producto, cantidad: 1, notas: "" });
    renderCart();
  }

  const total = () =>
    [...state.carrito.values()].reduce((sum, l) => sum + Number(l.producto.precio) * l.cantidad, 0);

  function renderCart() {
    const lines = [...state.carrito.values()];
    const submit = h(
      "button",
      { class: "btn btn--red btn--block", type: "button", disabled: !lines.length },
      "Enviar a cocina"
    );
    submit.addEventListener("click", () => withButton(submit, send));

    mount(
      cartBox,
      h("h2", {}, "Pedido ", h("span", { class: "hand" }, `(${lines.reduce((n, l) => n + l.cantidad, 0)})`)),
      lines.length
        ? h(
            "ul",
            { class: "cart__lines" },
            lines.map((l) =>
              h(
                "li",
                {},
                h(
                  "div",
                  { class: "cart__row" },
                  h("strong", {}, l.producto.nombre),
                  h("span", {}, formatMoney(Number(l.producto.precio) * l.cantidad))
                ),
                h(
                  "div",
                  { class: "cart__row" },
                  h(
                    "div",
                    { class: "qty" },
                    h("button", { class: "btn btn--cream btn--sm btn--icon", type: "button", "aria-label": "Quitar uno", onClick: () => change(l, -1) }, "−"),
                    h("span", {}, l.cantidad),
                    h("button", { class: "btn btn--cream btn--sm btn--icon", type: "button", "aria-label": "Agregar uno", onClick: () => change(l, 1) }, "+")
                  ),
                  h("input", {
                    class: "input input--sm",
                    placeholder: "Notas (sin cebolla…)",
                    maxLength: 200,
                    value: l.notas,
                    onInput: (e) => (l.notas = e.target.value)
                  })
                )
              )
            )
          )
        : emptyState("Vacío", "Toca un producto para agregarlo."),
      h("div", { class: "cart__total" }, h("span", {}, "Total"), h("strong", {}, formatMoney(total()))),
      state.error ? h("p", { class: "field__error", role: "alert" }, state.error) : null,
      submit
    );
  }

  function change(line, delta) {
    line.cantidad += delta;
    if (line.cantidad <= 0) state.carrito.delete(line.producto.id);
    renderCart();
  }

  async function send() {
    state.error = "";
    const body = {
      tipo: state.tipo,
      detalles: [...state.carrito.values()].map((l) => ({
        productoId: l.producto.id,
        cantidad: l.cantidad,
        notas: l.notas.trim() || null
      }))
    };
    if (state.tipo === "SALON") {
      if (!state.mesaId) {
        state.error = "Elige una mesa para el pedido de salón.";
        return renderCart();
      }
      body.mesaId = state.mesaId;
    }
    if (state.tipo === "DOMICILIO") {
      const e = state.entrega;
      if (!e.nombreCliente.trim() || !e.telefonoCliente.trim() || !e.direccion.trim()) {
        state.error = "Nombre, teléfono y dirección son obligatorios para domicilios.";
        return renderCart();
      }
      body.metodoPago = state.metodoPago;
      body.datosEntrega = {
        nombreCliente: e.nombreCliente.trim(),
        telefonoCliente: e.telefonoCliente.trim(),
        direccion: e.direccion.trim(),
        barrio: e.barrio.trim() || null,
        referencias: e.referencias.trim() || null
      };
    }
    try {
      await pedidoService.crear(body);
      toast("¡Pedido enviado a cocina!", "ok");
      confetti();
      state.carrito.clear();
      state.mesaId = "";
      state.mesas = await mesaService.disponibles().catch(() => state.mesas);
      renderExtra();
      renderCart();
    } catch (error) {
      state.error = error instanceof ApiError && error.fieldErrors
        ? Object.values(error.fieldErrors).flat().join(" ")
        : error.message;
      renderCart();
    }
  }

  async function init() {
    try {
      const [categorias, mesas] = await Promise.all([menuService.categorias(), mesaService.disponibles()]);
      state.categorias = categorias;
      state.mesas = mesas;
      const intencion = JSON.parse(sessionStorage.getItem(NUEVO_KEY) ?? "null");
      sessionStorage.removeItem(NUEVO_KEY);
      if (intencion?.tipo) state.tipo = intencion.tipo;
      if (intencion?.mesaId && mesas.some((m) => m.id === intencion.mesaId)) state.mesaId = intencion.mesaId;
    } catch (error) {
      mount(container, errorState(error, () => renderNuevoPedido(container)));
      return;
    }
    renderChips();
    renderTipo();
    renderExtra();
    renderCart();
    loadMenu();
  }

  mount(
    container,
    pageHeader("Nuevo pedido", "Toma el pedido y mándalo directo a cocina."),
    h(
      "div",
      { class: "order-layout" },
      h(
        "section",
        {},
        h("div", { class: "card order-config" }, h("h2", {}, "Tipo de pedido"), typeBox, extraBox),
        h(
          "div",
          { class: "toolbar" },
          chipsBox,
          h("input", {
            class: "input",
            type: "search",
            placeholder: "Buscar producto…",
            "aria-label": "Buscar producto",
            onInput: debounce((e) => {
              state.busqueda = e.target.value.trim();
              loadMenu();
            }, 300)
          })
        ),
        menuBox
      ),
      cartBox
    )
  );
  init();
}
