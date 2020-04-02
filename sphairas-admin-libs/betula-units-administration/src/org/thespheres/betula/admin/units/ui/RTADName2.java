/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.admin.units.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.Collator;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.openide.util.NbBundle;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocument;
import org.thespheres.betula.admin.units.RemoteTargetAssessmentDocumentName;
import org.thespheres.betula.document.Marker;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.model.MultiSubject;
import org.thespheres.betula.services.NamingResolver;
import org.thespheres.betula.services.scheme.spi.Term;
import org.thespheres.betula.services.util.Signees;

/**
 *
 * @author boris.heithecker
 */
public class RTADName2 implements PropertyChangeListener, RemoteTargetAssessmentDocumentName {

    final RemoteTargetAssessmentDocument rtad;
    private String dn;
    private String dn_short;
    private final static Collator COLLATOR = Collator.getInstance(Locale.GERMAN);
    private String searchable;
    private final PropertyChangeSupport pSupport = new PropertyChangeSupport(this);

    @SuppressWarnings("LeakingThisInConstructor")
    public RTADName2(RemoteTargetAssessmentDocument rtad) {
        this.rtad = rtad;
        this.rtad.addPropertyChangeListener(this);
    }

    @Override
    public String getColumnLabel() {
        if (dn_short == null) {
            final NamingResolver.Result nr = rtad.getNamingResolverResult();
            if (nr != null) {
                dn_short = nr.getResolvedElement("fach.kurz");
                final String dnid = nr.getResolvedElement("kurs_id");
                if (dnid != null) {
                    dn_short = dn_short + " " + dnid;
                } else if (nr.hasResolverHint(NamingResolver.Result.HINT_STATIC_NAME)) {
                    dn_short = nr.getResolvedName();
                }
                if (dn_short == null) {
                    dn_short = nr.getResolvedName();
                }
            }
            if (dn_short == null) {
                dn_short = rtad.getDocumentId().getId();
            }
            rtad.getUniqueMarker("kgs.unterricht")
                    .map(Marker::getShortLabel)
                    .ifPresent(s -> dn_short += "*");
        }
        return dn_short;
    }

    @Override
    public String getDisplayName(Term current) {
        if (dn == null) {
            final NamingResolver.Result nr = rtad.getNamingResolverResult();
            if (nr != null) {
                dn = current != null ? nr.getResolvedName(current) : nr.getResolvedName();
            }
            if (dn == null) {
                dn = rtad.getDocumentId().getId();
            }
        }
        return dn;
    }

    @Override
    public String getToolTipText() {
        return rtad.getDocumentId().getId();
    }

    @Override
    public String getSearchableString(Term current) {
        if (searchable == null) {
            final Signee signee = rtad.getSignee("entitled.signee");
            final StringJoiner sj = new StringJoiner(" ");
            //Term
            sj.add(getDisplayName(current));
            //Subject & realm
            final MultiSubject ms = rtad.getMultiSubject();
            if (ms != null) {
                String sub = ms.getSubjectMarkerSet().stream()
                        .map(m -> m.getLongLabel())
                        .collect(Collectors.joining(","));
                if (ms.getRealmMarker() != null) {
                    sub += "[" + ms.getRealmMarker().getLongLabel() + "]";
                }
                final String message = NbBundle.getMessage(StudentValuesToolTipHighlighter.class, "StudentValuesToolTipHighlighter.subject", sub);
                sj.add(message);
            }
            //Signee
            if (signee != null) {
                final String sn = Signees.get(rtad.getProvider()).map(s -> s.getSignee(signee))
                        .orElse(signee.getId());
                sj.add(sn);
            }
            //Doc id
            sj.add(rtad.getDocumentId().getId());
            searchable = sj.toString();
        }
        return searchable;
    }

    @Override
    public int compareTo(RemoteTargetAssessmentDocumentName o) {
        //Zwei ColumnLabel können gleich sein, z.B. mit vorjahreskurs etc.
        return COLLATOR.compare(getColumnLabel() + rtad.getDocumentId().getId(), o.getColumnLabel() + rtad.getDocumentId().getId());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (RemoteTargetAssessmentDocument.PROP_SIGNEES.equals(evt.getPropertyName())) {
            String old = getSearchableString(null);
            synchronized (this) {
                searchable = null;
            }
            pSupport.firePropertyChange(PROP_SEARCHEABLE_STRING, old, getSearchableString(null));
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pSupport.removePropertyChangeListener(listener);
    }

}
