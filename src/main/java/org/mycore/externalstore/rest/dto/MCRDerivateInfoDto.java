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

import java.util.List;
import java.util.Map;

/**
 * Dto for derivate info.
 */
public class MCRDerivateInfoDto {

    private String id;

    private List<MCRDerivateTitleDto> titles;

    private boolean view;

    private boolean delete;

    private Map<String, String> metadata;

    private boolean write;

    public MCRDerivateInfoDto(String id, List<MCRDerivateTitleDto> titles, boolean view, boolean delete,
        boolean write) {
        this.id = id;
        this.titles = titles;
        this.view = view;
        this.delete = delete;
        this.write = write;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<MCRDerivateTitleDto> getTitles() {
        return titles;
    }

    public void setTitles(List<MCRDerivateTitleDto> titles) {
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

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

}
