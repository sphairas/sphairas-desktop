/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.project.local.unit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang3.StringUtils;
import org.thespheres.betula.Student;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.util.XmlStudents;

/**
 *
 * @author boris.heithecker
 */
class StudentsPanelModel extends AbstractTableModel implements ActionListener {

    private final LocalUnit unit;
    private final ArrayList<Item> items = new ArrayList<>();
    private final Comparator<Item> itemComp = Comparator.comparing(Item::getDirectoryName, Collator.getInstance());
//    private final ArrayList<Item> removed = new ArrayList<>();

    public StudentsPanelModel(LocalUnit unit) {
        this.unit = unit;
        this.unit.getStudents().stream()
                .map(Item::new)
                .sorted(itemComp)
                .forEach(items::add);
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int ri, int ci) {
        Item it = items.get(ri);
        switch (ci) {
            case 0:
                return it.getDirectoryName();
            case 1:
                return it.getGivenNames();
            case 2:
                return it.getSurname();
            case 3:
                return it.getStudentId().getId();
        }
        return null;
    }

    @Override
    public boolean isCellEditable(int ri, int ci) {
        return ci == 1 || ci == 2;
    }

    @Override
    public void setValueAt(Object val, int ri, int ci) {
        final Item it = items.get(ri);
        final String text = (String) val;
        switch (ci) {
            case 1:
                it.given(text);
                break;
            case 2:
                it.surname(text);
                break;
        }
        fireTableRowsUpdated(ri, ri);
    }

    void add(Student s) {
        final Item add = new Item(s);
        items.add(add);
        fireTableDataChanged();
    }

    void addAll(Collection<Student> c) {
        c.stream()
                .map(Item::new)
                .forEach(items::add);
        fireTableDataChanged();
    }

    void removeItemAt(int ri) {
        Item rem = items.remove(ri);
//        removed.add(rem);
        fireTableDataChanged();
    }

    boolean isItemModified(int r) {
        Item it = items.get(r);
        return it.isModified();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final List<Student> c = items.stream()
                //                .map(it -> it.student())
                .collect(Collectors.toList());
        final XmlStudents xmlstuds = new XmlStudents(c);
        unit.setStudents(xmlstuds, true);
    }

    private class Item implements Student {

        private final Student student;
        private String surname;
        private String given;

        private Item(Student s) {
            this.student = s;
        }

        private boolean isModified() {
            return surname != null || given != null;
        }

        @Override
        public StudentId getStudentId() {
            return student.getStudentId();
        }

        @Override
        public String getFullName() {
            return getGivenNames().trim() + " " + getSurname().trim();
        }

        @Override
        public String getDirectoryName() {
            if (isModified()) {
                return getSurname() + ", " + getGivenNames();
            }
            return student.getDirectoryName();
        }

        @Override
        public String getGivenNames() {
            return given != null ? given : student.getGivenNames();
        }

        private void given(String text) {
            text = StringUtils.trimToNull(text);
            if (Objects.equals(student.getGivenNames(), text)) {
                given = null;
            } else {
                given = text;
            }
        }

        @Override
        public String getSurname() {
            return surname != null ? surname : student.getSurname();
        }

        private void surname(String text) {
            text = StringUtils.trimToNull(text);
            if (Objects.equals(student.getSurname(), text)) {
                surname = null;
            } else {
                surname = text;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Student)) {
                return false;
            }
            Student s = (Student) o;
            return s.getStudentId().equals(getStudentId());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 43 * hash + Objects.hashCode(student.getStudentId());
            return hash;
        }

    }
}
