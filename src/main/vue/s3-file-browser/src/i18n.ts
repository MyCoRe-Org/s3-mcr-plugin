import { LangApiClient } from '@jsr/mycore__js-common/i18n';

export default class I18n {
  private static langApiClient: LangApiClient | null = null;

  private static loadingPromise: Promise<void> | null = null;

  private static complete = false;

  private static dict: Record<string, string>;

  private static fill: Record<string, string>[] = [];

  public static async loadToObject(
    i18n: Record<string, string>
  ): Promise<void> {
    if (I18n.complete) {
      I18n.applyTo(i18n);
      return;
    }
    this.fill.push(i18n);
    if (!I18n.loadingPromise) {
      I18n.loadingPromise = (async () => {
        const dict = await I18n.load('mcr.file.browser.');
        I18n.dict = dict;
        I18n.fill.forEach(d => I18n.applyTo(d));
        I18n.complete = true;
      })();
    }
    await I18n.loadingPromise;
  }

  private static applyTo(i18n: Record<string, string>) {
    Object.keys(I18n.dict).forEach(k => {
      i18n[k.substring('mcr.file.browser.'.length)] = I18n.dict[k];
    });
  }

  private static getClient(): LangApiClient {
    if (!I18n.langApiClient) {
      const baseUrl = (window as { webApplicationBaseURL?: string })
        .webApplicationBaseURL;
      if (!baseUrl) throw new Error('webApplicationBaseURL is not defined!');
      I18n.langApiClient = new LangApiClient(baseUrl);
    }
    return I18n.langApiClient;
  }

  private static async load(prefix: string): Promise<Record<string, string>> {
    return I18n.getClient().getTranslations(prefix + '*');
  }
}
