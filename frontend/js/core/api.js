import { config } from "../config.js";
import { session } from "./session.js";

export class ApiError extends Error {
  constructor(status, detail, fieldErrors = null) {
    super(detail);
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

let onUnauthorized = () => {};
export const setUnauthorizedHandler = (handler) => {
  onUnauthorized = handler;
};

async function request(method, path, { query, body, auth = true } = {}) {
  const url = new URL(path, config.apiBaseUrl);
  for (const [key, value] of Object.entries(query ?? {})) {
    if (value !== undefined && value !== null && value !== "") url.searchParams.set(key, value);
  }

  const headers = { Accept: "application/json" };
  if (body !== undefined && !(body instanceof FormData)) headers["Content-Type"] = "application/json";
  const current = session.get();
  if (auth && current) headers.Authorization = `Bearer ${current.token}`;

  let response;
  try {
    response = await fetch(url, { method, headers, body: body === undefined ? undefined : body instanceof FormData ? body : JSON.stringify(body) });
  } catch {
    throw new ApiError(0, "No se pudo conectar con el servidor. Verifica que el backend esté en ejecución.");
  }

  if (response.status === 204) return null;
  const isJson = response.headers.get("content-type")?.includes("json");
  const payload = isJson ? await response.json() : null;

  if (!response.ok) {
    if (response.status === 401 && auth) onUnauthorized();
    throw new ApiError(response.status, payload?.detail ?? "Ocurrió un error inesperado.", payload?.errors ?? null);
  }
  return payload;
}

export const api = {
  get: (path, query) => request("GET", path, { query }),
  post: (path, body) => request("POST", path, { body }),
  put: (path, body) => request("PUT", path, { body }),
  patch: (path, body) => request("PATCH", path, { body }),
  delete: (path) => request("DELETE", path),
  publicGet: (path, query) => request("GET", path, { query, auth: false }),
  login: (body) => request("POST", "/api/auth/login", { body, auth: false })
};
