/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.exceltableimport;

import java.util.Collections;
import java.util.List;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.tableimport.DataImportSettings;
import org.thespheres.betula.tableimport.TableImportConverter;
import org.thespheres.betula.tableimport.TableImportConverterService;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = TableImportConverterService.class)
public class MSExcelTableImportConverterService implements TableImportConverterService {

    @Override
    public List<TableImportConverter> converters(DataImportSettings.Type[] type) {
        return Collections.singletonList(new MSExcelTableImportConverterImpl());
    }

}
