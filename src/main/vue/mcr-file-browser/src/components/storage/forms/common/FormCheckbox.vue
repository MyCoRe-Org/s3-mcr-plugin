<template>
  <div :id="groupId" class="form-check form-group">
    <input
      :id="id"
      v-model="model"
      class="form-check-input"
      type="checkbox"
      :disabled="disabled"
    />
    <label class="form-check-label" :for="id">{{ label }}</label>
    <b-tooltip
      v-if="tooltip"
      :target="groupId"
      :placement="tooltipPlacement"
      :triggers="tooltipTriggers"
      :title="tooltip"
    />
  </div>
</template>

<script setup lang="ts">
import { BTooltip } from 'bootstrap-vue-next';
import {
  DEFAULT_TOOLTIP_PLACEMENT,
  DEFAULT_TOOLTIP_TRIGGERS,
} from '@/constants/ui';

const props = withDefaults(
  defineProps<{
    id: string;
    label: string;
    tooltip?: string;
    disabled?: boolean;
    tooltipPlacement?: string;
    tooltipTriggers?: string;
  }>(),
  {
    tooltip: undefined,
    tooltipPlacement: DEFAULT_TOOLTIP_PLACEMENT,
    tooltipTriggers: DEFAULT_TOOLTIP_TRIGGERS,
  }
);

const model = defineModel<boolean>();

const groupId = `${props.id}Group`;
</script>
