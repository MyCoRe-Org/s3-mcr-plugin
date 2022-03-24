<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ This file is part of ***  M y C o R e  ***
  ~ See http://www.mycore.de/ for details.
  ~
  ~ MyCoRe is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~
  ~ MyCoRe is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~
  ~ You should have received a copy of the GNU General Public License
  ~ along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mcr="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation" xmlns:mods="http://www.loc.gov/mods/v3" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:acl="xalan://org.mycore.access.MCRAccessManager"

                exclude-result-prefixes="i18n mcr mods xlink acl">
    <xsl:import href="xslImport:modsmeta:metadata/extended-file-browser.xsl" />
    <xsl:template match="/">
        <div id="mir-extended-file-browser">
            <div class="mir_extended_file_browser">
                <script src="{$WebApplicationBaseURL}webjars/vue/2.6.14/vue.min.js" />
                <script src="{$WebApplicationBaseURL}vue/file-browser/fileBrowser.umd.min.js" />
                <link href="{$WebApplicationBaseURL}vue/file-browser/fileBrowser.css" rel="stylesheet"/>
                <div id="mir-extended-file-browser-mount">
                    <fb base-url="{$WebApplicationBaseURL}" object-id="{mycoreobject/@ID}"></fb>
                </div>
                <script>
                    new Vue({
                    components: {
                    fb: fileBrowser
                    }
                    }).$mount('#mir-extended-file-browser-mount');
                </script>
            </div>
        </div>
        <xsl:apply-imports />
    </xsl:template>

</xsl:stylesheet>