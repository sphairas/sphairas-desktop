/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.document.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.thespheres.betula.document.Document.SigneeInfo;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.document.Timestamp;

/**
 *
 * @author boris.heithecker
 */
public class AbstractCreationInfo implements SigneeInfo, Externalizable {

    private Signee creator;
    private Timestamp when;

    public AbstractCreationInfo() {
    }

    public AbstractCreationInfo(SigneeInfo original) {
        this.creator = original.getSignee();
        this.when = original.getTimestamp();
    }

    @Override
    public Signee getSignee() {
        return creator;
    }

    @Override
    public Timestamp getTimestamp() {
        return when;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        boolean hasCreator = creator != null;
        out.writeBoolean(hasCreator);
        if (hasCreator) {
            out.writeObject(creator);
        }
        long ts = when != null ? when.getDate().getTime() : -1l;
        out.writeLong(ts);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        boolean hasCreator = in.readBoolean();
        if (hasCreator) {
            creator = (Signee) in.readObject();
        }
        long ts = in.readLong();
        if (ts != -1) {
            when = new Timestamp(ts);
        }
    }

}
