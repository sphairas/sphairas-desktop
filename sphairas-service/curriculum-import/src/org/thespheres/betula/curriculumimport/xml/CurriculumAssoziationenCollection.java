/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.curriculumimport.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.thespheres.betula.curriculumimport.StundentafelImportTargetsItem;
import org.thespheres.betula.xmlimport.utilities.AbstractLinkCollection;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "curriculum-associations")
@XmlAccessorType(value = XmlAccessType.FIELD)
public class CurriculumAssoziationenCollection extends AbstractLinkCollection<CurriculumAssoziation, StundentafelImportTargetsItem.ID> {

    public CurriculumAssoziationenCollection() {
        super(CurriculumAssoziation.class);
    }

    @Override
    protected CurriculumAssoziation create(StundentafelImportTargetsItem.ID id, int clone) {
        return new CurriculumAssoziation(id);
    }

}
