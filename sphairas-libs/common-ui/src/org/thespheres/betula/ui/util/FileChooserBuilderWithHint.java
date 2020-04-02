package org.thespheres.betula.ui.util;

import java.io.File;
import javax.swing.JFileChooser;
import org.openide.filesystems.FileChooserBuilder;

/**
 *
 * @author boris.heithecker
 */
public class FileChooserBuilderWithHint extends FileChooserBuilder {

    private final String hint;

    public FileChooserBuilderWithHint(Class<?> clz, String hint) {
        super(clz);
        this.hint = hint;
    }

    @Override
    public JFileChooser createFileChooser() {
        final JFileChooser ret = super.createFileChooser();
        final File selected = ret.getCurrentDirectory();
        if (selected != null && selected.isDirectory()) {
            final File withHint = new File(selected, hint);
            ret.setSelectedFile(withHint);
        }
        return ret;
    }

}
