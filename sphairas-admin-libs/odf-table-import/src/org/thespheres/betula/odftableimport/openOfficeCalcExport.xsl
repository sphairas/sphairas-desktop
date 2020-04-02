<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : filter.xsl
    Created on : 23. August 2015, 10:36
    Author     : boris.heithecker
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
                xmlns:bt="http://www.thespheres.org/xsd/betula/betula.xsd"
                xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
                xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
                xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
                xmlns="http://www.thespheres.org/xsd/betula/csv-import.xsd" 
                exclude-result-prefixes="table text office">
    
    <xsl:output indent = "yes" encoding = "UTF-8" omit-xml-declaration = "no"/>
    
    <xsl:template match="/">
        
        <xsl:variable name="num">
            <xsl:value-of select="count(/office:document-content/office:body/office:spreadsheet/table:table)" />
        </xsl:variable>
        
        <xsl:choose>
            
            <xsl:when test="$num = 1">
                <!--<xsl:for-each select="">-->
                                
                <xml-csv-file>   
               
                    <xsl:apply-templates select="/office:document-content/office:body/office:spreadsheet/table:table[1]">
                    
                    </xsl:apply-templates>
                </xml-csv-file>
            
                <!--</xsl:for-each>-->
            </xsl:when>
            
            <xsl:otherwise>
                <xml-csv-files>
                    <xsl:for-each select="/office:document-content/office:body/office:spreadsheet/table:table">
                                
                        <xml-csv-file>   
           
                            <xsl:attribute name="id">
                                <xsl:value-of select="@table:name"/>
                            </xsl:attribute>
               
                            <xsl:apply-templates select=".">
                    
                            </xsl:apply-templates>
                        </xml-csv-file>
            
                    </xsl:for-each>
                </xml-csv-files>
            </xsl:otherwise>
            
        </xsl:choose>
                
        <!--        <xsl:if test="$num = 2">  
        </xml-csv-files>
        </xsl:if>-->
        
    </xsl:template>
    
    <xsl:template match="/office:document-content/office:body/office:spreadsheet/table:table" >

            
        <products>
            <product id="openOfficeCalcExport">Libre Office Calc Export File</product>
        </products>           
            
        <!-- Process all table-rows after the column labels in table-row 1 -->
        <xsl:for-each select="table:table-row">
            <xsl:if test="position()=1">                        
                <columns>
                    <xsl:for-each select="table:table-cell">
                        <!--                                <xsl:variable name="colpos" select="position()">
                        </xsl:variable>
                        <xsl:variable name="colname">
                            <xsl:value-of select="../../table:table-row[1]/table:table-cell[$colpos]/text:p"/>
                        </xsl:variable>-->
                        <column>
                            <xsl:attribute name="column-id">
                                <xsl:value-of select="position()"/>
                            </xsl:attribute>
                            <display-label>
                                <!--<xsl:value-of select="../../table:table-row[1]/table:table-cell[$colpos]/text:p"/>-->
                                <xsl:value-of select="text:p"/>
                            </display-label>
                        </column>
                    </xsl:for-each>                           
                </columns>                        
            </xsl:if>                    
        </xsl:for-each>
                
        <lines>
            <!--<xsl:for-each select="/office:document/office:body/office:spreadsheet/table:table">-->
            <!-- Process all table-rows after the column labels in table-row 1 -->
            <xsl:for-each select="table:table-row">
                <xsl:if test="position()>1">                        
                    <line>
                        <xsl:for-each select="table:table-cell">
                            <!--                                <xsl:variable name="colpos" select="position()">
                            </xsl:variable>-->
                            <!--                                <xsl:variable name="elname">
                                <xsl:value-of select="../../table:table-row[1]/table:table-cell[$colpos]/text:p"/>
                            </xsl:variable>-->
                            <value>
                                <xsl:attribute name="column-id">
                                    <xsl:value-of select="position()"/>
                                </xsl:attribute>
                                <xsl:value-of select="text:p"/>
                            </value>
                        </xsl:for-each>
                    </line>                        
                </xsl:if>
            </xsl:for-each>
            <!--</xsl:for-each>-->
        </lines>     
        
    </xsl:template>
    
</xsl:stylesheet>
