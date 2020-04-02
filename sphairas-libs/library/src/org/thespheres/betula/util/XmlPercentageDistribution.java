/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.thespheres.betula.assess.Distribution;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "target-assessment", namespace = "http://www.thespheres.org/xsd/betula/percentage-distribution.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlPercentageDistribution implements Distribution<Double> {

    @XmlAttribute(name = "name")
    private String name;

    @XmlElement(name = "display-name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String display;

    @XmlElement(name = "values")
    @XmlList
    private double[] values;

    public XmlPercentageDistribution() {
    }

    public XmlPercentageDistribution(String name, String display, double[] values) {
        this.name = name;
        this.display = display;
        this.values = values;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return display;
    }

    @Override
    public List<Double> distribute(final Double ceiling) {
        return ceiling == null ? Collections.EMPTY_LIST : Arrays.stream(values)
                .map(d -> ceiling * d)
                .mapToObj(Double::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public List<Double> getDistributionValues() {
        return Arrays.stream(values)
                .mapToObj(Double::valueOf)
                .collect(Collectors.toList());
    }

}
