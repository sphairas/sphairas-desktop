package org.thespheres.betula.niedersachsen.berichte.ui;

import java.util.Arrays;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.adminreports.TextKursImportItem;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.niedersachsen.Faecher;

/**
 *
 * @author boris.heithecker
 */
public class NdsTextKursImportItem extends TextKursImportItem {

    private Marker kursart;
    private Marker other;
    protected UnitId unit;

    NdsTextKursImportItem(UnitId unit, DocumentId target) {
        super(target);
        this.unit = unit;
    }

    @Override
    public void initializeMarkers(final Marker[] markers) {
        Arrays.stream(markers).forEach(m -> {
            switch (m.getConvention()) {
                case Faecher.CONVENTION_NAME:
                    setFach(m);
                    break;
                case "kgs.schulzweige":
                    setKursart(m);
                    break;
            }
        });
    }

    @Override
    public UnitId getUnit() {
        return unit;
    }

    public Marker getKursart() {
        return kursart;
    }

    public void setKursart(Marker kursart) {
        this.kursart = kursart;
    }

    public Marker getOtherMarker() {
        return other;
    }

    public void setOtherMarker(Marker m) {
        this.other = m;
    }

}
