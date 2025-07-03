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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  exclude-result-prefixes="xsl">
  <xsl:import href="xslImport:modsmeta:metadata/extended-file-browser.xsl"/>

  <xsl:template match="/">
    <div id="mir-extended-file-browser">
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
    </div>

    <xsl:apply-imports/>
  </xsl:template>

</xsl:stylesheet>
