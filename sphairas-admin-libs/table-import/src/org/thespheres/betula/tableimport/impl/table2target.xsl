<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
                xmlns:bt="http://www.thespheres.org/xsd/betula/betula.xsd"
                xmlns:csv="http://www.thespheres.org/xsd/betula/csv-import.xsd"
                exclude-result-prefixes="">
    
    <xsl:output indent = "yes" encoding = "UTF-8" omit-xml-declaration = "no"/>
    <xsl:param name="authority" />
    <xsl:param name="student.authority" />
    <xsl:param name="entries" />
    
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
                
                <xsl:variable name="current" select="/csv:xml-csv-file/csv:lines"/>
               
                <xsl:choose>
                        
                    <xsl:when test="/csv:xml-csv-file/csv:grouping-keys/csv:grouping-key">
                        
                        <xsl:comment>"Using keys..."</xsl:comment>           
                         
                        <xsl:for-each select="/csv:xml-csv-file/csv:grouping-keys/csv:grouping-key">                            
                            
                            <xsl:variable name="key" select="@grouping-id"/>
                            
                            <xsl:comment>
                                <xsl:value-of select="concat('Group: ', current())" />
                            </xsl:comment>                               
                            
                            <xi:target-item>
                                
                                <xsl:attribute name="position"> 
                                    <xsl:value-of select="position()"/> 
                                </xsl:attribute>
                                                                                               
                                <xsl:element name="label">
                                    <xsl:value-of select="."/> 
                                </xsl:element>   
                                 
                                <xsl:for-each select="csv:key-value">
                                    <xsl:variable name="elname">
                                        <xsl:value-of select="@assigned-key"/>
                                    </xsl:variable>
                                    <xsl:if test="$elname != ''">
                                        
                                        <xsl:if test="$elname != 'unit.id' and $elname != 'marker'">
                                            <xsl:element name="{$elname}">
                                                <xsl:value-of select="."/> 
                                            </xsl:element>             
                                        </xsl:if>  
                                        
                                        <xsl:if test="$elname = 'unit.id'">
                                            
                                            <xsl:comment>"Unit"</xsl:comment>     
                                             
                                            <unit xmlns="http://www.thespheres.org/xsd/betula/betula.xsd">
                                                <xsl:attribute name="id"> 
                                                    <xsl:value-of select="."/> 
                                                </xsl:attribute>
                                                <xsl:attribute name="authority"> 
                                                    <xsl:value-of select="$authority"/> 
                                                </xsl:attribute>
                                            </unit>
                                        </xsl:if>                                          
                        
                                    </xsl:if>      
                                </xsl:for-each>
                                
                                <xsl:if test="csv:key-value[@assigned-key='marker']">
                                    
                                    <xsl:comment>"Source markers"</xsl:comment> 
                                    
                                    <source-markers>
                                        <xsl:for-each select="csv:key-value[@assigned-key='marker']">
                                            
                                            <marker>
                                                <xsl:value-of select="."/>
                                            </marker>                                        
                                            
                                        </xsl:for-each>                                        
                                    </source-markers>

                                </xsl:if>
                                
                                <xsl:choose>
                                    <xsl:when test="$entries = 'true'">
                                        <xsl:call-template name="students-entries">
                                            <xsl:with-param name="current" select="$current"/>
                                            <xsl:with-param name="key" select="$key" />
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:call-template name="students">
                                            <xsl:with-param name="current" select="$current"/>
                                            <xsl:with-param name="key" select="$key" />
                                        </xsl:call-template>
                                    </xsl:otherwise>
                                </xsl:choose>
                                
                            </xi:target-item>
                        
                        </xsl:for-each>
                            
                    </xsl:when> 
                       
                    <xsl:otherwise>
                            
                        <xsl:for-each select="/csv:xml-csv-file/csv:lines/csv:line">
                            <xi:target-item>
                            
                                <xsl:attribute name="position"> 
                                    <xsl:value-of select="position()"/> 
                                </xsl:attribute>
                            
                                <xsl:if test="csv:value">
                                    <xsl:for-each select="csv:value">
                                        <xsl:variable name="colid" select="@column-id"/>
                                        <xsl:variable name="elname">
                                            <xsl:value-of select="/csv:xml-csv-file/csv:columns/csv:column[@column-id=$colid]/@assigned-key"/>
                                        </xsl:variable>
                                        <xsl:if test="$elname != ''">
                                            <!--<xsl:element name="{$elname} and $elname != 'student.id">-->
                                            <xsl:element name="{$elname}">
                                                <xsl:value-of select="."/> 
                                            </xsl:element>
                                        </xsl:if>                      
                                    </xsl:for-each>
                                </xsl:if>                                   
                           
                            </xi:target-item>
                        </xsl:for-each>
                            
                    </xsl:otherwise>
                        
                </xsl:choose>           
                                                        
            </items>
        </xi:xml-import>
        
    </xsl:template>
    
    <xsl:template name="students-entries" match="/csv:xml-csv-file/csv:lines">
        <xsl:param name="key" />
        <xsl:param name="current" />         
            
        <xsl:if test="$current/csv:line[@grouping-key=$key]">
            <entries>
        
                <xsl:for-each select="$current/csv:line[@grouping-key=$key]">
               
                    <xi:target-entry-item xmlns:xi="http://www.thespheres.org/xsd/betula/xml-import.xsd">
                        
                        <xsl:apply-templates select=".">                    
                        </xsl:apply-templates>
                    
                    </xi:target-entry-item>
                
                </xsl:for-each>
            
            </entries>        
        </xsl:if>
                   
    </xsl:template>
    
    <xsl:template name="students" match="/csv:xml-csv-file/csv:lines">
        <xsl:param name="key" />
        <xsl:param name="current" />         
            
        <xsl:if test="$current/csv:line[@grouping-key=$key]">
            <students>
        
                <xsl:for-each select="$current/csv:line[@grouping-key=$key]">
               
                    <xi:student-item xmlns:xi="http://www.thespheres.org/xsd/betula/xml-import.xsd">
                        
                        <xsl:apply-templates select=".">
                    
                        </xsl:apply-templates>
                    
                    </xi:student-item>
                
                </xsl:for-each>
            
            </students>        
        </xsl:if>
                   
    </xsl:template>
    
    <xsl:template name="student" match="/csv:xml-csv-file/csv:lines/csv:line">
                    
        <xsl:if test="csv:value">                    
            <xsl:for-each select="csv:value">
                <xsl:variable name="colid" select="@column-id"/>
                <xsl:variable name="elname">
                    <xsl:value-of select="/csv:xml-csv-file/csv:columns/csv:column[@column-id=$colid]/@assigned-key"/>
                </xsl:variable>
                <xsl:choose>
                            
                    <xsl:when test="$elname != ''">
                        <!--<xsl:if test="$elname != 'source-unit' and $elname != 'unit.id' and $elname != 'student.id'">-->
                        <xsl:if test="$elname != 'student.id'">
                            <xsl:element name="{$elname}">
                                <xsl:value-of select="."/> 
                            </xsl:element>
                        </xsl:if>   
                    
                        <xsl:if test="$elname = 'student.id' and number(.) = . and floor(.) = .">
                            <student xmlns="http://www.thespheres.org/xsd/betula/betula.xsd">
                                <xsl:attribute name="id"> 
                                    <xsl:value-of select="."/> 
                                </xsl:attribute>
                                <xsl:attribute name="authority"> 
                                    <xsl:value-of select="$student.authority"/> 
                                </xsl:attribute>
                            </student>
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
                   
    </xsl:template>
</xsl:stylesheet>
