export type FieldValidation = {
  clean: boolean;
  valid: boolean;
};

export type ValidationState<K extends string> = Record<K, FieldValidation>;
