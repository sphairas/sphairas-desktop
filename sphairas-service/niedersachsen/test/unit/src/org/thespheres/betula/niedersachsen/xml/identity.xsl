<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:nds="http://www.thespheres.org/xsd/niedersachsen/zeugnisse.xsd" 
                xpath-default-namespace="http://www.thespheres.org/xsd/niedersachsen/zeugnisse.xsd" 
                version="1.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy copy-namespaces="yes">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[namespace-uri()='']">
        <xsl:element name="nds:{local-name()}">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>
    
</xsl:stylesheet>
