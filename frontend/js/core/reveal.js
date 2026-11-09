/** Animaciones de scroll: reveal, barra de progreso, parallax y contadores. */
const tracked = new Set();
let revealBound = false;

/** Posición vertical en el documento sin contar transforms del propio elemento (offsetTop las ignora). */
function layoutBox(node) {
  let top = 0;
  for (let el = node; el; el = el.offsetParent) top += el.offsetTop;
  return { top, bottom: top + node.offsetHeight };
}

/**
 * El efecto se repite al entrar y salir de pantalla (arriba o abajo).
 * Se calcula con la posición de layout, no con la caja transformada: el estado oculto desplaza el
 * elemento y, medido con IntersectionObserver, lo devolvía al viewport y provocaba parpadeo infinito.
 * Entra con el viewport ligeramente recortado y sale solo al quedar bien lejos (histéresis).
 */
function updateReveal() {
  const nodes = [...tracked].filter((node) => node.isConnected);
  const viewTop = window.scrollY;
  const viewBottom = viewTop + window.innerHeight;
  const enter = window.innerHeight * 0.08;
  const leave = 320;
  const boxes = nodes.map(layoutBox);
  nodes.forEach((node, i) => {
    const { top, bottom } = boxes[i];
    const visible = node.classList.contains("is-visible");
    const inside = bottom > viewTop + enter && top < viewBottom - enter;
    const far = bottom < viewTop - leave || top > viewBottom + leave;
    if (!visible && inside) node.classList.add("is-visible");
    else if (visible && far) node.classList.remove("is-visible");
  });
}

export function initReveal(root = document) {
  root.querySelectorAll("[data-reveal]").forEach((node) => tracked.add(node));
  if (!revealBound) {
    revealBound = true;
    let queued = false;
    const schedule = () => {
      if (queued) return;
      queued = true;
      requestAnimationFrame(() => {
        queued = false;
        updateReveal();
      });
    };
    window.addEventListener("scroll", schedule, { passive: true });
    window.addEventListener("resize", schedule);
    window.addEventListener("load", schedule);
  }
  updateReveal();
}

export function initScrollEffects() {
  const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const progress = document.querySelector("[data-scroll-progress]");
  const header = document.querySelector("[data-header]");
  const parallax = [...document.querySelectorAll("[data-parallax]")];
  const sections = [...document.querySelectorAll("[data-scrub]")];
  let ticking = false;

  function update() {
    ticking = false;
    const y = window.scrollY;
    const max = document.documentElement.scrollHeight - window.innerHeight;
    if (progress) progress.style.transform = `scaleX(${max > 0 ? y / max : 0})`;
    header?.classList.toggle("is-scrolled", y > 24);
    if (reduced) return;
    for (const node of parallax) {
      const speed = Number(node.dataset.parallax) || 0.1;
      const rect = node.getBoundingClientRect();
      const offset = (rect.top + rect.height / 2 - window.innerHeight / 2) * speed;
      node.style.setProperty("--py", `${offset.toFixed(1)}px`);
    }
    for (const section of sections) {
      const rect = section.getBoundingClientRect();
      const total = rect.height + window.innerHeight;
      const value = Math.min(1, Math.max(0, (window.innerHeight - rect.top) / total));
      section.style.setProperty("--scrub", value.toFixed(3));
    }
  }

  window.addEventListener(
    "scroll",
    () => {
      if (!ticking) {
        ticking = true;
        requestAnimationFrame(update);
      }
    },
    { passive: true }
  );
  window.addEventListener("resize", update);
  update();
}

export function initCounters(root = document) {
  const nodes = [...root.querySelectorAll("[data-count]")];
  const observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (!entry.isIntersecting) continue;
        observer.unobserve(entry.target);
        animateCount(entry.target);
      }
    },
    { threshold: 0.6 }
  );
  nodes.forEach((node) => observer.observe(node));
}

function animateCount(node) {
  const target = Number(node.dataset.count);
  const suffix = node.dataset.suffix ?? "";
  const duration = 1400;
  const start = performance.now();
  const tick = (now) => {
    const t = Math.min(1, (now - start) / duration);
    const eased = 1 - Math.pow(1 - t, 3);
    node.textContent = `${Math.round(target * eased).toLocaleString("es-CO")}${suffix}`;
    if (t < 1) requestAnimationFrame(tick);
  };
  requestAnimationFrame(tick);
}
