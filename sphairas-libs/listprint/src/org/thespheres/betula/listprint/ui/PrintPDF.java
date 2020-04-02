/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.listprint.ui;

import org.thespheres.betula.listprint.Formatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.thespheres.betula.listprint.PDFFactory;
import org.thespheres.betula.listprint.XSLFOException;

@ActionID(
        category = "Betula",
        id = "org.thespheres.betula.listprint.ui.PrintPDF"
)
@ActionRegistration(
        displayName = "#PrintPDF.displayName",
        iconBase = "org/thespheres/betula/listprint/resources/pdf_exports.png",
        asynchronous = true)
@ActionReference(path = "Menu/File", position = 2300)
@Messages("PrintPDF.displayName=Als pdf-Datei drucken")
public final class PrintPDF implements ActionListener {

    private final List<PDFFactory> context;

    public PrintPDF(List<PDFFactory> context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.stream().forEach(f -> {
            try {
                Formatter.getDefault().transform(f.createRoot(), f.getOutputStream(null), "application/pdf");
                f.success();
            } catch (IOException | XSLFOException ex) {
                f.failure(ex);
            }
        });
    }
}
