/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.sibank;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbBundle;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
@XmlRootElement(name = "DatenExport")
@XmlAccessorType(XmlAccessType.FIELD)
public final class DatenExportXml {

    public enum File {

        KURSE("Kurse"), SCHUELER("Schüler"), AGS("AGs");
        private final String display;

        private File(String dn) {
            this.display = dn;
        }

        public String getDisplayName() {
            return this.display;
        }
    }
    @XmlElement(name = "Satz")
    public List<Satz> satzes = new ArrayList<>();

    public boolean checkPossiblySchuelerFile() {
        return !satzes.isEmpty() && satzes.get(0).identNummer != null;
    }

    public File guessFile() {
        if (!satzes.isEmpty() && satzes.get(0).identNummer != null && satzes.get(0).fach == null) {
            return File.SCHUELER;
        } else if (!satzes.isEmpty() && (satzes.get(0).agausserunt != null || satzes.get(0).pflichtag != null)) {
            return File.AGS;
        }
        return File.KURSE;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static final class Satz {

        static final String[] fields = {"KLASSE", "FACH",
            "FACHART", "KURSNR", "LEHRERK_RZEL", "LEHRERNAME", "FAMILIENNAME", "NAMENSPR_FIX",
            "NAME_VORNAME", "RUFNAME", "STUFE", "GEBURTSDATUM", "EMAIL", "BILDUNGSGANG"};

        @XmlElement(name = "STUFE")//a. Schülerdatei
        public String stufe;

        @XmlElement(name = "KLASSE")//a. Schülerdatei
        public String klasse;

        @XmlElement(name = "KLASSE_LETZTES_SCHULJAHR")//Schülerdatei
        public String klasse_vorjahr;
        @XmlElement(name = "KLASSENLEHRER")//Schülerdatei
        public String klassenlehrer;
        @XmlElement(name = "KLASSENLEHRER_2")//Schülerdatei
        public String klassenlehrer2;
        @XmlElement(name = "KLASSENLEHRER_K_RZEL")//Schülerdatei
        public String klassenlehrer_par;
        @XmlElement(name = "KLASSENLEHRER_K_RZEL")//Schülerdatei
        public String klassenlehrer_par2;

        @XmlElement(name = "FACH")
        public String fach;

        @XmlElement(name = "FACHART")
        public String fachart;

        @XmlElement(name = "KURSNR")
        public String kursnr;

        @XmlElement(name = "LEHRERK_RZEL")
        public String lkuerzel;

        @XmlElement(name = "LEHRERNAME")
        public String lname;

        @XmlElement(name = "NAME_VORNAME")
        private String namevorname;//auchLehrer

        @XmlElement(name = "FAMILIENNAME")//a. Schülerdatei
        public String familienname;

        @XmlElement(name = "NAMENSPR_FIX")//a. Schülerdatei
        public String namenspraefix;

        @XmlElement(name = "RUFNAME")//a. Schülerdatei
        private String rufname;

        @XmlElement(name = "NAME_RUFNAME")//Schülerdatei 
        public String name_rufname;

        @XmlElement(name = "OFFIZIELLER_VORNAME")//Schülerdatei
        public String offiziellerVorname;

        @XmlElement(name = "IDENTNUMMER")//Schülerdatei
        public String identNummer;

        @XmlElement(name = "GESCHLECHT")//Schülerdatei
        public String geschlecht;

        @XmlElement(name = "GEBURTSORT")//Schülerdatei
        public String geburtsOrt;

        @XmlElement(name = "GEBURTSDATUM")//a. Schülerdatei
        public String gebdatum;

        @XmlElement(name = "EMAIL")
        public String email;

        @XmlElement(name = "BILDUNGSGANG")//a. Schülerdatei
        public String bildungsgang;

        @XmlElement(name = "STATUS")
        public String status;

        //        
        @XmlElement(name = "JAHRESNOTE")
        public String jahresnote;
        @XmlElement(name = "NOTE")
        public String note;
        @XmlElement(name = "NOTE_M_NDLICH")
        public String noteMuendlich;
        @XmlElement(name = "NOTE_PRAKTISCH")
        public String notePraktisch;
        @XmlElement(name = "NOTE_SCHRIFTLICH")
        public String noteSchriftlich;
        @XmlElement(name = "PR_FUNGSNOTE")
        public String pruefungsnote;
        @XmlElement(name = "TEXTNOTE")
        public String textnote;
        //

        @XmlElement(name = "AG1")//Schülerdatei
        public String ag1;
        @XmlElement(name = "AG2")//Schülerdatei
        public String ag2;
        @XmlElement(name = "AG3")//Schülerdatei
        public String ag3;
        @XmlElement(name = "AG4")//Schülerdatei
        public String ag4;
        @XmlElement(name = "AG5")//Schülerdatei
        public String ag5;
        //Lehrerimport 
        @XmlElement(name = "K_RZEL")
        public String l_kuerzel;
        @XmlElement(name = "NAME")
        public String l_name;
//        @XmlElement(name = "NAME_VORNAME")
//        String l_namevorname;
        @XmlElement(name = "PERSONALNR")
        public String l_pnummer;
        @XmlElement(name = "NAMENSZUSATZ ")
        public String l_nzusatz;
        @XmlElement(name = "VORNAME")
        public String l_vorname;
        @XmlElement(name = "VORNAME_2 ")
        public String l_vorname2;
        @XmlElement(name = "TITEL")
        public String l_titel;
        @XmlElement(name = "E_MAIL")//a.  Schülerdatei !!!!!!!!!!!!11  AGAUSSERUNTERRICHTLICH    PFLICHTAG
        public String l_email;
        @XmlTransient
        private String inferredDirName;
        @XmlTransient
        private boolean inferDirName = true;
        //Felder für AGS
        @XmlElement(name = "AGAUSSERUNTERRICHTLICH")
        public String agausserunt;
        @XmlElement(name = "PFLICHTAG")
        public String pflichtag;

        String getLehrerDirName() {
            return namevorname;
        }

        public boolean isAG() {
            return this.agausserunt != null && this.pflichtag != null;
        }

        @NbBundle.Messages({"DatenExportXml.nameConflict=Xml-Element \"NAME_VORNAME\" offenbar nicht gesetzt, verwende \"NAME_RUFNAME\" mit Wert \"{0}\".",
            "DatenExportXml.nameConflict2=Xml-Element \"NAME_VORNAME\" und \"NAME_RUFNAME\" offenbar nicht gesetzt, verwende \"FAMILIENNAME\" und \"OFFIZIELLER_VORNAME\"  mit Wert \"{0}\"."})
        public String getInferredDirectoryName() {
            if (inferDirName) {
                String stud = this.namevorname;
                if (stud == null) {//Offenbar aus der Schüler-Export-Tabelle
                    stud = this.name_rufname;
                    if (stud != null) {
                        String msg = NbBundle.getMessage(DatenExportXml.class, "DatenExportXml.nameConflict", stud);
                        ImportUtil.getIO().getOut().println(msg);
                    } else if (this.familienname != null && this.offiziellerVorname != null) {
                        stud = this.familienname + ", " + this.offiziellerVorname;
                        String msg = NbBundle.getMessage(DatenExportXml.class, "DatenExportXml.nameConflict2", stud);
                        ImportUtil.getIO().getOut().println(msg);
                    }
                }
                if (stud != null) {
                    inferredDirName = stud;
                }
                inferDirName = false;
            }
            return inferredDirName;
        }

        @NbBundle.Messages({"DatenExportXml.warning.nonamevorname=Unterschiedliche Namesvarianten gefunden: „{0}“ und „{1}“"})
        public String getInferredDirectoryName(File forType) {
            if (inferDirName) {
                String stud;
                if (forType.equals(File.SCHUELER)) {//Offenbar aus der Schüler-Export-Tabelle
                    stud = this.name_rufname;
                } else {
                    String nv = this.namevorname;
                    String foff = null;
                    if (this.familienname != null && this.offiziellerVorname != null) {
                        foff = this.familienname + ", " + this.offiziellerVorname;
                    }
                    if (foff != null) {
                        if (nv != null && !nv.equals(foff)) {
                            String msg = NbBundle.getMessage(DatenExportXml.class, "DatenExportXml.warning.nonamevorname", nv, foff);
                            ImportUtil.getIO().getOut().println(msg);
                        }
                        stud = foff;
                    } else if (nv != null) {
                        stud = nv;
                    } else {
                        stud = "unbekannt";
                    }
                }
                inferredDirName = stud;
                inferDirName = false;
            }
            return inferredDirName;
        }

        public String findN() {
            if (familienname == null || offiziellerVorname == null) {
                return null;
            }
            String fam = familienname.trim();
            if (!StringUtils.isBlank(namenspraefix)) {
                fam = namenspraefix.trim() + " " + fam;
            }
            String given[] = StringUtils.split(offiziellerVorname);
            StringJoiner sj = new StringJoiner(",");
            for (String g : given) {
                sj.add(g);
            }
            return fam + ";" + sj.toString() + ";;;";
        }

        public String findFN() {
            String fn = name_rufname;
            if (fn == null) {
                fn = familienname + ", " + offiziellerVorname;
            }
            return fn;
        }

        public String findBDay() {
            try {
                LocalDate ld = LocalDate.parse(gebdatum, SiBankImportData.SIBANK_DATUM);
                return IComponentUtilities.DATE_FORMATTER.format(ld);
            } catch (DateTimeException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public String findGender() {
            if (geschlecht != null) {
                if (null != geschlecht.trim()) {
                    switch (geschlecht.trim()) {
                        case "W":
                            return "F";
                        case "M":
                            return "M";
                    }
                }
            }
            return null;
        }
    }
}
