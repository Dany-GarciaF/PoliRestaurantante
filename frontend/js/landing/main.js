import { initCounters, initReveal, initScrollEffects } from "../core/reveal.js";
import { $, $$ } from "../core/dom.js";
import { initFx, initSplitHeadings, initVelocitySkew } from "../core/fx.js";
import { initMenu } from "./menu.js";

function initNavigation() {
  const header = $("[data-header]");
  const toggle = $("[data-nav-toggle]");
  const panel = $("[data-nav-panel]");
  if (!toggle || !panel) return;

  const setOpen = (open) => {
    header.classList.toggle("is-open", open);
    toggle.setAttribute("aria-expanded", String(open));
    document.body.classList.toggle("no-scroll", open);
  };

  toggle.addEventListener("click", () => setOpen(toggle.getAttribute("aria-expanded") !== "true"));
  $$("a", panel).forEach((link) => link.addEventListener("click", () => setOpen(false)));
  document.addEventListener("keydown", (event) => event.key === "Escape" && setOpen(false));

  const links = $$("[data-nav-link]");
  const sections = links.map((link) => $(link.getAttribute("href"))).filter(Boolean);
  const observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (!entry.isIntersecting) continue;
        links.forEach((link) => link.classList.toggle("is-active", link.getAttribute("href") === `#${entry.target.id}`));
      }
    },
    { rootMargin: "-45% 0px -50% 0px" }
  );
  sections.forEach((section) => observer.observe(section));
}

function initTilt() {
  if (window.matchMedia("(hover: none), (prefers-reduced-motion: reduce)").matches) return;
  document.addEventListener("pointermove", (event) => {
    const card = event.target.closest?.(".dish, .why-card");
    if (!card) return;
    const rect = card.getBoundingClientRect();
    const x = (event.clientX - rect.left) / rect.width - 0.5;
    const y = (event.clientY - rect.top) / rect.height - 0.5;
    card.style.setProperty("--rx", `${(-y * 8).toFixed(2)}deg`);
    card.style.setProperty("--ry", `${(x * 10).toFixed(2)}deg`);
  });
  document.addEventListener(
    "pointerout",
    (event) => {
      const card = event.target.closest?.(".dish, .why-card");
      if (!card || card.contains(event.relatedTarget)) return;
      card.style.removeProperty("--rx");
      card.style.removeProperty("--ry");
    },
    true
  );
}

function initCarousel() {
  const track = $("[data-carousel]");
  if (!track) return;
  const step = () => Math.min(track.clientWidth * 0.9, 420);
  $("[data-carousel-prev]")?.addEventListener("click", () => track.scrollBy({ left: -step(), behavior: "smooth" }));
  $("[data-carousel-next]")?.addEventListener("click", () => track.scrollBy({ left: step(), behavior: "smooth" }));
}

initNavigation();
initTilt();
initCarousel();
initScrollEffects();
initReveal();
initCounters();
initSplitHeadings();
initFx();
initVelocitySkew();
window.addEventListener("poli:content-added", () => initReveal());
initMenu();

requestAnimationFrame(() => document.body.classList.add("is-ready"));
