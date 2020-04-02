/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.reports.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;
import org.thespheres.betula.reports.ReportFoldManager;

/**
 *
 * @author boris.heithecker
 */
public class ReportsEditorSupport extends DataEditorSupport implements GuardedEditorSupport, OpenCookie, EditCookie, EditorCookie.Observable, PrintCookie, CloseCookie, SaveAsCapable, LineCookie {

    private final SaveCookie saver = new Saver();
    private final CookieSet set;
    private SectionsProvider guardedProvider;
    private StyledDocument docLoadingSaving;

    ReportsEditorSupport(ReportsDataObject rdob, MultiDataObject.Entry entry, CookieSet set) {
        super(rdob, rdob.getLookup(), new Environment(rdob, entry));
        this.set = set;
    }

    @Override
    protected boolean asynchronousOpen() {
        return true;
    }

    @Override
    protected Pane createPane() {
        //Multiview:: create pane here
        return super.createPane();
    }

    @Override
    protected boolean notifyModified() {
        if (super.notifyModified()) {
            addSaveCookie();
            return true;
        }
        return false;
    }

    @Override
    protected void notifyUnmodified() {
        super.notifyUnmodified();
        unsetSaver(true);
    }

    private void addSaveCookie() {
        DataObject obj = getDataObject();
        if (obj.getLookup().lookup(SaveCookie.class) == null) {
            set.add(saver);
            obj.setModified(true);
        }
    }

    private void unsetSaver(boolean on) {
        DataObject obj = getDataObject();
        Node.Cookie cookie = obj.getLookup().lookup(SaveCookie.class);

        if (cookie != null && cookie.equals(saver)) {
            set.remove(saver);
            if (on) {
                obj.setModified(false);
            }
        }
    }

    @Override
    public StyledDocument getDocument() {
        if (docLoadingSaving != null) {
            return docLoadingSaving;
        }
        return super.getDocument();
    }

    @Override
    protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        if (guardedProvider == null) {
            String mimeType = ((CloneableEditorSupport.Env) this.env).getMimeType();
            guardedProvider = (SectionsProvider) GuardedSectionsFactory.find(mimeType).create(this);
        }

        // load content to kit
        if (guardedProvider != null) {
            docLoadingSaving = doc;
            Charset cs = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            try (Reader reader = guardedProvider.createGuardedReader(stream, cs)) {
                kit.read(reader, doc, 0);
            }
        } else {
            kit.read(stream, doc, 0);
        }
        docLoadingSaving = null;
    }

    @Override
    protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
        if (guardedProvider != null) {
            docLoadingSaving = doc;
            Charset cs = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
            try (Writer writer = guardedProvider.createGuardedWriter(stream, cs)) {
                kit.write(writer, doc, 0, doc.getLength());
            }
        } else {
            kit.write(stream, doc, 0, doc.getLength());
        }
        docLoadingSaving = null;
    }

    private static class Environment extends DataEditorSupport.Env {

        private static final long serialVersionUID = 1L;

        private final MultiDataObject.Entry entry;

        public Environment(DataObject obj, MultiDataObject.Entry entry) {
            super(obj);
            this.entry = entry;
        }

        @Override
        protected FileObject getFile() {
            return entry.getFile();
        }

        @Override
        protected FileLock takeLock() throws IOException {
            return entry.takeLock();
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return getDataObject().getLookup().lookup(ReportsEditorSupport.class);
        }

    }

    private class Saver implements SaveCookie {

        @Override
        public void save() throws IOException {
            ReportsEditorSupport.this.saveDocument();
        }

        @Override
        public String toString() {
            return getDataObject().getPrimaryFile().getNameExt();
        }
    }

    @MimeRegistration(mimeType = "text/betula-reports+xml", service = FoldManagerFactory.class)
    public static class Factory implements FoldManagerFactory {

        @Override
        public FoldManager createFoldManager() {
            return new ReportFoldManager();
        }
    }
}
