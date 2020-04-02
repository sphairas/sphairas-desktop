/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.tableimport.csv;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "xml-csv-parser-settings") //, namespace = "http://www.thespheres.org/xsd/betula/csv-import.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlCsvParserSettings {

    @XmlAttribute(name = "separator-char")
    private int separatorChar;
    @XmlAttribute(name = "quote-char")
    private int quoteChar;

//    public char getSeparatorChar() {
//        return separatorChar != 0 ? (char) separatorChar : CSVParser.DEFAULT_SEPARATOR;
//    }
//
//    public void setSeparatorChar(char separatorChar) {
//        this.separatorChar = (int) separatorChar;
//    }
//
//    public char getQuoteChar() {
//        return quoteChar != 0 ? (char) quoteChar : CSVParser.DEFAULT_QUOTE_CHARACTER;
//    }
    public void setQuoteChar(char quoteChar) {
        this.quoteChar = (int) quoteChar;
    }

    public CsvParser createCSVParser() {
        final CsvParserSettings settings = new CsvParserSettings();
        settings.detectFormatAutomatically();
        return new CsvParser(settings);
    }
}
