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

package org.mycore.externalstore.rest.dto;

public class MCRDerivateTitleDto {

    private String text;

    private String lang;

    private String form;

    public MCRDerivateTitleDto(String text, String lang, String form) {
        this.text = text;
        this.lang = lang;
        this.form = form;
    }

    public String getForm() {
        return form;
    }

    public String getLang() {
        return lang;
    }

    public String getText() {
        return text;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setText(String text) {
        this.text = text;
    }
}
