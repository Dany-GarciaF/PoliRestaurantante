import { config } from "../config.js";

/** Sesión en sessionStorage: se descarta al cerrar la pestaña. */
export const session = {
  get() {
    try {
      const raw = sessionStorage.getItem(config.storageKey);
      if (!raw) return null;
      const data = JSON.parse(raw);
      if (data.exp && data.exp * 1000 < Date.now()) {
        sessionStorage.removeItem(config.storageKey);
        return null;
      }
      return data;
    } catch {
      return null;
    }
  },
  save({ accessToken, rol }) {
    const claims = decodeClaims(accessToken);
    const data = { token: accessToken, rol, correo: claims.sub ?? "", exp: claims.exp ?? 0 };
    sessionStorage.setItem(config.storageKey, JSON.stringify(data));
    return data;
  },
  clear() {
    sessionStorage.removeItem(config.storageKey);
  }
};

function decodeClaims(token) {
  try {
    const payload = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(decodeURIComponent(escape(atob(payload))));
  } catch {
    return {};
  }
}
