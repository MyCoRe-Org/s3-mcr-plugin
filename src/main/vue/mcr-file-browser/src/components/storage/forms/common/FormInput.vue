<template>
  <div class="form-group">
    <label :for="id">{{ label }}</label>
    <input
      :id="id"
      v-model="model"
      :type="type"
      :class="['form-control', validationClass]"
      :autocomplete="autocomplete"
      :disabled="disabled"
    />
    <b-tooltip
      v-if="tooltip"
      :target="id"
      :placement="tooltipPlacement"
      :triggers="tooltipTriggers"
      :title="tooltip"
    />
    <div v-if="showError" class="invalid-feedback">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { BTooltip } from 'bootstrap-vue-next';
import {
  DEFAULT_TOOLTIP_PLACEMENT,
  DEFAULT_TOOLTIP_TRIGGERS,
} from '@/constants/ui';

interface Validation {
  valid: boolean;
  clean: boolean;
}

const props = withDefaults(
  defineProps<{
    id: string;
    label: string;
    tooltip?: string;
    errorMessage?: string;
    type?: string;
    validation?: Validation;
    autocomplete?: string;
    disabled?: boolean;
    tooltipPlacement?: string;
    tooltipTriggers?: string;
  }>(),
  {
    tooltip: undefined,
    type: 'text',
    validation: undefined,
    autocomplete: undefined,
    errorMessage: undefined,
    tooltipPlacement: DEFAULT_TOOLTIP_PLACEMENT,
    tooltipTriggers: DEFAULT_TOOLTIP_TRIGGERS,
  }
);

const model = defineModel<string>();

const validationClass = computed(() => {
  if (!props.validation || props.validation.clean) {
    return '';
  }
  return props.validation.valid ? 'is-valid' : 'is-invalid';
});

const showError = computed(
  () => props.validation && !props.validation.clean && !props.validation.valid
);
</script>
