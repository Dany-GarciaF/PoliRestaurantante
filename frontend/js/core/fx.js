/**
 * Efectos visuales: partículas, stickers flotantes y confeti.
 * Todo corre en <canvas>, se pausa fuera de pantalla/pestaña oculta y se desactiva con prefers-reduced-motion.
 */
const reduced = () => window.matchMedia("(prefers-reduced-motion: reduce)").matches;
const rand = (a, b) => a + Math.random() * (b - a);
const pick = (list) => list[Math.floor(Math.random() * list.length)];

function createLoop(canvas, { step, draw }) {
  const ctx = canvas.getContext("2d");
  let width = 0;
  let height = 0;
  let dpr = 1;
  let running = false;
  let visible = false;
  let last = 0;

  const resize = () => {
    dpr = Math.min(window.devicePixelRatio || 1, 2);
    const rect = canvas.getBoundingClientRect();
    width = rect.width;
    height = rect.height;
    canvas.width = Math.max(1, Math.round(width * dpr));
    canvas.height = Math.max(1, Math.round(height * dpr));
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
  };

  const frame = (now) => {
    if (!running) return;
    const dt = Math.min((now - last) / 16.67, 3);
    last = now;
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    step?.(dt, width, height);
    ctx.clearRect(0, 0, width, height);
    draw(ctx, width, height);
    requestAnimationFrame(frame);
  };

  const sync = () => {
    const should = visible && !document.hidden;
    if (should && !running) {
      running = true;
      last = performance.now();
      requestAnimationFrame(frame);
    } else if (!should) running = false;
  };

  new ResizeObserver(resize).observe(canvas);
  new IntersectionObserver(([entry]) => {
    visible = entry.isIntersecting;
    sync();
  }).observe(canvas);
  document.addEventListener("visibilitychange", sync);
  resize();
  return { size: () => ({ width, height }), wake: () => { visible = true; sync(); } };
}

/* Brasas: suben desde abajo y se apartan del puntero. */
function embers(canvas, { colors, count = 70, up = true }) {
  const parts = [];
  const pointer = { x: -999, y: -999 };
  const host = canvas.parentElement;
  host.addEventListener("pointermove", (e) => {
    const r = canvas.getBoundingClientRect();
    pointer.x = e.clientX - r.left;
    pointer.y = e.clientY - r.top;
  });
  host.addEventListener("pointerleave", () => (pointer.x = pointer.y = -999));

  const make = (w, h, anywhere) => ({
    x: rand(0, w),
    y: anywhere ? rand(0, h) : up ? h + 10 : rand(0, h),
    vx: rand(-0.3, 0.3),
    vy: up ? -rand(0.4, 1.6) : rand(-0.3, 0.3),
    r: rand(1.2, 4),
    a: rand(0.3, 1),
    t: rand(0, 6.28),
    color: pick(colors)
  });

  const loop = createLoop(canvas, {
    step(dt, w, h) {
      while (parts.length < count) parts.push(make(w, h, parts.length < count * 0.6 && !parts.some((p) => p.init)));
      for (const p of parts) {
        p.init = true;
        p.t += 0.03 * dt;
        p.x += (p.vx + Math.sin(p.t) * 0.4) * dt;
        p.y += p.vy * dt;
        const dx = p.x - pointer.x;
        const dy = p.y - pointer.y;
        const d2 = dx * dx + dy * dy;
        if (d2 < 14000) {
          const f = (1 - d2 / 14000) * 3.2 * dt;
          const d = Math.sqrt(d2) || 1;
          p.x += (dx / d) * f;
          p.y += (dy / d) * f;
        }
        if (p.y < -12 || p.y > h + 14 || p.x < -12 || p.x > w + 12) Object.assign(p, make(w, h, false));
      }
    },
    draw(ctx) {
      for (const p of parts) {
        ctx.globalAlpha = p.a * (0.55 + 0.45 * Math.sin(p.t * 2));
        ctx.fillStyle = p.color;
        ctx.shadowColor = p.color;
        ctx.shadowBlur = 12;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.r, 0, 6.283);
        ctx.fill();
      }
      ctx.globalAlpha = 1;
      ctx.shadowBlur = 0;
    }
  });
  return loop;
}

