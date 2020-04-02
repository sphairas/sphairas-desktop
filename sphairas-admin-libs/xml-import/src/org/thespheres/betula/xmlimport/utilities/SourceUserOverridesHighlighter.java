/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.utilities;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.table.TableColumnExt;
import org.openide.util.ImageUtilities;
import org.thespheres.betula.xmlimport.ImportTargetsItem;
import org.thespheres.betula.xmlimport.uiutil.ImportTableModel;

/**
 *
 * @author boris.heithecker
 */
public class SourceUserOverridesHighlighter extends PainterHighlighter implements HighlightPredicate, Painter {

    private final AbstractSourceOverrides overrides;

    @SuppressWarnings({"LeakingThisInConstructor",
        "OverridableMethodCallInConstructor"})
    public SourceUserOverridesHighlighter(AbstractSourceOverrides parent) {
        super();
        this.overrides = parent;
        setHighlightPredicate(this);
        setPainter(this);
    }

    @Override
    public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
        int row = adapter.convertRowIndexToModel(adapter.row);
        Object val0 = adapter.getValueAt(row, 0);
        if (adapter.getComponent() instanceof JXTable) {
            JXTable table = (JXTable) adapter.getComponent();
            TableColumnExt col = table.getColumnExt(adapter.column);
            String colId = (String) col.getClientProperty(ImportTableModel.PROP_COLUMN_ID);
            return evaluateItem(val0, colId);
        }
        return false;
    }

    @Override
    public void paint(Graphics2D g, Object object, int width, int height) {
        Image img = ImageUtilities.loadImage("org/thespheres/betula/gpuntis/resources/pencil-small.png", true);
        g.drawImage(img, width - 17, 1, 16, 16, null);
    }

    protected boolean evaluateItem(Object val0, String colId) {
        if (val0 instanceof ImportTargetsItem) {
            ImportTargetsItem il = (ImportTargetsItem) val0;
            switch (colId) {
                case "subject":
                    return overrides.hasSubjectOverride(il);
                case "unit":
                    return overrides.hasUnitOverride(il);
                case "documentBase":
                    return overrides.hasDocumentBaseOverride(il);
                case "signee":
                    return overrides.hasSigneeOverride(il);
                case "deleteDate":
                    return overrides.hasDeleteDateOverride(il);
                case "targetId":
                    return overrides.hasTargetIdOverride(il);
            }
            return overrides.hasColumnOverride(il, colId);
        }
        return false;
    }
}
