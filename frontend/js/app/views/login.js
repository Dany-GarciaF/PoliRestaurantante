import { h, mount } from "../../core/dom.js";
import { ApiError } from "../../core/api.js";
import { session } from "../../core/session.js";
import { authService } from "../services.js";

export function renderLogin(root, onSuccess) {
  const error = h("p", { class: "field__error", role: "alert" });
  const submit = h("button", { class: "btn btn--red btn--block", type: "submit" }, "Entrar");
  const correo = h("input", { class: "input", id: "correo", type: "email", required: true, autocomplete: "username", placeholder: "tu@correo.com" });
  const clave = h("input", { class: "input", id: "contrasena", type: "password", required: true, autocomplete: "current-password", placeholder: "••••••••" });

  const form = h(
    "form",
    { class: "login__form", novalidate: true },
    h("h1", {}, "Entra a la ", h("span", { class: "hand" }, "cocina")),
    h("p", {}, "Acceso para el equipo de Poli Restaurante."),
    h("div", { class: "field" }, h("label", { for: "correo" }, "Correo"), correo),
    h("div", { class: "field" }, h("label", { for: "contrasena" }, "Contraseña"), clave),
    error,
    submit,
    h("a", { class: "login__back", href: "index.html" }, "← Volver al sitio")
  );

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    error.textContent = "";
    if (!form.reportValidity()) return;
    submit.disabled = true;
    submit.textContent = "Entrando…";
    try {
      const data = await authService.login(correo.value.trim(), clave.value);
      onSuccess(session.save(data));
    } catch (e) {
      error.textContent = e instanceof ApiError && e.status === 401 ? "Correo o contraseña incorrectos." : e.message;
      submit.disabled = false;
      submit.textContent = "Entrar";
    }
  });

  mount(
    root,
    h(
      "main",
      { class: "login" },
      h(
        "section",
        { class: "login__art", "aria-hidden": "true" },
        h("div", { class: "login__rays" }),
        h("span", { class: "sticker" }, "Solo personal"),
        h("div", { class: "login__brand" }, "POLI", h("span", {}, "RESTAURANTE")),
        h("p", { class: "hand" }, "Hamburguesas que arman el despelote"),
        h("div", { class: "login__ribbon" }, h("div", { class: "login__ribbon-track" }, [0, 1].map(() => h("span", {}, "¡ÑAM! ✦ SIN FILTRO ✦ BOOM ✦ ANTOJO ✦ PICA ✦ ¡PIDE YA! ✦ "))))
      ),
      h("section", { class: "login__panel" }, form)
    )
  );
  correo.focus();
}