/* Stickers irreverentes: etiquetas prerenderizadas que flotan hacia arriba. Tope bajo para cuidar el rendimiento. */
const MAX_STICKERS = 16;
const STICKER_STYLES = [
  { bg: "#c8ff1f", fg: "#111" },
  { bg: "#ff3fa4", fg: "#fffaf0" },
  { bg: "#ffd23f", fg: "#111" },
  { bg: "#fffaf0", fg: "#d9190b" }
];
function stickerSprites(words) {
  return words.map((word, i) => {
    const { bg, fg } = STICKER_STYLES[i % STICKER_STYLES.length];
    const sprite = document.createElement("canvas");
    const c = sprite.getContext("2d");
    const font = '900 34px "Archivo Black", Impact, "Arial Black", sans-serif';
    c.font = font;
    const w = Math.ceil(c.measureText(word).width) + 44;
    const h = 64;
    sprite.width = w + 8;
    sprite.height = h + 8;
    c.font = font;
    c.lineJoin = "round";
    c.fillStyle = "#111";
    c.beginPath();
    c.roundRect(8, 8, w - 4, h - 4, 18);
    c.fill();
    c.fillStyle = bg;
    c.strokeStyle = "#111";
    c.lineWidth = 4;
    c.beginPath();
    c.roundRect(2, 2, w - 4, h - 4, 18);
    c.fill();
    c.stroke();
    c.fillStyle = fg;
    c.textAlign = "center";
    c.textBaseline = "middle";
    c.fillText(word, w / 2 - 2, h / 2 + 2);
    return sprite;
  });
}

function stickers(canvas, { words, rate = 0.012 }) {
  const parts = [];
  const sprites = stickerSprites(words);
  let acc = 0;
  const spawn = (w, h, anywhere = false) => {
    if (parts.length >= MAX_STICKERS) return;
    const sprite = pick(sprites);
    const scale = rand(0.55, 0.95);
    parts.push({
      x: rand(40, Math.max(41, w - 40)),
      y: anywhere ? rand(0, h) : h + 60,
      vy: -rand(0.35, 0.9),
      rot: rand(-0.5, 0.5),
      vr: rand(-0.006, 0.006),
      sprite,
      w: sprite.width * scale * 0.7,
      h: sprite.height * scale * 0.7,
      alpha: rand(0.75, 1),
      t: rand(0, 6.28)
    });
  };

  const loop = createLoop(canvas, {
    step(dt, w, h) {
      if (!parts.length) for (let i = 0; i < 8; i++) spawn(w, h, true);
      acc += rate * dt;
      while (acc >= 1) {
        acc -= 1;
        spawn(w, h);
      }
      for (let i = parts.length - 1; i >= 0; i--) {
        const p = parts[i];
        p.t += 0.02 * dt;
        p.y += p.vy * dt;
        p.x += Math.sin(p.t) * 0.35 * dt;
        p.rot += p.vr * dt;
        if (p.y < -80) parts.splice(i, 1);
      }
    },
    draw(ctx) {
      const d = ctx.getTransform().a;
      for (const p of parts) {
        const cos = Math.cos(p.rot) * d;
        const sin = Math.sin(p.rot) * d;
        ctx.globalAlpha = p.alpha;
        ctx.setTransform(cos, sin, -sin, cos, p.x * d, p.y * d);
        ctx.drawImage(p.sprite, -p.w / 2, -p.h / 2, p.w, p.h);
      }
      ctx.globalAlpha = 1;
      ctx.setTransform(d, 0, 0, d, 0, 0);
    }
  });
  return loop;
}

