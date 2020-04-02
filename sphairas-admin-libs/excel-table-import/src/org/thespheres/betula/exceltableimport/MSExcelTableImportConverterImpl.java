/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.exceltableimport;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openide.util.NbBundle;
import org.thespheres.betula.tableimport.TableImportConverter;
import org.thespheres.betula.tableimport.csv.XmlCsvFile;
import org.thespheres.betula.tableimport.csv.XmlCsvFile.Column;
import org.thespheres.betula.tableimport.csv.XmlCsvFile.Line;
import org.thespheres.betula.tableimport.csv.XmlCsvFile.Value;
import org.thespheres.betula.util.CollectionUtil;
import org.thespheres.betula.util.Utilities;

/**
 *
 * @author boris.heithecker
 */
class MSExcelTableImportConverterImpl extends TableImportConverter {

    private static final int MINIMUM_COLUMN_COUNT = 2;
    private final DataFormatter formatter = new DataFormatter();

    MSExcelTableImportConverterImpl() {
        super(MSExcelFileDataObject.MIME, NbBundle.getMessage(MSExcelFileDataObject.class, "MSExcelFileDataObject.displayName"));
    }

    @Override
    public XmlCsvFile[] load(final File file, final String igonred) throws IOException {
        final Workbook wb;
        try {
            wb = WorkbookFactory.create(file);
        } catch (InvalidFormatException | EncryptedDocumentException ex) {
            throw new IOException(ex);
        }
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(wb.sheetIterator(), Spliterator.ORDERED), false)
                .map(this::sheetToCsv)
                .filter(Objects::nonNull)
                .toArray(XmlCsvFile[]::new);
    }

    private XmlCsvFile sheetToCsv(final Sheet sh) {
        // Decide which rows to process
        int rowStart = Math.min(15, sh.getFirstRowNum());
        int rowEnd = Math.max(2000, sh.getLastRowNum());

        final Column[] columns = IntStream.rangeClosed(rowStart, rowEnd)
                .mapToObj(r -> sh.getRow(r))
                .filter(Objects::nonNull)
                .findFirst()
                .map(this::createColumns)
                .orElse(null);

        if (columns == null) {
            return null;
        }

        final Line[] lines = IntStream.rangeClosed(rowStart, rowEnd)
                .skip(1l)
                .mapToObj(r -> sh.getRow(r))
                .filter(Objects::nonNull)
                .map(r -> createLine(r, columns))
                .toArray(Line[]::new);

        if (lines.length == 0) {
            return null;
        }

        return new XmlCsvFile(columns, lines);
    }

    private Column[] createColumns(final Row r) {
        int lastColumn = Math.max(r.getLastCellNum(), MINIMUM_COLUMN_COUNT);
        return IntStream.rangeClosed(0, lastColumn)
                .mapToObj(cn -> {
                    final Cell cell = r.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell != null && cell.getCellTypeEnum() == CellType.STRING) {
                        final String text = cell.getRichStringCellValue().getString();
                        final String label = StringUtils.trimToNull(text);
                        if (label != null) {
                            final String id = Utilities.createId(cn);
                            return new Column(id, label);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toArray(Column[]::new);
    }

    private Line createLine(final Row r, final Column[] columns) {
        int lastColumn = Math.max(r.getLastCellNum(), MINIMUM_COLUMN_COUNT);
        final Value[] val = IntStream.rangeClosed(0, lastColumn)
                .mapToObj(cn -> {
                    final Cell cell = r.getCell(cn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    if (cell != null) {
                        // The spreadsheet is not empty in this cell
                        final String id = Utilities.createId(cn);
                        final Column column = Arrays.stream(columns)
                                .filter(c -> c.getId().equals(id))
                                .collect(CollectionUtil.requireSingleOrNull());
                        if (column != null) {
                            //                    CellReference cellRef = new CellReference(r.getRowNum(), cell.getColumnIndex());
//                    System.out.print(cellRef.formatAsString());
//                    System.out.print(" - ");
// get the text that appears in the cell by getting the cell value and applying any data formats (Date, 0.00, 1.23e9, $1.23, etc)
                            String text = formatter.formatCellValue(cell);
//                    System.out.println(text);

//                    // Alternatively, get the value and format it yourself
//                    switch (cell.getCellTypeEnum()) {
//                        case STRING:
//                            System.out.println(cell.getRichStringCellValue().getString());
//                            break;
//                        case NUMERIC:
//                            if (DateUtil.isCellDateFormatted(cell)) {
//                                System.out.println(cell.getDateCellValue());
//                            } else {
//                                System.out.println(cell.getNumericCellValue());
//                            }
//                            break;
//                        case BOOLEAN:
//                            System.out.println(cell.getBooleanCellValue());
//                            break;
//                        case FORMULA:
//                            System.out.println(cell.getCellFormula());
//                            break;
//                        case BLANK:
//                            System.out.println();
//                            break;
//                        default:
//                            System.out.println();
//                    }
                            return new Value(column, text);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toArray(Value[]::new);
        return new Line(val);
    }
}
