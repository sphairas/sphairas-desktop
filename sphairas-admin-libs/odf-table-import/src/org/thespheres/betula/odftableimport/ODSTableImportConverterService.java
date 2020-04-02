/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.odftableimport;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;
import org.thespheres.betula.tableimport.DataImportSettings;
import org.thespheres.betula.tableimport.TableImportConverter;
import org.thespheres.betula.tableimport.TableImportConverterService;

/**
 *
 * @author boris.heithecker
 */
@ServiceProvider(service = TableImportConverterService.class)
public class ODSTableImportConverterService implements TableImportConverterService {

    private static ODSTableImportConverterImpl INSTANCE;

    @Override
    public List<TableImportConverter> converters(DataImportSettings.Type[] type) {
        if (INSTANCE == null) {
            try {
                INSTANCE = new ODSTableImportConverterImpl();
            } catch (Exception ex) {
                Logger.getLogger(ODSTableImportConverterImpl.class.getCanonicalName()).log(Level.SEVERE, "Cannot create instance of ODSTableImportConverterImpl.", ex);
            }
        }
        return Collections.singletonList(INSTANCE);
    }

}
