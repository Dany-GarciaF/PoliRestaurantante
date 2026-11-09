import { h, mount } from "../../core/dom.js";
import { formatMoney, imageUrl, ROL_LABEL } from "../../core/format.js";
import { toast, confirmDialog, openModal } from "../../core/ui.js";
import { adminService, mesaService } from "../services.js";
import { openForm } from "../components/form.js";
import { pageHeader, emptyState, errorState, loadingBlock, paginationBar } from "../components/common.js";

const activoBadge = (on, yes = "Activo", no = "Inactivo") => h("span", { class: `badge badge--${on ? "on" : "off"}` }, on ? yes : no);
const btn = (label, onClick, cls = "btn--cream") => h("button", { class: `btn ${cls} btn--sm`, type: "button", onClick }, label);

function table(headers, rows) {
  return h("div", { class: "table-wrap" }, h("table", { class: "table" },
    h("thead", {}, h("tr", {}, headers.map((t) => h("th", {}, t)))),
    h("tbody", {}, rows)));
}

/** Plantilla común para listados CRUD con recarga tras cada acción. */
function crudView(container, { title, subtitle, createLabel, load, render, onCreate }) {
  const out = h("div", {});
  async function refresh() {
    mount(out, loadingBlock(1));
    try {
      mount(out, await render(await load(), refresh));
    } catch (error) {
      mount(out, errorState(error, refresh));
    }
  }
  mount(container, pageHeader(title, subtitle, h("button", { class: "btn btn--red", type: "button", onClick: () => onCreate(refresh) }, createLabel)), out);
  refresh();
}

async function desactivar(refresh, message, action) {
  if (!(await confirmDialog({ title: "Desactivar", message, confirmLabel: "Desactivar", danger: true }))) return;
  try {
    await action();
    toast("Desactivado correctamente", "ok");
    refresh();
  } catch (error) {
    toast(error.message, "error");
  }
}

function adminGrid(cards) {
  return h("div", { class: "admin-grid" }, cards);
}

const stat = (value, label) => h("div", { class: "admin-stat" }, h("strong", {}, value), h("span", {}, label));

export function renderCategorias(container) {
  const fields = [
    { name: "nombre", label: "Nombre", required: true, attrs: { maxLength: 100 } },
    { name: "descripcion", label: "Descripción", type: "textarea", attrs: { maxLength: 300 } },
    { name: "activa", label: "Activa", type: "checkbox", default: true }
  ];
  const form = (title, initial, onSubmit, refresh) => openForm({ title, fields, initial, onSubmit: async (v) => { await onSubmit(v); toast("Guardado", "ok"); refresh(); } });
  crudView(container, {
    title: "Categorías", subtitle: "Organiza el menú.", createLabel: "+ Nueva categoría",
    load: adminService.categorias,
    onCreate: (refresh) => form("Nueva categoría", {}, adminService.crearCategoria, refresh),
    render: (items, refresh) => items.length ? h("div", {},
      h("div", { class: "admin-stats" }, stat(items.length, "Categorías"), stat(items.filter((c) => c.activa).length, "Activas")),
      adminGrid(items.map((c, i) => h("article", { class: `admin-card${c.activa ? "" : " is-off"}`, style: { "--i": i % 12 } },
        h("div", { class: "admin-card__body" },
          h("div", {}, activoBadge(c.activa)),
          h("h3", { class: "admin-card__title" }, c.nombre),
          h("p", { class: "admin-card__desc" }, c.descripcion || "Sin descripción")),
        h("div", { class: "admin-card__actions" },
          btn("Editar", () => form("Editar categoría", c, (v) => adminService.actualizarCategoria(c.id, v), refresh)),
          c.activa ? btn("Desactivar", () => desactivar(refresh, `¿Desactivar "${c.nombre}"?`, () => adminService.desactivarCategoria(c.id)), "btn--red") : null))))
    ) : emptyState("Sin categorías", "Crea la primera.")
  });
}

