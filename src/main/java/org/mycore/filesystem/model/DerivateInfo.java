package org.mycore.filesystem.model;


import java.util.List;
import java.util.Map;

public class DerivateInfo {

    protected String id;
    protected List<DerivateTitle> titles;
    protected boolean view;
    protected boolean delete;
    protected Map<String, Object> metadata;

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    protected boolean write;

    public DerivateInfo(String id, List<DerivateTitle> titles, boolean view, boolean delete, boolean write) {
        this.id = id;
        this.titles = titles;
        this.view = view;
        this.delete = delete;
        this.write = write;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DerivateTitle> getTitles() {
        return titles;
    }

    public void setTitles(List<DerivateTitle> titles) {
        this.titles = titles;
    }

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
