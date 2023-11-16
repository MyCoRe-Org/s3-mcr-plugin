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

public class MCRDerivateInfosDto {

    private List<MCRDerivateInfoDto> derivates;

    private boolean create;

    public MCRDerivateInfosDto(List<MCRDerivateInfoDto> derivates, boolean create) {
        this.derivates = derivates;
        this.create = create;
    }

    public List<MCRDerivateInfoDto> getDerivates() {
        return derivates;
    }

    public void setDerivates(List<MCRDerivateInfoDto> derivates) {
        this.derivates = derivates;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

}