function openImageUploader(producto, refresh) {
  let file = null;
  const preview = h("img", { class: "upload__preview", alt: "Vista previa", hidden: !producto.imagenUrl, src: imageUrl(producto.imagenUrl) ?? "" });
  const input = h("input", { type: "file", accept: "image/jpeg,image/png,image/webp", id: "img-file" });
  const drop = h("label", { class: "upload__drop", for: "img-file" }, h("strong", {}, "Arrastra una imagen o haz clic"), h("small", {}, "JPG, PNG o WEBP · máx. 3 MB"));
  const save = h("button", { class: "btn btn--red", type: "button", disabled: true }, "Subir imagen");
  const modal = openModal({
    title: `Imagen · ${producto.nombre}`,
    content: h("div", { class: "upload" }, preview, drop, input),
    footer: [
      producto.imagenUrl ? h("button", { class: "btn btn--cream", type: "button", onClick: async () => {
        try { await adminService.eliminarImagenProducto(producto.id); toast("Imagen eliminada", "ok"); modal.close(); refresh(); } catch (e) { toast(e.message, "error"); }
      } }, "Quitar imagen") : null,
      save
    ]
  });

  function pick(selected) {
    if (!selected) return;
    if (!["image/jpeg", "image/png", "image/webp"].includes(selected.type)) return toast("Formato no permitido.", "error");
    if (selected.size > 3 * 1024 * 1024) return toast("La imagen supera 3 MB.", "error");
    file = selected;
    preview.src = URL.createObjectURL(selected);
    preview.hidden = false;
    save.disabled = false;
  }
  input.addEventListener("change", () => pick(input.files[0]));
  ["dragover", "dragenter"].forEach((ev) => drop.addEventListener(ev, (e) => { e.preventDefault(); drop.classList.add("is-over"); }));
  ["dragleave", "drop"].forEach((ev) => drop.addEventListener(ev, () => drop.classList.remove("is-over")));
  drop.addEventListener("drop", (e) => { e.preventDefault(); pick(e.dataTransfer.files[0]); });
  save.addEventListener("click", async () => {
    save.disabled = true;
    try {
      await adminService.subirImagenProducto(producto.id, file);
      toast("Imagen actualizada", "ok");
      modal.close();
      refresh();
    } catch (e) {
      toast(e.message, "error");
      save.disabled = false;
    }
  });
}

