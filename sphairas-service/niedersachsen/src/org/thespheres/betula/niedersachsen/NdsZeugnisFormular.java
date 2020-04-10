/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.niedersachsen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlValue;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.UnitId;
import org.thespheres.betula.document.DocumentId;
import org.thespheres.betula.document.Signee;
import org.thespheres.betula.niedersachsen.zeugnis.DeReportBuilderUtil;

/**
 *
 * @author boris.heithecker
 */
//@XmlRootElement(name = "zeugnis-sekundarstufe-niedersachsen", namespace = "http://www.thespheres.org/xsd/betula/zeugnis-sekundarstufe-niedersachsen/v0-0-1")
@XmlRootElement(name = "NdsZeugnisSekundarstufe", namespace = "http://www.thespheres.org/xsd/niedersachsen/zeugnisse.xsd")
@XmlAccessorType(XmlAccessType.FIELD)
public class NdsZeugnisFormular {

    public static final String FORMULAR_MIME = "text/zeugnis-sekundarstufe-niedersachsen+xml";
    public static String PFLICHTUNTERRICHT = "pu";
    public static String LEHRGAENGE = "lehrgaenge";
    public static String WAHLPFLICHTUNTERRICHT = "wpu";
    public static String PROFILUNTERRICHT = "profilu";
    public static String BERICHTE = "berichte";

