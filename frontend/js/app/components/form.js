import { h } from "../../core/dom.js";
import { ApiError } from "../../core/api.js";
import { openModal, toast } from "../../core/ui.js";

/**
 * Formulario modal declarativo.
 * field: { name, label, type, options, required, hint, attrs, default }
 * Muestra los errores por campo que devuelve la API (`errors`).
 */
export function openForm({ title, fields, initial = {}, submitLabel = "Guardar", onSubmit, wide = false }) {
  const controls = new Map();
  const errorNodes = new Map();

  const rows = fields.map((field) => {
    const id = `f-${field.name}`;
    const value = initial[field.name];
    let control;

    if (field.type === "select") {
      control = h(
        "select",
        { class: "select", id, name: field.name, required: field.required },
        field.options.map((option) =>
          h("option", { value: option.value, selected: String(option.value) === String(value ?? "") }, option.label)
        )
      );
    } else if (field.type === "textarea") {
      control = h("textarea", { class: "textarea", id, name: field.name, required: field.required, ...(field.attrs ?? {}) }, value ?? "");
    } else if (field.type === "checkbox") {
      control = h("input", { type: "checkbox", id, name: field.name, checked: value ?? field.default ?? false });
    } else {
      control = h("input", {
        class: "input",
        id,
        name: field.name,
        type: field.type ?? "text",
        required: field.required,
        value: value ?? "",
        autocomplete: field.type === "password" ? "new-password" : "off",
        ...(field.attrs ?? {})
      });
    }

    controls.set(field.name, { control, field });
    const error = h("div", { class: "field__error", "aria-live": "polite" });
    errorNodes.set(field.name, error);

    if (field.type === "checkbox") {
      return h("div", { class: "field" }, h("label", { class: "check", for: id }, control, field.label), error);
    }
    return h(
      "div",
      { class: "field" },
      h("label", { for: id }, field.label),
      control,
      field.hint ? h("small", {}, field.hint) : null,
      error
    );
  });

  const form = h("form", { novalidate: true }, ...rows);
  const modal = openModal({
    title,
    wide,
    content: form,
    footer: [
      h("button", { class: "btn btn--cream", type: "button", onClick: () => modal.close() }, "Cancelar"),
      h("button", { class: "btn btn--red", type: "button", onClick: () => form.requestSubmit() }, submitLabel)
    ]
  });

  function collect() {
    const values = {};
    for (const [name, { control, field }] of controls) {
      if (field.type === "checkbox") values[name] = control.checked;
      else if (field.type === "number") values[name] = control.value === "" ? null : Number(control.value);
      else {
        const raw = control.value.trim();
        values[name] = raw === "" && !field.required ? null : raw;
      }
    }
    return values;
  }

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    errorNodes.forEach((node) => (node.textContent = ""));
    if (!form.reportValidity()) return;
    const buttons = modal.root.querySelectorAll(".modal__foot button");
    buttons.forEach((b) => (b.disabled = true));
    try {
      await onSubmit(collect());
      modal.close();
    } catch (error) {
      if (error instanceof ApiError && error.fieldErrors) {
        for (const [name, messages] of Object.entries(error.fieldErrors)) {
          const node = errorNodes.get(name);
          if (node) node.textContent = [messages].flat().join(" ");
        }
      }
      toast(error.message ?? "No se pudo guardar.", "error");
    } finally {
      buttons.forEach((b) => (b.disabled = false));
    }
  });

  return modal;
}
