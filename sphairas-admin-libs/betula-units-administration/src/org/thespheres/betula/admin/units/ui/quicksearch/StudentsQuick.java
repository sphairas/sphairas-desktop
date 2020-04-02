/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui.quicksearch;

import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.admin.units.RemoteStudent;
import org.thespheres.betula.admin.units.RemoteStudents;

@Messages({"QuickSearch/SuS/org-thespheres-betula-admin-units-ui-quicksearch-StudentsQuick.instance=Schülerin/Schüler"})
public class StudentsQuick implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        for (final RemoteStudent item : RemoteStudents.find(request.getText())) {
            if (!response.addResult((Runnable) () -> item.openInEditor(), item.getHtmlDirectoryName())) {
                break;
            }
        }
    }

}
