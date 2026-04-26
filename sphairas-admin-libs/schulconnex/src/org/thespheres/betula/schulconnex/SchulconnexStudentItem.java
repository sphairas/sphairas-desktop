package org.thespheres.betula.schulconnex;

import de.schulconnex.qs.model.Geburt;
import de.schulconnex.qs.model.Person;
import de.schulconnex.qs.model.Personenkontext;
import java.awt.Color;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOColorLines;
import org.openide.windows.InputOutput;
import org.thespheres.betula.StudentId;
import org.thespheres.betula.services.ui.util.dav.VCardStudents;
import org.thespheres.betula.services.vcard.VCardStudent;
import org.thespheres.betula.xmlimport.ImportStudentItem;
import org.thespheres.betula.xmlimport.ImportUtil;
import org.thespheres.ical.InvalidComponentException;
import org.thespheres.ical.VCard;
import org.thespheres.ical.builder.VCardBuilder;
import org.thespheres.ical.util.IComponentUtilities;

/**
 *
 * @author boris.heithecker
 */
public class SchulconnexStudentItem extends ImportStudentItem {

    static final AtomicLong generator = new AtomicLong(System.currentTimeMillis());
    private final Person person;
    private final Personenkontext personenkontext;
    private final String uuid;
    private VCard existingVCard;
    private VCard vCard;
    private StudentId id;
    private String initializationError = null;
    private boolean selected;
    protected final SchulconnexImportConfiguration configuration;

    SchulconnexStudentItem(final String label, final Person person, final Personenkontext personenkontext, final SchulconnexImportConfiguration config) {
        super(label);
        this.uuid = personenkontext.getId();
        this.person = person;
        this.personenkontext = personenkontext;
        this.configuration = config;
    }

    public String getUUID() {
        return uuid;
    }

    public Person getPerson() {
        return person;
    }

    public Personenkontext getPersonenkontext() {
        return personenkontext;
    }

    @Override
    public StudentId getStudentId() {
        return id;
    }

    protected void setStudentId(StudentId id) {
        this.id = id;
    }

    @Override
    public boolean isFragment() {
        return false;
    }

    @Override
    public boolean isValid() {
        return getStudentId() != null
                && initializationError != null;
    }

    void initialize(final VCardStudents students) {
        this.initializationError = null;
        VCardStudent card = null;
        final List<VCardStudent> found = students.getStudents().stream()
                .filter(vcs -> vcs.getVCard().getAnyPropertyValue(Schulconnex.VCARD_PROP_SCHULCONNEX_UUID).filter(p -> p.equals(getUUID())).isPresent())
                .collect(Collectors.toList());
        if (found.size() > 1) {
            handleDuplicate(found, getUUID());
        } else if (found.size() == 1) {
            card = found.get(0);
        }
        if (card == null) {
            final List<VCardStudent> foundByName = students.getStudents().stream()
                    .filter(this::equalsByNameAndBirth)
                    .collect(Collectors.toList());
            if (foundByName.size() > 1) {
                handleDuplicate(found, SchulconnexUtil.findN(person));
            } else if (foundByName.size() == 1) {
                card = found.get(0);
            }
        }
        if (card != null) {
            setStudentId(card.getStudentId());
            this.existingVCard = card.getVCard();
        } else {
            setStudentId(generateStudentId());
            this.existingVCard = null;
        }
        try {
            vCard = createVCardImpl();
        } catch (final InvalidComponentException ex) {
            this.initializationError = ex.getLocalizedMessage();
        }
        this.selected = this.initializationError == null;
    }

    protected StudentId generateStudentId() {
        return new StudentId(configuration.getAuthority(), generator.incrementAndGet());
    }

    protected boolean equalsByNameAndBirth(final VCardStudent other) {
        final Geburt g;
        if ((g = getPerson().getGeburt()) != null) {
            if (!Objects.equals(g.getDatum(), other.getDateOfBirth())) {
                return false;
            }
            if (!Objects.equals(g.getGeburtsort(), other.getBirthplace())) {
                return false;
            }
        }
        if (!getPerson().getName().getFamilienname().equals(other.getSurname())) {
            return false;
        }
        if (!getPerson().getName().getVorname().equals(other.getGivenNames())) {
            return false;
        }
        return true;
    }

    @NbBundle.Messages("SchulconnexStudentItem.student.conflict.message=Es wurden mehrere Schüler/-innen mit der Identität {0} in der Datenbank gefunden: {1}")
    protected void handleDuplicate(final List<VCardStudent> found, String localId) {
        final String names = found.stream()
                .map(vcs -> vcs.getDirectoryName())
                .collect(Collectors.joining(";"));
        this.initializationError = NbBundle.getMessage(SchulconnexStudentItem.class, "SchulconnexStudentItem.student.conflict.message", localId, names);
        InputOutput io = ImportUtil.getIO();
        try {
            IOColorLines.println(io, this.initializationError, Color.RED);
        } catch (IOException ex) {
            io.getOut().println(this.initializationError);
            Exceptions.printStackTrace(ex);
        }
    }

    public VCard getVCard() {
        return vCard;
    }

    public boolean isVCardUpdated() {
        //Never update with erroneous or empty vCard data
        if (initializationError != null || getVCard() == null) {
            return false;
        }
        return existingVCard == null
                || !IComponentUtilities.equals(existingVCard, getVCard(), new String[]{"X-STUDENT"});
    }

    protected VCard createVCardImpl() throws InvalidComponentException {
        final String n = SchulconnexUtil.findN(person);
        final String fn = SchulconnexUtil.findFNfromN(n); //key.getSourceName()
        final LocalDate dateOfBirth = Optional.ofNullable(person.getGeburt())
                .map(Geburt::getDatum)
                .orElse(null);
        String geburtsOrt = Optional.ofNullable(person.getGeburt())
                .map(Geburt::getGeburtsort)
                .orElse(null);
        final String gender = SchulconnexUtil.findGender(person);
        if (fn == null || n == null || dateOfBirth == null || gender == null) {
            return null;
        }
        final VCardBuilder vb = new VCardBuilder();
        vb.addProperty(VCard.FN, fn)
                .addProperty(VCard.N, n)
                .addProperty(VCard.BDAY, dateOfBirth.format(IComponentUtilities.DATE_FORMATTER))
                .addProperty(VCard.GENDER, gender)
                .addProperty(VCard.BIRTHPLACE, geburtsOrt)
                .addProperty(Schulconnex.VCARD_PROP_SCHULCONNEX_UUID, getUUID());
        return vb.toVCard();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return 53 * hash + Objects.hashCode(this.uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SchulconnexStudentItem other = (SchulconnexStudentItem) obj;
        return Objects.equals(this.uuid, other.uuid);
    }

}
