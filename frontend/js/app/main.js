import { h, mount } from "../core/dom.js";
import { ROL_LABEL } from "../core/format.js";
import { session } from "../core/session.js";
import { setUnauthorizedHandler } from "../core/api.js";
import { toast } from "../core/ui.js";
import { renderLogin } from "./views/login.js";
import { renderNuevoPedido } from "./views/mesero-nuevo.js";
import { renderMisPedidos } from "./views/mesero-pedidos.js";
import { renderMapaMesas } from "./views/mesero-mesas.js";
import { renderCocina } from "./views/cocina.js";
import { renderReportes, renderPedidosAdmin } from "./views/admin-reportes.js";
import { renderCategorias, renderProductos, renderMesas, renderUsuarios, renderConfiguracion } from "./views/admin-gestion.js";

/** Rutas por rol: la primera es la pantalla de inicio del rol. */
const ROUTES = {
  MESERO: [
    { path: "mesas", label: "Mesas", icon: "🪑", view: renderMapaMesas },
    { path: "nuevo", label: "Nuevo pedido", icon: "➕", view: renderNuevoPedido },
    { path: "pedidos", label: "Mis pedidos", icon: "🧾", view: renderMisPedidos }
  ],
  COCINA: [{ path: "cola", label: "Cola", icon: "🔥", view: renderCocina }],
  ADMINISTRADOR: [
    { path: "reportes", label: "Reportes", icon: "📊", view: renderReportes },
    { path: "pedidos", label: "Pedidos", icon: "🧾", view: renderPedidosAdmin },
    { path: "productos", label: "Productos", icon: "🍔", view: renderProductos },
    { path: "categorias", label: "Categorías", icon: "🏷️", view: renderCategorias },
    { path: "mesas", label: "Mesas", icon: "🪑", view: renderMesas },
    { path: "usuarios", label: "Usuarios", icon: "👥", view: renderUsuarios },
    { path: "config", label: "Domicilios", icon: "🛵", view: renderConfiguracion }
  ]
};

const root = document.getElementById("app");
let cleanup = null;
let current = null;

function logout(message) {
  cleanup?.();
  cleanup = null;
  session.clear();
  current = null;
  location.hash = "";
  if (message) toast(message, "info");
  boot();
}

function shell(user, routes) {
  const content = h("main", { class: "app__content", id: "contenido", tabindex: "-1" });
  const links = routes.map((r) =>
    h("a", { class: "nav-link", href: `#/${r.path}`, dataset: { path: r.path } }, h("span", { "aria-hidden": "true" }, r.icon), h("span", {}, r.label))
  );
  mount(
    root,
    h(
      "div",
      { class: "app" },
      h(
        "aside",
        { class: "app__side" },
        h("a", { class: "app__brand", href: "index.html" }, "POLI", h("span", {}, "RESTAURANTE")),
        h("nav", { class: "app__nav", "aria-label": "Principal" }, links)
      ),
      h(
        "div",
        { class: "app__main" },
        h(
          "header",
          { class: "app__top" },
          h("div", { class: "app__user" }, h("span", { class: "badge badge--LISTO" }, ROL_LABEL[user.rol] ?? user.rol), h("span", {}, user.correo)),
          h("button", { class: "btn btn--cream btn--sm", type: "button", onClick: () => logout() }, "Salir")
        ),
        content
      )
    )
  );
  return { content, links };
}

function start(user) {
  current = user;
  const routes = ROUTES[user.rol];
  if (!routes) return logout("Rol no soportado.");
  const { content, links } = shell(user, routes);

  const navigate = () => {
    const path = location.hash.replace(/^#\//, "");
    const route = routes.find((r) => r.path === path) ?? routes[0];
    if (path !== route.path) return void (location.hash = `#/${route.path}`);
    links.forEach((l) => l.classList.toggle("is-active", l.dataset.path === route.path));
    cleanup?.();
    cleanup = null;
    content.classList.remove("view-in");
    void content.offsetWidth;
    content.classList.add("view-in");
    cleanup = route.view(content) ?? null;
  };

  window.onhashchange = navigate;
  navigate();
}

function boot() {
  window.onhashchange = null;
  const user = session.get();
  if (user) start(user);
  else renderLogin(root, (data) => {
    location.hash = "";
    start(data);
  });
}

setUnauthorizedHandler(() => current && logout("Tu sesión expiró. Inicia sesión de nuevo."));
boot();
