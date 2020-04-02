/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.adminreports.impl;

import java.io.IOException;
import org.thespheres.betula.admin.units.MultiUnitOpenSupport;

/**
 *
 * @author boris.heithecker
 */
class DescriptorProjectProperties extends MultiUnitOpenSupport.MultiUnitProperties {

    private final RemoteReportsDescriptor descriptor;
//    private final FileObject project;

    private DescriptorProjectProperties(final RemoteReportsDescriptor descriptor, final RemoteReportsDescriptorFileDataObject data) {
        super(data);
        this.descriptor = descriptor;
//        this.project = project.getProjectDirectory();
    }

    static DescriptorProjectProperties create(final RemoteReportsDescriptorFileDataObject rdob) throws IOException {
        if (rdob.isValid()) {
            final RemoteReportsDescriptor d = rdob.getDescriptor();
            return new DescriptorProjectProperties(d, rdob);
        }
        throw new IOException();
    }

}
