import { api } from "../core/api.js";
import { h, mount, debounce } from "../core/dom.js";
import { formatMoney, imageUrl } from "../core/format.js";


/** Datos de muestra: solo se usan si la API no responde o aún no hay productos. */
const FALLBACK = {
  categorias: [
    { id: "f-1", nombre: "Hamburguesas" },
    { id: "f-2", nombre: "Acompañantes" },
    { id: "f-3", nombre: "Bebidas" }
  ],
  productos: [
    { id: "p-1", nombre: "La Clásica del Despelote", descripcion: "Carne smash, queso, tomate y salsa de la casa.", precio: 24000, categoria: { id: "f-1", nombre: "Hamburguesas" } },
    { id: "p-2", nombre: "Furia Verde", descripcion: "Doble carne, pepinillos, lechuga crujiente y alioli.", precio: 28000, categoria: { id: "f-1", nombre: "Hamburguesas" } },
    { id: "p-3", nombre: "Queso Fundido", descripcion: "Triple queso derretido y cebolla caramelizada.", precio: 26000, categoria: { id: "f-1", nombre: "Hamburguesas" } },
    { id: "p-4", nombre: "Papas Caóticas", descripcion: "Crocantes, con salsa y mucha actitud.", precio: 12000, categoria: { id: "f-2", nombre: "Acompañantes" } },
    { id: "p-5", nombre: "Limonada Rebelde", descripcion: "Bien fría, bien ácida, bien nuestra.", precio: 8000, categoria: { id: "f-3", nombre: "Bebidas" } }
  ]
};

export async function initMenu() {
  const grid = document.querySelector("[data-menu-grid]");
  const chipsRoot = document.querySelector("[data-menu-chips]");
  const search = document.querySelector("[data-menu-search]");
  const note = document.querySelector("[data-menu-note]");
  if (!grid || !chipsRoot) return;

  const state = { categorias: [], categoriaId: null, busqueda: "", demo: false };

  mount(grid, ...Array.from({ length: 3 }, () => h("div", { class: "skeleton" })));

  try {
    const categorias = await api.publicGet("/api/publico/menu/categorias");
    state.categorias = categorias.contenido;
  } catch {
    state.demo = true;
  }
  if (!state.demo && state.categorias.length === 0) state.demo = true;
  if (state.demo) state.categorias = FALLBACK.categorias;

  renderChips();
  await loadProducts();

  search?.addEventListener(
    "input",
    debounce(() => {
      state.busqueda = search.value.trim();
      loadProducts();
    }, 350)
  );

  function renderChips() {
    const options = [{ id: null, nombre: "Todo" }, ...state.categorias];
    mount(
      chipsRoot,
      options.map((option) =>
        h(
          "button",
          {
            class: "chip",
            type: "button",
            "aria-pressed": String(option.id === state.categoriaId),
            onClick: () => {
              state.categoriaId = option.id;
              renderChips();
              loadProducts();
            }
          },
          option.nombre
        )
      )
    );
  }

  async function loadProducts() {
    let productos;
    if (state.demo) {
      productos = FALLBACK.productos.filter(
        (p) =>
          (!state.categoriaId || p.categoria.id === state.categoriaId) &&
          p.nombre.toLowerCase().includes(state.busqueda.toLowerCase())
      );
    } else {
      try {
        const page = await api.publicGet("/api/publico/menu/productos", {
          categoriaId: state.categoriaId,
          busqueda: state.busqueda,
          tamano: 50
        });
        productos = page.contenido;
      } catch {
        productos = [];
      }
    }
    if (note) note.hidden = !state.demo;
    renderProducts(productos);
  }

  function renderProducts(productos) {
    if (productos.length === 0) {
      mount(grid, h("div", { class: "empty", style: { gridColumn: "1 / -1" } }, h("span", { class: "hand" }, "Nada por aquí…"), "Prueba con otra categoría o búsqueda."));
      return;
    }
    mount(
      grid,
      productos.map((producto, i) => {
        return h(
          "article",
          { class: "dish", "data-reveal": "up", style: { "--i": i % 6 } },
          producto.imagenUrl
            ? h("div", { class: "dish__art dish__art--photo" }, h("img", { src: imageUrl(producto.imagenUrl), alt: producto.nombre, loading: "lazy" }))
            : h("div", { class: "dish__art dish__art--photo dish__art--empty" }, h("span", {}, "🍔")),
          h(
            "div",
            { class: "dish__info" },
            h("span", { class: "dish__tag" }, producto.categoria.nombre),
            h("h3", {}, producto.nombre),
            h("p", {}, producto.descripcion ?? ""),
            h("strong", { class: "dish__price" }, formatMoney(producto.precio))
          )
        );
      })
    );
    window.dispatchEvent(new CustomEvent("poli:content-added"));
  }
}
