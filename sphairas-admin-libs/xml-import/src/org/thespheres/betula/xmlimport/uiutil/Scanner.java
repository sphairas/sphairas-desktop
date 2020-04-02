/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.thespheres.betula.xmlimport.uiutil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import org.openide.util.Exceptions;
import org.thespheres.betula.document.Container;
import org.tukaani.xz.XZInputStream;

/**
 *
 * @author boris.heithecker
 */
public class Scanner {

// Ggf. entstehen andere Zeichenketten bei seriellem Anschluß da dann hier ASCII genutzt wird.
//Seriell ausgelesen kommt das heraus:
    public static void decode() {
        String input = "/Td6WFoAAATm1rRGAgAhARYAAAB0L+Wj4BAdAgxdAB4Py4cR2M5mkQ+DHsr9ezPUf+m32igxdiVmIE0qCW1q9ylwOEETlQiK0Fsdk0viUoaFC/cqv8aWmyF5o4V6VI3j3a/5XE9uIDIUDS66iYnCAJ0e4LvbH+e7fq5tLqV7ZzR2fwc4/sEpdbKRIBPl7GjvD5UVUwBtH5odTESnczFkVzR/ZbeGAoYmFWEQP/huP8qd4H/i6PjfsjE8/ev4NM+q"
                + "cHnZJY4pfRDq/aJXYbwY+DR1oYxqIsHCj53/v3wQj3DPJDyfdGlKP4yO711iOeCVecTYbGONMXDE2mUIAtS7mY0eszdodgSuW4f7K8Bnv9plBw+bIbeOp1VcVo29xWjF+9wBYBfHLR4DrIipzvWJmULLw3ByBRa8WcndBQzPKFPXSHKFlr7BaNbwLnaWd2Bv64+3DbPKUalhQkfsJPYCBvdKVYSF2Z3bz2DpfRbCqCbzOL+JfFTIUJ8lA2APQznC"
                + "NV2YBJ5I83HssmmaR2SeMqHmUXy+dQ1+WD1q79Q6Hj/j91nQUareeoitwSdcv30OdcyvinVGLtOU7hHJl1QQRn4itRmLsNKkpnMe/LKkGcLpuBQ057l8U2tDJWn0YigMfGK+PLBNkFp4qbns1449LP8wimU/wDeUtdG7Vd2//6SaWOvJPF2WmyCo25AefyMaKGPxfnOZjSa3c3UbpgUHn6UrSFMLNia7dJmNAF82YjL0uuDrAAGoBJ4gAADpMUim"
                + "scRn+wIAAAAABFla";
        byte[] arr = Base64.getDecoder().decode(input);
        ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        try {
            XZInputStream inxz = new XZInputStream(bais);
            JAXBContext jaxb = JAXBContext.newInstance(Container.class);
            Object unmarshal = jaxb.createUnmarshaller().unmarshal(inxz);
            Container c = (Container) unmarshal;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (JAXBException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