export function renderProductos(container) {
  let categorias = [];
  const vista = { modo: "carrusel", index: 0 };
  const fields = () => [
    { name: "nombre", label: "Nombre", required: true, attrs: { maxLength: 150 } },
    { name: "descripcion", label: "Descripción", type: "textarea", attrs: { maxLength: 500 } },
    { name: "precio", label: "Precio (COP)", type: "number", required: true, attrs: { min: 0, step: 100 } },
    { name: "categoriaId", label: "Categoría", type: "select", required: true, options: categorias.filter((c) => c.activa).map((c) => ({ value: c.id, label: c.nombre })) },
    { name: "disponible", label: "Disponible", type: "checkbox", default: true }
  ];
  const form = (title, initial, onSubmit, refresh) => openForm({ title, fields: fields(), initial, onSubmit: async (v) => { await onSubmit(v); toast("Guardado", "ok"); refresh(); } });
  crudView(container, {
    title: "Productos", subtitle: "Precios y disponibilidad.", createLabel: "+ Nuevo producto",
    load: async () => { const [p, c] = await Promise.all([adminService.productos(), adminService.categorias()]); categorias = c; return p; },
    onCreate: (refresh) => categorias.length ? form("Nuevo producto", {}, adminService.crearProducto, refresh) : toast("Crea primero una categoría.", "info"),
    render: (items, refresh) => {
      if (!items.length) return emptyState("Sin productos", "Agrega el primero.");
      const filtro = { q: "", cat: "" };
      const chips = h("div", { class: "chips" });
      const edit = (p) => form("Editar producto", { ...p, categoriaId: p.categoria?.id }, (v) => adminService.actualizarProducto(p.id, v), refresh);
      const card = (p, i) => h("article", { class: `admin-card${p.disponible ? "" : " is-off"}`, style: { "--i": i % 12 } },
        h("div", { class: "admin-card__media" },
          p.imagenUrl ? h("img", { src: imageUrl(p.imagenUrl), alt: "", loading: "lazy" }) : h("span", { class: "admin-card__ph", "aria-hidden": "true" }, "🍔"),
          h("span", { class: "admin-card__tag" }, p.categoria?.nombre ?? "Sin categoría"),
          h("span", { class: "admin-card__status" }, activoBadge(p.disponible, "Disponible", "No disponible"))),
        h("div", { class: "admin-card__body" },
          h("h3", { class: "admin-card__title" }, p.nombre),
          h("p", { class: "admin-card__desc" }, p.descripcion || "Sin descripción"),
          h("span", { class: "admin-card__price" }, formatMoney(p.precio))),
        h("div", { class: "admin-card__actions" },
          btn("Imagen", () => openImageUploader(p, refresh), "btn--lime"),
          btn("Editar", () => edit(p)),
          p.disponible ? btn("Desactivar", () => desactivar(refresh, `¿Marcar "${p.nombre}" como no disponible?`, () => adminService.desactivarProducto(p.id))) : null));
      const pad = (n) => String(n).padStart(2, "0");
      const stage = h("div", { class: "showcase__track" });
      const film = h("div", { class: "filmstrip", role: "tablist" });
      const counter = h("span", { class: "showcase__count" });
      let list = [];

      const slide = (p, i) => h("article", { class: `slide${p.disponible ? "" : " is-off"}`, role: "group", "aria-label": `${i + 1} de ${list.length}`, onClick: (e) => { if (i !== vista.index && !e.target.closest("button")) goTo(i); } },
        h("div", { class: "slide__media" },
          p.imagenUrl ? h("img", { src: imageUrl(p.imagenUrl), alt: p.nombre, loading: "lazy" }) : h("span", { class: "slide__ph", "aria-hidden": "true" }, "🍔"),
          h("span", { class: "slide__index" }, `${pad(i + 1)}`)),
        h("div", { class: "slide__info" },
          h("span", { class: "admin-card__tag slide__tag" }, p.categoria?.nombre ?? "Sin categoría"),
          h("h2", { class: "slide__title" }, p.nombre),
          h("p", { class: "slide__desc" }, p.descripcion || "Sin descripción"),
          h("div", { class: "slide__meta" }, h("span", { class: "slide__price" }, formatMoney(p.precio)), activoBadge(p.disponible, "Disponible", "No disponible")),
          h("div", { class: "slide__actions" },
            btn("Cambiar imagen", () => openImageUploader(p, refresh), "btn--lime"),
            btn("Editar", () => edit(p)),
            p.disponible ? btn("Desactivar", () => desactivar(refresh, `¿Marcar "${p.nombre}" como no disponible?`, () => adminService.desactivarProducto(p.id))) : null)));

      function goTo(n, smooth = true) {
        if (!list.length) return;
        vista.index = (n + list.length) % list.length;
        stage.style.transform = `translateX(calc(var(--off) - ${vista.index} * var(--w)))`;
        [...stage.children].forEach((s, i) => s.classList.toggle("is-active", i === vista.index));
        [...film.children].forEach((b, i) => {
          b.classList.toggle("is-active", i === vista.index);
          b.setAttribute("aria-selected", String(i === vista.index));
          if (i === vista.index) film.scrollTo({ left: b.offsetLeft - film.clientWidth / 2 + b.clientWidth / 2, behavior: smooth ? "smooth" : "auto" });
        });
        counter.textContent = `${pad(vista.index + 1)} / ${pad(list.length)}`;
      }

      const showcase = h("section", { class: "showcase", tabindex: "0", "aria-label": "Carrusel de productos" },
        h("div", { class: "showcase__viewport" }, stage),
        h("div", { class: "showcase__bar" },
          h("button", { class: "showcase__nav", type: "button", "aria-label": "Anterior", onClick: () => goTo(vista.index - 1) }, "‹"),
          counter,
          h("button", { class: "showcase__nav", type: "button", "aria-label": "Siguiente", onClick: () => goTo(vista.index + 1) }, "›")),
        film);
      showcase.addEventListener("keydown", (e) => {
        if (e.key === "ArrowRight") goTo(vista.index + 1);
        if (e.key === "ArrowLeft") goTo(vista.index - 1);
      });
      let startX = null;
      showcase.addEventListener("pointerdown", (e) => { startX = e.target.closest("button, a, input") ? null : e.clientX; });
      showcase.addEventListener("pointerup", (e) => {
        if (startX !== null && Math.abs(e.clientX - startX) > 60) goTo(vista.index + (e.clientX < startX ? 1 : -1));
        startX = null;
      });

      const viewToggle = h("div", { class: "view-toggle" },
        ["carrusel", "cuadricula"].map((v) => h("button", {
          class: "chip", type: "button", "aria-pressed": String(vista.modo === v),
          onClick: () => { vista.modo = v; draw(); }
        }, v === "carrusel" ? "Carrusel" : "Cuadrícula")));

      function draw() {
        const q = filtro.q.toLowerCase();
        list = items.filter((p) => (!filtro.cat || p.categoria?.id === filtro.cat) && (!q || `${p.nombre} ${p.descripcion ?? ""}`.toLowerCase().includes(q)));
        mount(chips, [{ id: "", nombre: "Todas" }, ...categorias].map((c) =>
          h("button", { class: "chip", type: "button", "aria-pressed": String(filtro.cat === c.id), onClick: () => { filtro.cat = c.id; vista.index = 0; draw(); } }, c.nombre)));
        mount(viewToggle, ["carrusel", "cuadricula"].map((v) => h("button", {
          class: "chip", type: "button", "aria-pressed": String(vista.modo === v),
          onClick: () => { vista.modo = v; draw(); }
        }, v === "carrusel" ? "Carrusel" : "Cuadrícula")));
        if (!list.length) {
          mount(body, emptyState("Nada por aquí", "Ningún producto coincide con el filtro."));
        } else if (vista.modo === "cuadricula") {
          mount(body, h("div", { class: "admin-grid" }, list.map(card)));
        } else {
          mount(stage, list.map(slide));
          mount(film, list.map((p, i) => h("button", {
            class: "filmstrip__item", type: "button", role: "tab", title: p.nombre, onClick: () => goTo(i)
          }, p.imagenUrl ? h("img", { src: imageUrl(p.imagenUrl), alt: "" }) : h("span", { "aria-hidden": "true" }, "🍔"), h("small", {}, p.nombre))));
          mount(body, showcase);
          goTo(Math.min(vista.index, list.length - 1), false);
        }
      }
      const body = h("div", { class: "showcase-host" });
      draw();
      return h("div", {},
        h("div", { class: "admin-stats" }, stat(items.length, "Productos"), stat(items.filter((p) => p.disponible).length, "Disponibles"),
          stat(items.filter((p) => !p.disponible).length, "Agotados"), stat(items.filter((p) => p.imagenUrl).length, "Con foto")),
        h("div", { class: "admin-filters" },
          h("input", { class: "input", type: "search", placeholder: "Buscar producto…", "aria-label": "Buscar producto", onInput: (e) => { filtro.q = e.target.value.trim(); vista.index = 0; draw(); } }),
          chips, viewToggle),
        body);
    }
  });
}

