/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.acer.remote.ui.editor;

import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.thespheres.acer.remote.ui.AbstractMessage;

/**
 *
 * @author boris.heithecker
 */
public class MessagesEditor extends CloneableEditor {

    private AbstractMessage message;

    public MessagesEditor() {
    }

    public MessagesEditor(AbstractMessage msg) {
        super(msg.getMessageEditorSupport(), true);
        this.message = msg;
        activatedNodes();
    }

    private void activatedNodes() {
        if (message != null) {
            setActivatedNodes(new Node[]{message.getNodeDelegate()});
        }
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

//    @Override
//    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
//        super.readExternal(oi);
//        Object o = oi.readObject();
//        if (o instanceof BeansConfigurationState) {
//            beansConfig = (BeansConfigurationState) o;
//            initializeComponent();
//            //        activatedNodes();
//        } else {
//            throw new IOException();
//        }
//    }
//
//    @Override
//    public void writeExternal(ObjectOutput oo) throws IOException {
//        super.writeExternal(oo);
//        message.getMessageEditorSupport().
//    }

}
