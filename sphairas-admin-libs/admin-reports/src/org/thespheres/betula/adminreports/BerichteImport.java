package org.thespheres.betula.adminreports;

import org.openide.util.Lookup;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.services.ProviderInfo;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.ws.WebServiceProvider;
import org.thespheres.betula.xmlimport.utilities.ConfigurableImportTarget;

/**
 *
 * @author boris.heithecker
 * @param <I>
 */
public interface BerichteImport<I extends TextKursImportItem> { //extends ImportTarget

    public static BerichteImport find() {
        return Lookup.getDefault().lookup(BerichteImport.class);//TODO: identifier....
    }

    public ProviderInfo getProvider();

    public I createTextKurs(UnitId unit, DocumentId target);

    public TextKursUpdater<I> createUpdater(I[] kurse, WebServiceProvider provider, Term current, ConfigurableImportTarget cit);

    public String getBerichteSuffix();

}
