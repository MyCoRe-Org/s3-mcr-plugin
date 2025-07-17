import type { Component } from 'vue';
import { ValidationState } from './validation';

export interface StorageFormConfig<T, K extends keyof T = keyof T> {
  component: Component;
  createDefaults: () => T;
  useValidation: () => {
    isValid: ValidationState<Extract<K, string>>;
    validate: (data: T) => boolean;
    resetValidation: () => void;
  };
}
