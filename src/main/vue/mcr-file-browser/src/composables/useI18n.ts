import { reactive, readonly } from 'vue';

const _translations = reactive<Record<string, string>>({});

export function useI18n() {
  const t = (key: string): string => {
    return _translations[key] || `???${key}???`;
  };

  const setMessage = (key: string, message: string): void => {
    _translations[key] = message;
  };

  const addMessages = (messages: Record<string, string>): void => {
    for (const [key, value] of Object.entries(messages)) {
      _translations[key] = value;
    }
  };

  const translations = readonly(_translations);

  return {
    t,
    setMessage,
    addMessages,
    translations,
  };
}