/* Confeti global, disparado desde un punto de la pantalla. */
let confettiLoop = null;
const confettiParts = [];
function ensureConfetti() {
  if (confettiLoop) return;
  const canvas = document.createElement("canvas");
  canvas.className = "fx-confetti";
  canvas.setAttribute("aria-hidden", "true");
  document.body.append(canvas);
  const colors = ["#c8ff1f", "#ff3fa4", "#ffd23f", "#ff7a1a", "#fffaf0", "#d9190b"];
  confettiLoop = createLoop(canvas, {
    step(dt, w, h) {
      for (let i = confettiParts.length - 1; i >= 0; i--) {
        const p = confettiParts[i];
        p.vy += 0.18 * dt;
        p.vx *= 0.99;
        p.x += p.vx * dt;
        p.y += p.vy * dt;
        p.rot += p.vr * dt;
        p.life -= dt;
        if (p.life <= 0 || p.y > h + 20) confettiParts.splice(i, 1);
      }
    },
    draw(ctx) {
      for (const p of confettiParts) {
        ctx.save();
        ctx.globalAlpha = Math.min(1, p.life / 25);
        ctx.translate(p.x, p.y);
        ctx.rotate(p.rot);
        ctx.fillStyle = p.color;
        ctx.fillRect(-p.w / 2, -p.h / 2, p.w, p.h);
        ctx.restore();
      }
    }
  });
  confettiLoop.colors = colors;
}

export function confetti(x = window.innerWidth / 2, y = window.innerHeight / 2, count = 90) {
  if (reduced()) return;
  ensureConfetti();
  for (let i = 0; i < count; i++) {
    const angle = rand(0, 6.28);
    const speed = rand(4, 13);
    confettiParts.push({
      x,
      y,
      vx: Math.cos(angle) * speed,
      vy: Math.sin(angle) * speed - 5,
      w: rand(6, 12),
      h: rand(4, 8),
      rot: rand(0, 6.28),
      vr: rand(-0.3, 0.3),
      life: rand(50, 95),
      color: pick(confettiLoop.colors)
    });
  }
  confettiLoop.wake();
}

/** Monta los efectos declarados con <canvas data-fx="embers|dust|stickers">. */
export function initFx() {
  if (reduced()) return;
  document.querySelectorAll("canvas[data-fx]").forEach((canvas) => {
    const kind = canvas.dataset.fx;
    if (kind === "embers") embers(canvas, { colors: ["#ff7a1a", "#ffd23f", "#ff3fa4", "#fffaf0"], count: 70 });
    if (kind === "dust") embers(canvas, { colors: ["#c8ff1f", "#ff3fa4", "#ffd23f"], count: 55, up: false });
    if (kind === "panel") embers(canvas, { colors: ["#ec2a40", "#ec2a40", "#ff6b7a", "#8b93ab"], count: 42, up: true });
    if (kind === "stickers") stickers(canvas, { words: ["¡ÑAM!", "WOW", "SIN FILTRO", "¡PIDE YA!", "BOOM", "ANTOJO", "NO MAMES", "PICA"] });
  });
  document.querySelectorAll("[data-confetti]").forEach((el) =>
    el.addEventListener("click", (e) => confetti(e.clientX, e.clientY, 40))
  );
}

/** Titulares que entran palabra por palabra al aparecer. */
export function initSplitHeadings() {
  const headings = document.querySelectorAll("[data-split]");
  headings.forEach((heading) => {
    const words = heading.textContent.trim().split(/\s+/);
    heading.setAttribute("aria-label", heading.textContent.trim());
    heading.replaceChildren(
      ...words.flatMap((word, i) => {
        const outer = document.createElement("span");
        outer.className = "word";
        outer.setAttribute("aria-hidden", "true");
        const inner = document.createElement("span");
        inner.style.setProperty("--w", i);
        inner.textContent = word;
        outer.append(inner);
        return [outer, document.createTextNode(" ")];
      })
    );
  });
  const observer = new IntersectionObserver(
    (entries) =>
      entries.forEach((entry) => entry.target.classList.toggle("is-visible", entry.isIntersecting)),
    { threshold: 0.4 }
  );
  headings.forEach((h) => observer.observe(h));
}

/** La cinta se inclina según la velocidad del scroll. */
export function initVelocitySkew() {
  if (reduced()) return;
  const targets = document.querySelectorAll(".tape-wrap");
  let lastY = scrollY;
  let skew = 0;
  const tick = () => {
    const delta = scrollY - lastY;
    lastY = scrollY;
    skew += (Math.max(-12, Math.min(12, delta * 0.6)) - skew) * 0.12;
    targets.forEach((t) => t.style.setProperty("--skew", `${skew.toFixed(2)}deg`));
    requestAnimationFrame(tick);
  };
  requestAnimationFrame(tick);
}
