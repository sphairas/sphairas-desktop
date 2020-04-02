/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.bemerkungen;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.util.Exceptions;
import org.thespheres.betula.niedersachsen.admin.ui.bemerkungen.ReportNotesArguments.Argument;

/**
 *
 * @author boris.heithecker
 */
class ArgumentsAction extends AbstractAction {

    private static JAXBContext JAXB;

    private synchronized static JAXBContext getJAXB() {
        if (JAXB == null) {
            try {
                JAXB = JAXBContext.newInstance(ReportNotesArguments.class);
            } catch (JAXBException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return JAXB;
    }

    static Action[] createActions(final JTextComponent attachTo) throws IOException {
        final ReportNotesArguments rna;
        try (final InputStream is = ReportNotesArguments.class.getResourceAsStream("/org/thespheres/betula/niedersachsen/admin/ui/resources/arguments.xml")) {
            rna = (ReportNotesArguments) getJAXB().createUnmarshaller().unmarshal(is);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
        return Arrays.stream(rna.getArguments())
                .map(ArgumentsAction::new)
                .peek(a -> a.attachTo(attachTo))
                .toArray(Action[]::new);
    }
    private final Argument argument;
    private JTextComponent targetComponent;

    private ArgumentsAction(Argument arg) {
        super(arg.getDisplayName() != null ? arg.getDisplayName() : arg.getName());
        this.argument = arg;
    }    

    private void attachTo(JTextComponent attachTo) {
        this.targetComponent = attachTo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (targetComponent != null) {
            final int pos = targetComponent.getCaretPosition();
            final String insert;
            if (argument.getInsertFormatParameter() != null) {
                insert = argument.getInsertFormatParameter();
            } else {
                insert = "{" + Integer.toString(argument.getIndex()) + "}";
            }
            try {
                targetComponent.getDocument().insertString(pos, insert, null);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

}
