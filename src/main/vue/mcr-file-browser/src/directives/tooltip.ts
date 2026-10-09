import { Directive } from 'vue';
import { Tooltip } from 'bootstrap';
import {
  DEFAULT_TOOLTIP_PLACEMENT,
  DEFAULT_TOOLTIP_TRIGGERS,
} from '@/constants/ui';

export type TooltipOptions = {
  title?: string;
  placement?: string;
  trigger?: string;
};

export const vTooltip: Directive<HTMLElement, TooltipOptions | undefined> = {
  mounted(el, { value }) {
    if (!value?.title) {
      return;
    }
    Tooltip.getOrCreateInstance(el, {
      title: value.title,
      placement: value.placement ?? DEFAULT_TOOLTIP_PLACEMENT,
      trigger: value.trigger ?? DEFAULT_TOOLTIP_TRIGGERS,
    } as Partial<Tooltip.Options>);
  },
  updated(el, { value, oldValue }) {
    if (value?.title && value.title !== oldValue?.title) {
      Tooltip.getOrCreateInstance(el).setContent({
        '.tooltip-inner': value.title,
      });
    }
  },
  beforeUnmount(el) {
    Tooltip.getInstance(el)?.dispose();
  },
};
