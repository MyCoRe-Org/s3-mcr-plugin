// In production, Bootstrap is provided as global bundle by the host page (MIR).
// Vite aliases 'bootstrap' to this module for builds, so it is not bundled twice.
import type * as Bootstrap from 'bootstrap';

declare global {
  interface Window {
    bootstrap?: typeof Bootstrap;
  }
}

const getBootstrap = (): typeof Bootstrap => {
  if (!window.bootstrap) {
    throw new Error('Bootstrap is not loaded');
  }
  return window.bootstrap;
};

// Resolve the global lazily, since it may not exist yet when this module is evaluated.
const lazy = <K extends 'Modal' | 'Tooltip'>(name: K): (typeof Bootstrap)[K] =>
  new Proxy(function () {} as unknown as (typeof Bootstrap)[K], {
    construct: (_target, args) =>
      Reflect.construct(getBootstrap()[name], args) as object,
    get: (_target, property) => Reflect.get(getBootstrap()[name], property),
  });

export const Modal = lazy('Modal');
export const Tooltip = lazy('Tooltip');
