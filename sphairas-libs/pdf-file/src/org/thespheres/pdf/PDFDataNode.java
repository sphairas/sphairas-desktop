/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.pdf;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author boris.heithecker
 */
class PDFDataNode extends DataNode {
    
    PDFDataNode(DataObject obj, Children ch, Lookup lookup) {
        super(obj, ch, lookup);
    }
    
    @Override
    public Transferable drag() throws IOException {
        return transfer();
    }
    
    
    private Transferable transfer() {
        FileObject fo = getDataObject().getPrimaryFile();
        File file = FileUtil.toFile(fo);
        final ArrayList<File> list = new ArrayList<>(1);
        list.add(file);
        return new ExTransferable.Single(DataFlavor.javaFileListFlavor) {
            @Override
            public Object getData() {
                return list;
            }
        };
    }
    
}
