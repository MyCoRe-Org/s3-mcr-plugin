package org.mycore.filesystem.model;

import java.util.List;

public class DerivateInformations {
    protected List<DerivateInfo> derivates;
    protected boolean create;

    public DerivateInformations(List<DerivateInfo> derivates, boolean create) {
        this.derivates = derivates;
        this.create = create;
    }

    public List<DerivateInfo> getDerivates() {
        return derivates;
    }

    public void setDerivates(List<DerivateInfo> derivates) {
        this.derivates = derivates;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }
}
