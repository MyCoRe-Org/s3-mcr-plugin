import { useI18n } from './useI18n';

export function useI18nPrefix(prefix: string) {
  const { t } = useI18n();

  function tp(suffix: string): string {
    return t(`${prefix}.${suffix}`);
  }

  return { t, tp };
}
