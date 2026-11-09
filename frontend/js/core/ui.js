import { h } from "./dom.js";

function toastRoot() {
  let root = document.querySelector(".toasts");
  if (!root) {
    root = h("div", { class: "toasts", role: "status", "aria-live": "polite" });
    document.body.append(root);
  }
  return root;
}

export function toast(message, type = "info", duration = 4200) {
  const node = h("div", { class: `toast toast--${type}` }, message);
  toastRoot().append(node);
  setTimeout(() => {
    node.classList.add("is-leaving");
    node.addEventListener("animationend", () => node.remove(), { once: true });
  }, duration);
}

/** Abre un modal accesible; devuelve { close, body } para componer contenido. */
export function openModal({ title, content, footer = null, wide = false, onClose = () => {} }) {
  const previousFocus = document.activeElement;
  const body = h("div", { class: "modal__body" }, content);
  const closeButton = h("button", { class: "btn btn--lime btn--sm btn--icon", type: "button", "aria-label": "Cerrar" }, "✕");
  const modal = h(
    "div",
    { class: `modal${wide ? " modal--wide" : ""}`, role: "dialog", "aria-modal": "true", "aria-label": title },
    h("div", { class: "modal__head" }, h("h2", {}, title), closeButton),
    body,
    footer ? h("div", { class: "modal__foot" }, footer) : null
  );
  const root = h("div", { class: "modal-root" }, modal);

  function close() {
    root.classList.add("is-closing");
    document.removeEventListener("keydown", onKey);
    root.addEventListener("animationend", () => root.remove(), { once: true });
    previousFocus?.focus?.();
    onClose();
  }

  function onKey(event) {
    if (event.key === "Escape") close();
  }

  closeButton.addEventListener("click", close);
  root.addEventListener("pointerdown", (event) => {
    if (event.target === root) close();
  });
  document.addEventListener("keydown", onKey);
  document.body.append(root);
  closeButton.focus();
  return { close, body, root };
}

export function confirmDialog({ title, message, confirmLabel = "Confirmar", danger = false }) {
  return new Promise((resolve) => {
    let decided = false;
    const finish = (value) => {
      if (decided) return;
      decided = true;
      dialog.close();
      resolve(value);
    };
    const dialog = openModal({
      title,
      content: h("p", {}, message),
      footer: [
        h("button", { class: "btn btn--cream", type: "button", onClick: () => finish(false) }, "Cancelar"),
        h("button", { class: `btn ${danger ? "btn--red" : "btn--lime"}`, type: "button", onClick: () => finish(true) }, confirmLabel)
      ],
      onClose: () => {
        if (!decided) {
          decided = true;
          resolve(false);
        }
      }
    });
  });
}

/** Ejecuta una acción deshabilitando el botón mientras corre y notifica errores. */
export async function withButton(button, action) {
  const original = button.textContent;
  button.disabled = true;
  button.replaceChildren(h("span", { class: "spinner", "aria-hidden": "true" }));
  try {
    return await action();
  } finally {
    button.disabled = false;
    button.textContent = original;
  }
}
