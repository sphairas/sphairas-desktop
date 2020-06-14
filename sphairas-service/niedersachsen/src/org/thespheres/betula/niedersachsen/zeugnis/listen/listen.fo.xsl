<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master master-name="master0" page-width="29.7cm" page-height="21.0cm" margin-top="2.0cm" margin-bottom="1.0cm" margin-left="2.0cm" margin-right="1.7cm">
                    <fo:region-body region-name="body0"/>
                    <fo:region-after region-name="footer0" extent="0.5cm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="master0" language="de">
                <fo:static-content flow-name="footer0">
                    <fo:block margin-left="0.0cm" margin-right="0.0cm" margin-top="0.0cm" font-size="7pt" font-family="SansSerif" color="#000000" text-align="center">
                        <xsl:value-of select="student-list-collection/footer/center"/>
                    </fo:block>
                </fo:static-content>
                <fo:flow flow-name="body0">
                    <xsl:if test="student-list-collection/list">
                        <xsl:for-each select="student-list-collection/list">
                            <fo:block break-before="page" font-size="12pt" font-family="SansSerif" font-weight="bold" space-after="0.1cm">
                                <xsl:value-of select="list-data/list-name"/>
                            </fo:block>
                            <fo:block text-align-last="justify" line-height="2pt" space-after="0.1cm">
                                <fo:leader leader-pattern="rule" color="#ff9a33" rule-thickness="2.0pt" rule-style="solid"/>
                            </fo:block>
                            <xsl:if test="data">
                                <fo:table width="26.0cm">
                                    <fo:table-column column-width="5.0cm">
                                        <xsl:if test="@first-column-width">
                                            <xsl:attribute name="column-width">
                                                <xsl:value-of select="@first-column-width"/>
                                            </xsl:attribute>     
                                        </xsl:if>   
                                    </fo:table-column>
                                    <xsl:for-each select="data-header/subject">
                                        <fo:table-column column-width="0.7cm"/>
                                    </xsl:for-each>
                                    <fo:table-column/>
                                    <fo:table-header>
                                        <fo:table-row height="2.2cm">
                                            <fo:table-cell number-columns-spanned="1" display-align="after">
                                                <fo:block margin-left="0.0cm" margin-right="22.0cm" margin-top="0.0cm" font-size="10pt" font-family="SansSerif" color="#000000" text-align="left" padding-left="2pt">Name</fo:block>
                                            </fo:table-cell>
                                            <xsl:for-each select="data-header/subject">
                                                <xsl:sort select="@tier" data-type="number" order="ascending"/>
                                                <xsl:sort select="@order" data-type="number" order="ascending"/>
                                                <fo:table-cell number-columns-spanned="1" display-align="after" line-height="0.85">
                                                    <!--width muss! gesetzt sein wie oben height!-->
                                                    <fo:block-container reference-orientation="90" font-family="SansSerif" display-align="center"  hyphenate="true" hyphenation-remain-character-count="8" width="2.2cm">                                                                                                 
                                                        <fo:block padding-before="1pt" padding-after="2pt" >
                                                            <xsl:attribute name="font-size">
                                                                <xsl:value-of select="@font-size"/>
                                                            </xsl:attribute>     
                                                            <!--                                                            <xsl:if test="@keep-together-within-line">
                                                                <xsl:attribute name="keep-together.within-line">
                                                                    <xsl:value-of select="@keep-together-within-line"/>
                                                                </xsl:attribute>     
                                                            </xsl:if>     -->
                                                            <xsl:if test="@subject-level">
                                                                <fo:inline font-size="5" font-style="italic" space-end="0.5pt" baseline-shift="super">
                                                                    <xsl:value-of select="@subject-level"/>
                                                                </fo:inline>
                                                            </xsl:if>                                                                                            
                                                            <xsl:value-of select="normalize-space(.)"/>                                                         
                                                        </fo:block>          
                                                    </fo:block-container>
                                                </fo:table-cell>
                                            </xsl:for-each>
                                        </fo:table-row>
                                        <fo:table-row background-color="#ffffff" height="0.3cm">
                                            <fo:table-cell>
                                                <xsl:attribute name="number-columns-spanned">
                                                    <xsl:number value="count(data-header/subject) + 1"/>
                                                </xsl:attribute>
                                                <fo:block text-align-last="justify" line-height="1pt" space-after="0.1cm">
                                                    <fo:leader leader-pattern="rule" color="#ff9a33" rule-thickness="1.0pt" rule-style="solid"/>
                                                </fo:block>
                                            </fo:table-cell>
                                        </fo:table-row>-
                                    </fo:table-header>
                                    <fo:table-body>
                                        <xsl:for-each select="data">
                                            <fo:table-row background-color="#ffffff" height="0.5cm" display-align="after" >
                                                <xsl:if test="(position() mod 2) = 0">
                                                    <xsl:attribute name="background-color">#FFE0C0</xsl:attribute>
                                                </xsl:if>
                                                <fo:table-cell number-columns-spanned="1">
                                                    <fo:block margin-left="0.0cm" margin-right="0.0cm" margin-top="0.0cm" font-size="10pt" font-family="SansSerif" color="#000000" text-align="left" padding-left="2pt">
                                                        <xsl:value-of select="student-name"/>
                                                        <xsl:if test="@student-hint">  
                                                            <fo:inline font-size="5" font-style="italic" baseline-shift="super">
                                                                <xsl:value-of select="@student-hint"/>
                                                            </fo:inline>  
                                                        </xsl:if>     
                                                    </fo:block>
                                                </fo:table-cell>
                                                <xsl:for-each select="subject-value">
                                                    <xsl:sort select="@tier" data-type="number" order="ascending"/>
                                                    <xsl:sort select="@order" data-type="number" order="ascending"/>
                                                    <fo:table-cell number-columns-spanned="1">
                                                        <fo:block  font-size="10pt" font-family="SansSerif" text-align="center">                                                            
                                                            <xsl:choose>
                                                                <xsl:when test="@color">
                                                                    <xsl:attribute name="color">
                                                                        <xsl:value-of select="@color"/>
                                                                    </xsl:attribute>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:attribute name="color">#000000</xsl:attribute>
                                                                </xsl:otherwise>
                                                            </xsl:choose>                                                            
                                                            <xsl:if test="@subject-level">
                                                                <fo:inline font-size="7" space-end="0.5pt" baseline-shift="super" color="#000000" >
                                                                    <xsl:value-of select="@subject-level"/>
                                                                </fo:inline>
                                                            </xsl:if>                                                     
                                                            <xsl:value-of select="normalize-space(.)"/>      
                                                            <xsl:if test="@label">
                                                                <fo:inline font-size="5" font-style="italic" space-start="0.5pt" baseline-shift="super" color="#000000" >
                                                                    <xsl:value-of select="@label"/>
                                                                </fo:inline>
                                                            </xsl:if>                          
                                                        </fo:block>
                                                    </fo:table-cell>
                                                </xsl:for-each>
                                                <fo:table-cell>
                                                    <fo:block font-size="10pt" font-family="SansSerif" color="#000000" text-align="left">
                                                        <xsl:choose>
                                                            <xsl:when test="note/@font-size">
                                                                <xsl:attribute name="font-size">
                                                                    <xsl:value-of select="note/@font-size"/>
                                                                </xsl:attribute>
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                                <xsl:attribute name="font-size">10pt</xsl:attribute>
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                        <xsl:value-of select="note"/>
                                                    </fo:block>
                                                </fo:table-cell>
                                            </fo:table-row>
                                        </xsl:for-each>
                                    </fo:table-body>
                                </fo:table>
                                <xsl:if test="footnote">
                                    <xsl:for-each select="footnote">
                                        <fo:block font-size="7pt" font-family="SansSerif" space-before="1pt">
                                            <fo:inline font-size="5" space-end="0.5pt" baseline-shift="super">
                                                <xsl:value-of select="@index"/>
                                            </fo:inline>
                                            <xsl:value-of select="normalize-space(.)"/>   
                                        </fo:block>
                                    </xsl:for-each>
                                </xsl:if>                                                                                                                                                                                                                    
                                <fo:block font-size="7pt" font-family="SansSerif" text-align="right" space-before="2pt" space-after="0.1cm">
                                    <xsl:value-of select="list-data/list-version"/>
                                </fo:block>
                            </xsl:if>
                            <fo:block text-align-last="justify" line-height="2pt" space-after="0.1cm">
                                <fo:leader leader-pattern="rule" color="#ff9a33" rule-thickness="2.0pt" rule-style="solid"/>
                            </fo:block>
                        </xsl:for-each>
                    </xsl:if>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>
