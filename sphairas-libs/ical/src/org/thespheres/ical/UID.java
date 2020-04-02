/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.ical;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.thespheres.betula.Identity;

/**
 *
 * @author boris.heithecker
 */
@XmlAccessorType(XmlAccessType.FIELD)
public final class UID extends Identity<String> implements Externalizable {

    @XmlAttribute(name = "uid", required = true)
    private String sysid;
    @XmlAttribute(name = "host", required = true)
    private String host;
    private static transient Long count = 0l;
    private static transient long lastMillis;
    private static final transient SimpleDateFormat utcdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    private static final transient NumberFormat nf = NumberFormat.getIntegerInstance();

    static {
//        nf.setMinimumIntegerDigits(4);
        nf.setGroupingUsed(false);
    }

    public UID() {//For Unmarshalling only!!!
    }

    public UID(String host, String uid) {
        this.sysid = uid;
        this.host = host;
    }

    @Messages("UID.parseException=Could not parse UID value {0}.")
    public static UID parse(String text) throws ParseException {
        int at = -1;
        if (text != null && !text.isEmpty()) {
            at = text.lastIndexOf('@');
            if (at != -1) {
                String sysid = text.substring(0, at);
                String host = text.substring(at + 1);
                return new UID(host, sysid);
            }
        }
        throw new ParseException(NbBundle.getMessage(UID.class, "UID.parseException", text), at);
    }

    @Override
    public String getId() {
        return sysid;
    }

    @Override
    public String getAuthority() {
        return host;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(sysid);
        out.writeObject(host);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        sysid = (String) in.readObject();
        host = (String) in.readObject();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.sysid);
        hash = 71 * hash + Objects.hashCode(this.host);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UID other = (UID) obj;
        if (!Objects.equals(this.sysid, other.sysid)) {
            return false;
        }
        return Objects.equals(this.host, other.host);
    }

    @Override
    public String toString() {
        return sysid + "@" + host;
    }

    public static UID create() throws IOException {
        return create(findNonLoopbackAddress());
    }

    public static UID create(String host) {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Host cannot be null or empty.");
        }
        long currentMillis;
        long c;
        synchronized (count) {
            currentMillis = System.currentTimeMillis();
            // guarantee uniqueness by ensuring timestamp is always greater
            // than the previous..
            if (currentMillis < lastMillis) {
                currentMillis = lastMillis;
            }
            if (currentMillis - lastMillis < 1000) {
                c = ++count;
            } else {
                c = count = 0l;
            }
            lastMillis = currentMillis;
        }
        StringJoiner sj = new StringJoiner("-");
        sj.add(utcdf.format(new Date(currentMillis)));
        if (c != 0) {
            sj.add(nf.format(c));
        }
        sj.add(ManagementFactory.getRuntimeMXBean().getName());
        return new UID(host, sj.toString());
    }

    private static String findNonLoopbackAddress() throws SocketException {
        for (NetworkInterface ni : NbCollections.iterable(NetworkInterface.getNetworkInterfaces())) {
            for (InetAddress a : NbCollections.iterable(ni.getInetAddresses())) {
                if (!a.isLoopbackAddress()) {
                    return a.getHostName();
                }
            }
        }
        return null;
    }
}
