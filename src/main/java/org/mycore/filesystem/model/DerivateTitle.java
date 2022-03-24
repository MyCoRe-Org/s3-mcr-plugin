package org.mycore.filesystem.model;

public class DerivateTitle {

    protected String text;
    protected String lang;
    protected String form;

    public DerivateTitle(String text, String lang, String form) {
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
