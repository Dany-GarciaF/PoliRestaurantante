# Frontend · Poli Restaurante

Sitio web en **JavaScript (módulos ES), HTML y CSS** sin dependencias ni paso de build.

- `index.html`: landing pública (menú real desde `/api/publico/menu/*`, con datos de demostración si el backend no responde).
- `app.html`: panel interno (login + vistas por rol: Mesero, Cocina, Administrador).

## Ejecutar

1. Backend en `http://localhost:8080` (`.\gradlew.bat bootRun`).
2. Servir esta carpeta en el puerto **5500** (los módulos ES no funcionan con `file://`):
   ```powershell
   cd frontend
   python -m http.server 5500
   # o: npx serve -l 5500
   ```
3. Abrir `http://localhost:5500` (landing) y `http://localhost:5500/app.html` (panel).

Otro puerto/origen: agrégalo a `CORS_ALLOWED_ORIGINS` en `.env` (separado por comas).
La URL del API se cambia en `js/config.js`.

## Estructura

```
css/    tokens (diseño) · base · components · landing · app
js/core/     api, session, dom, ui, format, reveal   (infraestructura reutilizable)
js/landing/  main, menu, burger
js/app/      main (router por rol), services (contratos HTTP), components/, views/
```

## Notas

- Los testimonios de `index.html` son **ejemplos**: reemplázalos por reales.
- Las hamburguesas son ilustraciones SVG (`js/landing/burger.js`); se pueden cambiar por fotos.
- La sesión vive en `sessionStorage` (se pierde al cerrar la pestaña). Todo texto del API se inserta con `textContent`, nunca `innerHTML`.
- Las animaciones respetan `prefers-reduced-motion`.
