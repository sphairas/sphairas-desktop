package org.thespheres.betula.listprint.builder;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author boris.heithecker
 */
public interface TableItem {

    public int getRowCount();

    public int getColumnCount();

    public String getColumnName(int columnIndex);

    public Object getValueAt(int rowIndex, int columnIndex);

}
