/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen.admin.ui.validate;

import org.thespheres.betula.StudentId;
import org.thespheres.betula.TermId;
import org.thespheres.betula.validation.impl.ValidationRunException;

/**
 *
 * @author boris.heithecker
 */
interface RunReport {

    void runOneTerm(TermId term) throws ValidationRunException;

    void runOneDocument(TermId term, StudentId stud) throws ValidationRunException;

    void run();
}