export function renderMesas(container) {
  const fields = [
    { name: "numero", label: "Número de mesa", type: "number", required: true, attrs: { min: 1 } },
    { name: "activa", label: "Activa", type: "checkbox", default: true }
  ];
  const form = (title, initial, onSubmit, refresh) => openForm({ title, fields, initial, onSubmit: async (v) => { await onSubmit(v); toast("Guardado", "ok"); refresh(); } });
  crudView(container, {
    title: "Mesas", subtitle: "Distribución del salón.", createLabel: "+ Nueva mesa",
    load: mesaService.listar,
    onCreate: (refresh) => form("Nueva mesa", {}, mesaService.crear, refresh),
    render: (items, refresh) => items.length ? h("div", {},
      h("div", { class: "admin-stats" }, stat(items.length, "Mesas"), stat(items.filter((m) => m.activa && m.estado !== "OCUPADA").length, "Libres"), stat(items.filter((m) => m.estado === "OCUPADA").length, "Ocupadas")),
      h("div", { class: "admin-grid", style: { "grid-template-columns": "repeat(auto-fill, minmax(200px, 1fr))" } }, items.map((m, i) =>
        h("article", { class: `admin-card admin-card--mesa${m.activa ? "" : " is-off"}`, style: { "--i": i % 12 } },
          h("div", { class: "admin-card__body" },
            h("span", { class: "admin-card__desc" }, "Mesa"),
            h("span", { class: "admin-card__num" }, m.numero),
            h("div", {}, h("span", { class: `badge badge--${m.estado === "OCUPADA" ? "PENDIENTE" : "ENTREGADO"}` }, m.estado === "OCUPADA" ? "Ocupada" : "Libre"), " ", activoBadge(m.activa))),
          h("div", { class: "admin-card__actions" },
            btn("Editar", () => form("Editar mesa", m, (v) => mesaService.actualizar(m.id, v), refresh)),
            m.activa ? btn("Desactivar", () => desactivar(refresh, `¿Desactivar la mesa ${m.numero}?`, () => mesaService.desactivar(m.id)), "btn--red") : null))))
    ) : emptyState("Sin mesas", "Agrega la primera.")
  });
}


