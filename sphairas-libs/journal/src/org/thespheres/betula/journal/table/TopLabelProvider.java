/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.journal.table;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import org.jdesktop.swingx.renderer.LabelProvider;
import org.jdesktop.swingx.renderer.StringValue;

/**
 *
 * @author boris.heithecker
 */
public class TopLabelProvider extends LabelProvider {

    public TopLabelProvider() {
    }

    public TopLabelProvider(StringValue converter) {
        super(converter);
    }

    public TopLabelProvider(int alignment) {
        super(alignment);
    }

    public TopLabelProvider(StringValue converter, int alignment) {
        super(converter, alignment);
    }

    @Override
    protected JLabel createRendererComponent() {
        JLabel ret = super.createRendererComponent();
        ret.setVerticalTextPosition(SwingConstants.TOP);
        return ret;
    }

}