    @XmlAttribute(name = "Mandant", required = false)
    private String provider;
    @Deprecated //In TemplateOptions
    @XmlAttribute(name = "Vorlage")
    private String template;
    @XmlElement(name = "Layout")
    private TemplateOptions templateOptions = new TemplateOptions();
    @XmlElement(name = "Kopf")
    private final Header kopf = new Header();
    @XmlElement(name = "Bereich")
    private List<Area> areas = new ArrayList<>();
    @XmlElement(name = "Ankreuzbereich")
    private CrossMarkArea crossMarkArea;
    @XmlElement(name = "Fußnote")
    private List<Footnote> footnotes = new ArrayList<>();
    @XmlElement(name = "Zusatz")
    private List<Note> notes = new ArrayList<>();
    @XmlElement(name = "Kopf2")
    private String secondPageHeader;
    @XmlElement(name = "Ort-Datum")
    private String placeDate;
    @XmlElement(name = "Zweitausfertigung")
    private Duplicate duplicate;
    @XmlAttribute(name = "addSpace")
    private Integer addSpace;
    private transient String sortString;
    @XmlElement(name = "Exception")
    private final List<String> exception = new ArrayList<>();
    @XmlElement(name = "report", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
    private DocumentId report;
    //This is necessary to catch all unknown elements during unmarshalling, 
    //so they will be marshalled again back into the document and don't get lost.
    @XmlAnyElement
    public org.w3c.dom.Element[] otherElements;

    //JAXB only
    public NdsZeugnisFormular() {
    }

    public NdsZeugnisFormular(final String sortString) {
        this.sortString = sortString;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    String getSortString() {
        return sortString;
    }

    public DocumentId getReportId() {
        return report;
    }

    public void setReportId(DocumentId report) {
        this.report = report;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public TemplateOptions getTemplateOptions() {
        return templateOptions;
    }

    public Header getKopf() {
        return kopf;
    }

    public List<Area> getAreas() {
        return areas;
    }

    public CrossMarkArea getCrossMarkArea() {
        return crossMarkArea;
    }

    public void setCrossMarkArea(CrossMarkArea cma) {
        this.crossMarkArea = cma;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public List<Footnote> getFootnotes() {
        return footnotes;
    }

    public String getPlaceDate() {
        return placeDate;
    }

    public void setPlaceDate(String placeDate) {
        this.placeDate = placeDate;
    }

    public String getSecondPageHeader() {
        return secondPageHeader;
    }

    public void setSecondPageHeader(String secondPageHeader) {
        this.secondPageHeader = secondPageHeader;
    }

    public Duplicate getDuplicate() {
        return duplicate;
    }

    public void setDuplicate(Duplicate duplicate) {
        this.duplicate = duplicate;
    }

    public Area addArea(String u) {
        final Area toAdd = new Area(u);
        areas.add(toAdd);
        return toAdd;
    }

    public Note addNote(String u, int position) {
        for (Note n : getNotes()) {
            if (n.getPosition() == position) {
                n.noteHeader = u;
                return n;
            }
        }
        Note toAdd = new Note(u, position);
        notes.add(toAdd);
        return toAdd;
    }

    public Footnote addFootnote(String index, String text) {
        for (Footnote n : getFootnotes()) {
            if (n.getIndex().equals(index)) {
                n.value = text;
                return n;
            }
        }
        Footnote toAdd = new Footnote(index, text);
        footnotes.add(toAdd);
        return toAdd;
    }

    public void addExceptionMessage(final String msg) {
        exception.add(msg);
    }

    public final void beforeMarshal(Marshaller m) {
        int addmm = 0;
        if (kopf.finalTitle != null) {
            addmm += 8;
        }
        if (areas.size() > 2) {
            addmm += (9 * (areas.size() - 2));
        }
        final long ll = areas.stream()
                .map(a -> a.lines)
                .collect(Collectors.summarizingInt(l -> l.size()))
                .getSum();
        if (ll > 6l) {
            addmm += (5 * (ll - 6));
        }
        if (kopf.leaving != null) {
            addmm += 5;
        }
        if (addmm > 30) {
            addSpace = 12;
        } else if (addmm > 16) {
            addSpace = 8;
        } else {
            addSpace = null;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Header {

        @XmlElement(name = "Schulname")
        private String schoolname;
        @XmlElement(name = "Schulname2")
        private String schoolname2;
        @XmlElement(name = "LogoLinks")
        private String imageLeft;
        @XmlElement(name = "LogoRechts")
        private String imageRight;
        @XmlAttribute(name = "LogoBreite")
        private String imageWidth;
        @XmlElement(name = "Schulzweig")
        private String divison;
        @XmlElement(name = "Zeugnisart")
        private String reportTitle;
        @XmlElement(name = "Halbjahrsangaben")
        private HeaderDataLine termData = null;
        @XmlElement(name = "Vorname-Nachname")
        private String studentName;
        @XmlElement(name = "Herkunftsangaben")
        private HeaderDataLine dataPlaceOfBirth = new HeaderDataLine();
        @XmlElement(name = "Abgangsdaten")
        private SchoolLeaving leaving;
        @XmlElement(name = "Schulabschluss")
        private String finalTitle = null;
        @XmlElement(name = "Versaeumnisse")
        private HeaderDataLine daysAbsent = new HeaderDataLine();
        @XmlElement(name = "unit", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
        private UnitId primaryUnit;
        @XmlElement(name = "student", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
        private StudentId student;

        public String getSchoolname() {
            return schoolname;
        }

        public void setSchoolname(String schoolname) {
            this.schoolname = schoolname;
        }

        public String getSchoolname2() {
            return schoolname2;
        }

        public void setSchoolname2(String schoolname2) {
            this.schoolname2 = schoolname2;
        }

        public String getImageLeft() {
            return imageLeft;
        }

        public void setImageLeft(String imageLeft) {
            this.imageLeft = imageLeft;
        }

        public String getImageRight() {
            return imageRight;
        }

        public void setImageRight(String imageRight) {
            this.imageRight = imageRight;
        }

        public String getImageWidth() {
            return imageWidth;
        }

        public void setImageWidth(String imageWidth) {
            this.imageWidth = imageWidth;
        }

        public String getDivison() {
            return divison;
        }

        public void setDivison(String divison) {
            this.divison = divison;
        }

        public String getReportTitle() {
            return reportTitle;
        }

        public void setReportTitle(String reportTitle) {
            this.reportTitle = reportTitle;
        }

        public HeaderDataLine getTermData() {
            if (termData == null) {
                termData = new HeaderDataLine();
            }
            return termData;
        }

        public void setTermData(String left, String center, String right) {
            getTermData().setLeft(left);
            getTermData().setCenter(center);
            getTermData().setRight(right);
        }

        public String getStudentName() {
            return studentName;
        }

        public void setStudentName(String studentName) {
            this.studentName = studentName;
        }

        public HeaderDataLine getDatePlaceOfBirth() {
            return dataPlaceOfBirth;
        }

        public void setDatePlaceOfBirth(String date, String place) {
            getDatePlaceOfBirth().setLeft(date);
            getDatePlaceOfBirth().setRight(place);
        }

        public SchoolLeaving getLeaving() {
            return leaving;
        }

        public void setSchoolLeaving(String date, String year) {
            leaving = new SchoolLeaving(date, year);
        }

        public String getFinalTitle() {
            return finalTitle;
        }

        public void setFinalTitle(String finalTitle) {
            this.finalTitle = finalTitle;
        }

        public HeaderDataLine getDaysAbsent() {
            return daysAbsent;
        }

        public void setDaysAbsent(String absent, String unex) {
            getDaysAbsent().setLeft(absent);
            getDaysAbsent().setRight(unex);
        }

        public UnitId getPrimaryUnit() {
            return primaryUnit;
        }

        public void setPrimaryUnit(UnitId primaryUnit) {
            this.primaryUnit = primaryUnit;
        }

        public StudentId getStudent() {
            return student;
        }

        public void setStudent(StudentId student) {
            this.student = student;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SchoolLeaving {

        @XmlElement(name = "Datum")
        private String date;
        @XmlElement(name = "Jahrgang")
        private String year;

        public SchoolLeaving() {
        }

        SchoolLeaving(String date, String year) {
            this.date = date;
            this.year = year;
        }

        public String getDate() {
            return date;
        }

        public String getYear() {
            return year;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class HeaderDataLine {

        @XmlElement(name = "Links")
        private String left;
        @XmlElement(name = "Mitte")
        private String center;
        @XmlElement(name = "Rechts")
        private String right;

        public String getLeft() {
            return left;
        }

        public void setLeft(String left) {
            this.left = left;
        }

        public String getCenter() {
            return center;
        }

        public void setCenter(String center) {
            this.center = center;
        }

        public String getRight() {
            return right;
        }

        public void setRight(String right) {
            this.right = right;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TemplateOptions {

        @XmlAttribute(name = "Vorlage")
        private String template;
        @XmlElement(name = "Parameter")
        private List<Option> properties = new ArrayList<>();

        public TemplateOptions() {
        }

        public TemplateOptions(String template) {
            this.template = template;
        }

        public String getTemplate() {
            return template;
        }

        public List<Option> getProperties() {
            return properties;
        }

        public void setOption(final String key, final String value) {
            final boolean set = properties.stream()
                    .filter(o -> o.getKey().equals(key))
                    .findAny()
                    .map(o -> o.setValue(value))
                    .orElse(false);
            if (!set) {
                properties.add(new Option(key, value));
            }
        }

        public void removeOption(String key) {
            final Iterator<Option> it = properties.iterator();
            while (it.hasNext()) {
                final Option o = it.next();
                if (o.getKey().equals(key)) {
                    it.remove();
                }
            }
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class Option {

            @XmlAttribute(name = "Name", required = true)
            private String key;
            @XmlAttribute(name = "Wert", required = true)
            private String value;

            public Option() {
            }

            public Option(String key, String value) {
                this.key = key;
                this.value = value;
            }

            public String getKey() {
                return key;
            }

            public String getValue() {
                return value;
            }

            boolean setValue(final String v) {
                this.value = v;
                return true;
            }

        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Area {

        @XmlElement(name = "Unterricht")
        private String header;
        @XmlElement(name = "LangZeile")
        private final List<LongLine> longlines = new ArrayList<>();
        @XmlElement(name = "Zeile")
        private final List<Line> lines = new ArrayList<>();
        @XmlElement(name = "Bericht")
        private List<Text> reports = new ArrayList<>();
        @XmlElement(name = "Anmerkung")
        private String note;

        //JAXBOnly
        public Area() {
        }

        public Area(String header) {
            this.header = header;
        }

        public String getHeader() {
            return header;
        }

        public void addEmptyLine() {
            lines.add(new Line());
        }

        public Line addLine(final String links, final String rechts) {
            final Line toAdd = new Line();
            if (links != null) {
                toAdd.getFieldLinks().setSubject(links);
            }
            if (rechts != null) {
                toAdd.getFieldRechts().setSubject(rechts);
            }
            lines.add(toAdd);
            return toAdd;
        }

        public void addLongLineCategory(final String cat) {
            final LongLine toAdd = new LongLine(cat);
            longlines.add(toAdd);
        }

        public LongLine addLongLine(final String fach) {
            final LongLine toAdd = new LongLine();
            toAdd.setSubject(fach);
            longlines.add(toAdd);
            return toAdd;
        }

        public List<Text> getReports() {
            return reports;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Line {

        @XmlElement(name = "Feld")
        private Field[] fields = new Field[]{new Field("Links"), new Field("Rechts")};

        public Field getFieldLinks() {
            return fields[0];
        }

        public Field getFieldRechts() {
            return fields[1];
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LongLine {

        @XmlElement(name = "LangKompBereich")
        private String subjectCat;
        @XmlElement(name = "LangFach")
        private String subject = "---";
        @XmlElement(name = "LangNoteneintrag")
        private String value = "---";
        @XmlAttribute(name = "Anmerkungsziffer")
        private String footnoteRef;
        @XmlElement(name = "target", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
        private DocumentId target;
        @XmlElement(name = "signee", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
        private Signee signee;

        public LongLine() {
        }

        public LongLine(String subjectCat) {
            this.subjectCat = subjectCat;
            this.subject = null;
            this.value = null;
        }

        public String getSubjectCategory() {
            return subjectCat;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getFootnoteRef() {
            return footnoteRef;
        }

        public void setFootnoteRef(String footnoteRef) {
            this.footnoteRef = footnoteRef;
        }

        public DocumentId getTarget() {
            return target;
        }

        public void setTarget(DocumentId target) {
            this.target = target;
        }

        public Signee getSignee() {
            return signee;
        }

        public void setSignee(Signee signee) {
            this.signee = signee;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CrossMarkArea {

        @XmlElement(name = "Fach")
        private final List<CrossMarkSubject> subjects = new ArrayList<>();

        public CrossMarkArea() {
        }

        public List<CrossMarkSubject> getSubjects() {
            return subjects;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CrossMarkSubject {

        @XmlElement(name = "FachName")
        private String subject;
        @XmlElement(name = "AnkreuzZeile")
        private final List<CrossMarkLine> lines = new ArrayList<>();

        public CrossMarkSubject() {
        }

        public CrossMarkSubject(String subject) {
            this.subject = subject;
        }

        public String getSubject() {
            return subject;
        }

        public List<CrossMarkLine> getLines() {
            return lines;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CrossMarkLine {

        @XmlElement(name = "Text")
        private String text;
        @XmlAttribute(name = "Wert")
        private int markPosition;
        @XmlAttribute(name = "Eintrag")
        private String entry;
        @XmlAttribute(name = "LinienEbene")
        private int level;

        public CrossMarkLine() {
        }

        public CrossMarkLine(String text) {
            this(text, 2);
        }

        public CrossMarkLine(String text, int level) {
            this.text = text;
            this.level = level;
        }

        public String getText() {
            return text;
        }

        public int getPosition() {
            return markPosition;
        }

        public void setPosition(int position) {
            this.markPosition = position;
        }

        public String getEntryText() {
            return entry;
        }

        public void setEntryText(String entry) {
            this.entry = entry;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Field {

        @XmlAttribute(name = "Position", required = true)
        private String position;
        @XmlElement(name = "Fach", required = true)
        private String subject = "---";
        @XmlElement(name = "Noteneintrag", required = true)
        private String value = "---";
        @XmlElement(name = "Fachleistungskurs")
        private String type;
        @XmlAttribute(name = "Anmerkungsziffer")
        private String footnoteRef;
        @XmlElement(name = "target", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
        private DocumentId target;
        @XmlElement(name = "signee", namespace = "http://www.thespheres.org/xsd/betula/betula.xsd")
        private Signee signee;

        //JAXBOnly
        public Field() {
        }

        public Field(String position) {
            this.position = position;
        }

        public String getPosition() {
            return position;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFootnoteRef() {
            return footnoteRef;
        }

        public void setFootnoteRef(String footnoteRef) {
            this.footnoteRef = footnoteRef;
        }

        public DocumentId getTarget() {
            return target;
        }

        public void setTarget(DocumentId target) {
            this.target = target;
        }

        public Signee getSignee() {
            return signee;
        }

        public void setSignee(Signee signee) {
            this.signee = signee;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Footnote {

        @XmlValue
        private String value;
        @XmlAttribute(name = "Ziffer")
        private String index;

        Footnote(String index, String value) {
            this.value = value;
            this.index = index;
        }

        public static String index(int index) {
            return Integer.toString(index + 1) + ".)";
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getIndex() {
            return index;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Note {

        @XmlAttribute(name = "Titel", required = true)
        private String noteHeader;
        @XmlAttribute(name = "Position", required = true)
        private int position;
        @XmlValue
        //TODO: adapted CollapsedStringAdapter which leaves \n = 0xA untouched 
        private String noteValue;

        //JAXB only
        public Note() {
        }

        public Note(String noteHeader, int position) {
            this.noteHeader = noteHeader;
            this.position = position;
        }

        public String getNoteHeader() {
            return noteHeader;
        }

        public String getNoteValue() {
            return noteValue;
        }

        public void setValue(String noteValue) {
            this.noteValue = noteValue;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Text extends Note {

        public Text() {
        }

        public Text(String noteHeader, int position) {
            super(noteHeader, position);
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Duplicate {

        @XmlElement(name = "SiegelSchule")
        private String siegelSchuleOderBehoerde;
        @XmlElement(name = "Schulleiter")
        private String schulleiter;
        @XmlElement(name = "Klassenlehrer")
        private String klassenlehrer;
        @XmlElement(name = "Zusatz")
        private String zusatz;
        @XmlElement(name = "Datum")
        private String datum;

        public Duplicate() {
        }

        public Duplicate(String siegelSchuleOderBehoerde, String gezeichnet, String datumZweitausfertigung) {
            this.siegelSchuleOderBehoerde = siegelSchuleOderBehoerde;
            this.schulleiter = gezeichnet;
            this.zusatz = datumZweitausfertigung;
        }

        public String getSiegelSchuleOderBehoerde() {
            return siegelSchuleOderBehoerde;
        }

        public void setSiegelSchuleOderBehoerde(String siegelSchuleOderBehoerde) {
            this.siegelSchuleOderBehoerde = siegelSchuleOderBehoerde;
        }

        public String getSchulleiter() {
            return schulleiter;
        }

        public void setSchulleiter(String schulleiter) {
            this.schulleiter = schulleiter;
        }

        public String getKlassenlehrer() {
            return klassenlehrer;
        }

        public void setKlassenlehrer(String klassenlehrer) {
            this.klassenlehrer = klassenlehrer;
        }

        public String getDatum() {
            return datum;
        }

        public void setDatum(String datum) {
            this.datum = datum;
        }

        public String getZusatz() {
            return zusatz;
        }

        public void setZusatz(String zusatz) {
            this.zusatz = zusatz;
        }

    }

    @XmlSeeAlso(NdsZeugnisFormular.class)
    @XmlRootElement(name = "NdsZeugnismappeSekundarstufe", namespace = "http://www.thespheres.org/xsd/niedersachsen/zeugnisse.xsd")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ZeugnisMappe {

        public static final String MAPPE_MIME = "text/zeugnis-sekundarstufe-niedersachsen-mappe+xml";
        @XmlAttribute(name = "Mandant", required = false)
        private String provider;
        @XmlElementRef
        private final List<NdsZeugnisFormular> reports = new ArrayList<>();

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public void add(final NdsZeugnisFormular add) {
            reports.add(add);
        }

        public NdsZeugnisFormular[] getReports() {
            return reports.stream()
                    .sorted(comparator())
                    .toArray(NdsZeugnisFormular[]::new);
        }

        protected void beforeMarshal(final Marshaller marshaller) throws JAXBException {
            Collections.sort(reports, comparator());
        }

        protected Comparator<NdsZeugnisFormular> comparator() {
            return (z1, z2) -> DeReportBuilderUtil.COLLATOR.compare(z1.getSortString(), z2.getSortString());
        }
    }

}