export function renderUsuarios(container) {
  const filtros = { busqueda: "", pagina: 0 };
  const roles = Object.entries(ROL_LABEL).map(([value, label]) => ({ value, label }));
  const out = h("div", {});

  const crear = () => openForm({
    title: "Nuevo usuario",
    fields: [
      { name: "nombre", label: "Nombre", required: true },
      { name: "correo", label: "Correo", type: "email", required: true },
      { name: "contrasena", label: "Contraseña", type: "password", required: true, hint: "Entre 8 y 72 caracteres.", attrs: { minLength: 8, maxLength: 72 } },
      { name: "rol", label: "Rol", type: "select", required: true, options: roles }
    ],
    onSubmit: async (v) => { await adminService.crearUsuario(v); toast("Usuario creado", "ok"); load(); }
  });

  const editar = (u) => openForm({
    title: "Editar usuario", initial: u,
    fields: [
      { name: "nombre", label: "Nombre", required: true },
      { name: "correo", label: "Correo", type: "email", required: true },
      { name: "rol", label: "Rol", type: "select", required: true, options: roles }
    ],
    onSubmit: async (v) => { await adminService.actualizarUsuario(u.id, v); toast("Guardado", "ok"); load(); }
  });

  const clave = (u) => openForm({
    title: `Nueva contraseña · ${u.nombre}`, submitLabel: "Restablecer",
    fields: [{ name: "contrasena", label: "Contraseña", type: "password", required: true, attrs: { minLength: 8, maxLength: 72 } }],
    onSubmit: async (v) => { await adminService.restablecerContrasena(u.id, v.contrasena); toast("Contraseña actualizada", "ok"); }
  });

  async function alternar(u) {
    try {
      await adminService.cambiarEstadoUsuario(u.id, !u.activo);
      toast(u.activo ? "Usuario desactivado" : "Usuario activado", "ok");
      load();
    } catch (error) {
      toast(error.message, "error");
    }
  }

  async function load() {
    mount(out, loadingBlock(1));
    try {
      const page = await adminService.usuarios({ ...filtros, tamano: 15 });
      mount(out,
        page.contenido.length ? table(["Nombre", "Correo", "Rol", "Estado", ""], page.contenido.map((u) =>
          h("tr", {}, h("td", {}, h("strong", {}, u.nombre)), h("td", {}, u.correo), h("td", {}, ROL_LABEL[u.rol] ?? u.rol), h("td", {}, activoBadge(u.activo)),
            h("td", { class: "actions" }, btn("Editar", () => editar(u)), btn("Contraseña", () => clave(u)), btn(u.activo ? "Desactivar" : "Activar", () => alternar(u), u.activo ? "btn--red" : "btn--lime"))))
        ) : emptyState("Sin usuarios", "Nada coincide con la búsqueda."),
        paginationBar(page, (n) => { filtros.pagina = n; load(); }));
    } catch (error) {
      mount(out, errorState(error, load));
    }
  }

  mount(container,
    pageHeader("Usuarios", "Meseros, cocina y administradores.", h("button", { class: "btn btn--red", type: "button", onClick: crear }, "+ Nuevo usuario")),
    h("div", { class: "toolbar" }, h("input", { class: "input", type: "search", placeholder: "Buscar por nombre o correo…", "aria-label": "Buscar usuario",
      onChange: (e) => { filtros.busqueda = e.target.value.trim(); filtros.pagina = 0; load(); } })),
    out);
  load();
}

export function renderConfiguracion(container) {
  const out = h("div", { class: "card config-card" });
  async function load() {
    try {
      const cfg = await adminService.configuracionDomicilios();
      const input = h("input", { class: "input", type: "number", min: 0, step: 1000, value: cfg.montoMaximoContraentrega, id: "monto" });
      const save = h("button", { class: "btn btn--red", type: "submit" }, "Guardar");
      const form = h("form", {}, h("div", { class: "field" }, h("label", { for: "monto" }, "Monto máximo para pagar contra entrega (COP)"),
        h("small", {}, "Los domicilios que superen este valor deben pagarse de forma virtual."), input), save);
      form.addEventListener("submit", async (e) => {
        e.preventDefault();
        save.disabled = true;
        try {
          await adminService.actualizarConfiguracionDomicilios(Number(input.value));
          toast("Configuración guardada", "ok");
        } catch (error) {
          toast(error.message, "error");
        } finally {
          save.disabled = false;
        }
      });
      mount(out, h("h2", {}, "Domicilios"), form);
    } catch (error) {
      mount(out, errorState(error, load));
    }
  }
  mount(container, pageHeader("Configuración", "Reglas de pago para domicilios."), out);
  load();
}
