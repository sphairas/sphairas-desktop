<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
                xmlns:bt="http://www.thespheres.org/xsd/betula/betula.xsd"
                xmlns:csv="http://www.thespheres.org/xsd/betula/csv-import.xsd"
                exclude-result-prefixes="">
    
    <xsl:output indent = "yes" encoding = "UTF-8" omit-xml-declaration = "no"/>
    <xsl:param name="signee.suffix" />
    
    <xsl:template match="/">
        
        <xi:xml-import xmlns:xi="http://www.thespheres.org/xsd/betula/xml-import.xsd">
            
            <products>  
                
                <xsl:if test="/csv:xml-csv-file/csv:products/csv:product">
                    <xsl:for-each select="/csv:xml-csv-file/csv:products/csv:product">
                        <product>                            
                            <xsl:attribute name="id">
                                <xsl:value-of select="@id"/> 
                            </xsl:attribute>
                            <xsl:value-of select="."/> 
                        </product>
                    </xsl:for-each>    
                </xsl:if>        
                   
            </products>
        
            <items>                        
                            
                <xsl:for-each select="/csv:xml-csv-file/csv:lines/csv:line">
                            
                    <xi:signee-item>
                            
                        <xsl:attribute name="position"> 
                            <xsl:value-of select="position()"/> 
                        </xsl:attribute>
                            
                        <xsl:if test="csv:value">
                            
                            <xsl:for-each select="csv:value">
                                <xsl:variable name="colid" select="@column-id"/>
                                <xsl:variable name="elname">
                                    <xsl:value-of select="/csv:xml-csv-file/csv:columns/csv:column[@column-id=$colid]/@assigned-key"/>
                                </xsl:variable>
                                <xsl:choose>                                  
                                              
                                    <xsl:when test="$elname != ''">
                                        <!--<xsl:if test="$elname != 'source-unit' and $elname != 'unit.id' and $elname != 'student.id'">-->
                                        <xsl:if test="$elname != 'signee' and $elname != 'signee.prefix'">
                                            <xsl:element name="{$elname}">
                                                <xsl:value-of select="."/> 
                                            </xsl:element>
                                        </xsl:if>   
                    
                                        <xsl:if test="$elname = 'signee'">
                                            <bt:signee>                                               
                                                <xsl:attribute name="prefix"> 
                                                    <xsl:value-of select="substring-before(current(),'@')" />
                                                </xsl:attribute>
                                                <xsl:attribute name="suffix"> 
                                                    <xsl:value-of select="substring-after(current(),'@')" /> 
                                                </xsl:attribute>
                                                <xsl:attribute name="alias">true</xsl:attribute>  
                                            </bt:signee>
                                        </xsl:if>
                                        
                                        <xsl:if test="$elname = 'signee.prefix'">
                                            <bt:signee>                                               
                                                <xsl:attribute name="prefix"> 
                                                    <xsl:value-of select="." />
                                                </xsl:attribute>
                                                <xsl:attribute name="suffix"> 
                                                    <xsl:value-of select="$signee.suffix" /> 
                                                </xsl:attribute>
                                                <xsl:attribute name="alias">true</xsl:attribute>  
                                            </bt:signee>
                                        </xsl:if>
                                                                                        
                                    </xsl:when>
                            
                                    <xsl:otherwise>
                                        <source-value>
                                            <xsl:variable name="label">
                                                <xsl:value-of select="/csv:xml-csv-file/csv:columns/csv:column[@column-id=$colid]/csv:display-label"/>
                                            </xsl:variable>
                                            <xsl:if test="$label != ''">
                                                <xsl:attribute name="source-definition">
                                                    <xsl:value-of select="$label"/> 
                                                </xsl:attribute>
                                                <xsl:value-of select="."/> 
                                            </xsl:if>
                                        </source-value>
                                    </xsl:otherwise>   
                                      
                                </xsl:choose>  
                
                                            
                            </xsl:for-each>
                        </xsl:if>                                   
                           
                    </xi:signee-item>
                        
                </xsl:for-each>       
                                                        
            </items>
        </xi:xml-import>
        
    </xsl:template>               
    
</xsl:stylesheet>
