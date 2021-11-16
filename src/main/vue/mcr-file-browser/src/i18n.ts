/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */


export class I18n {

    private static loading = false;
    private static complete = false;
    private static dict: Record<string, string>;
    private static fill: Record<string, string>[] = [];

    public static async loadToObject(i18n: Record<string, string>) {
        if(!I18n.complete){
            this.fill.push(i18n);

            if (!I18n.loading) {
                I18n.loading = true;
                this.dict = await I18n.load("mcr.file.browser.");
                I18n.loading = false;
                this.fill.forEach(i18n => {
                    for (const key in I18n.dict) {
                        i18n[key.substring("mcr.file.browser.".length)] = I18n.dict[key];
                    }
                })
                I18n.complete = true;
            }
        } else {
            for (const key in I18n.dict) {
                i18n[key.substring("mcr.file.browser.".length)] = I18n.dict[key];
            }
        }
    }

    private static async load(prefix: string): Promise<Record<string, string>> {
        if ("webApplicationBaseURL" in window) {
            const baseURL: string = (<any>((window)))['webApplicationBaseURL'];
            const response = await fetch(`${baseURL}rsc/locale/translate/${prefix}*`);
            return response.json();
        }

        throw new Error("webApplicationBaseURL is not defined!");
    }


}