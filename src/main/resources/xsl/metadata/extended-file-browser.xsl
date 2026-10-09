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
                xmlns:embargo="xalan://org.mycore.mods.MCRMODSEmbargoUtils"

                exclude-result-prefixes="i18n mcr mods xlink acl embargo">
    <xsl:import href="xslImport:modsmeta:metadata/extended-file-browser.xsl" />
    <xsl:template match="/">
        <xsl:variable xmlns:encoder="xalan://java.net.URLEncoder" name="loginURL"
          select="concat( $ServletsBaseURL, 'MCRLoginServlet?url=', encoder:encode( string( $RequestURL ) ) )" />
        <xsl:variable name="storeDerivates"
          select="mycoreobject/structure/derobjects/derobject[classification[@classid='derivate_types'][starts-with(@categid,'external_store_')]]" />

        <xsl:choose>
            <xsl:when test="key('rights', mycoreobject/@ID)/@read">
                <div id="mir-extended-file-browser">
                    <xsl:choose>
                        <xsl:when test="$storeDerivates and not($storeDerivates[key('rights', @xlink:href)/@read])">
                            <div id="mir-access-restricted">
                                <h3>
                                    <xsl:value-of select="i18n:translate('mcr.file.browser.headline')" />
                                </h3>
                                <div class="alert alert-warning" role="alert">
                                    <xsl:variable name="embargoDate" select="embargo:getEmbargo(mycoreobject/@ID)" />
                                    <xsl:choose>
                                        <xsl:when test="string-length($embargoDate)&gt;0">
                                            <!-- embargo is active -->
                                            <xsl:value-of select="i18n:translate('component.mods.metaData.dictionary.accessCondition.embargo.available',$embargoDate)" />
                                        </xsl:when>
                                        <xsl:when test="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:accessCondition[@type='restriction on access'][substring-after(@xlink:href,'#')='intern']">
                                            <xsl:value-of disable-output-escaping="yes" select="i18n:translate('mir.derivate.no_access.intern',$loginURL)" />
                                        </xsl:when>
                                        <xsl:when test="mycoreobject/metadata/def.modsContainer/modsContainer/mods:mods/mods:accessCondition[@type='restriction on access'][substring-after(@xlink:href,'#')='ipAddressRange']">
                                            <xsl:value-of select="i18n:translate('mir.derivate.no_access.ipAddressRange')" />
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="i18n:translate('mir.derivate.no_access')" />
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </div>
                            </div>
                        </xsl:when>
                        <xsl:otherwise>
                            <div class="mir_extended_file_browser">
                                <script type="importmap">
                                    <xsl:text>{</xsl:text>
                                    <xsl:text>"imports": {</xsl:text>
                                    <xsl:text>"vue": "</xsl:text>
                                    <xsl:value-of select="$WebApplicationBaseURL"/>
                                    <xsl:text>webjars/vue/3.5.17/dist/vue.runtime.esm-browser.prod.js"</xsl:text>
                                    <xsl:text>}</xsl:text>
                                    <xsl:text>}</xsl:text>
                                </script>
                                <script type="module" src="{$WebApplicationBaseURL}vue/external-storage-viewer/external-storage-viewer.es.js"/>
                                <link href="{$WebApplicationBaseURL}vue/external-storage-viewer/external-storage-viewer.css" rel="stylesheet"/>
                                <file-browser base-url="{$WebApplicationBaseURL}" object-id="{mycoreobject/@ID}"></file-browser>
                            </div>
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
            </xsl:when>
            <xsl:otherwise>
                <xsl:comment>
                    <xsl:value-of select="'mir-extended-file-browser: no &quot;read&quot; permission'" />
                </xsl:comment>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-imports />
    </xsl:template>

</xsl:stylesheet>
