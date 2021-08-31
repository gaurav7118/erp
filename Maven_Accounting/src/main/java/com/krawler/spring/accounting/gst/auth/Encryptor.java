/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.accounting.gst.auth;

/**
 *
 * @author krawler
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
//import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
//import org.json.simple.JSONObject;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * @author 969243
 *
 */
public class Encryptor {

    private static final char JKSPassword[];
    private static final char PFXPassword[];
    private static KeyStore ks = null;
    private static String alias = null;
    private static X509Certificate UserCert = null;
    private static PrivateKey UserCertPrivKey = null;
    private static PublicKey UserCertPubKey = null;
    private static X509Certificate myPubCert = null;

    static {
        JKSPassword = "123456".toCharArray();
        PFXPassword = "123456".toCharArray();
    }

    public static void main(String args[]) {
        try {
            args = new String[2];
            args[0] = "27AACCK5779R034790";
            String timestamp = "";
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMYYYYHHmmssSSSSSS");
            Date d = new Date();
            Timestamp ts = new Timestamp(d.getTime());
            System.out.println(""+ts.toString());
            System.out.println("ts.getNanos():  "+ts.getNanos());
            
            timestamp = sdf.format(d);
            System.out.println("timestamp :: " + timestamp);
            args[1] = timestamp;

            System.out.println("AspId : " + args[0]);
            System.out.println("TimeStamp : " + args[1]);
            //String timeAsp = "27freps9008b00276429032017172315300111";
            String timeAsp = args[0] + args[1];
            sdf = new SimpleDateFormat("ddMMYYYYHHMMSSSSSSSS");
            timestamp = sdf.format(d);
            System.out.println("timestamp :: " + timestamp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the java key-store file for signing the data being sent for pan
     * services.
     */
    public static void createJavaKeyStoreFile() {
        System.out.println("creating java keystore file");
        try {
            FileInputStream stream = new FileInputStream("/home/krawler/NetBeansProjects/GSTpoc/Keys/keystore.pfx");
            KeyStore keyStorePKCS12 = KeyStore.getInstance("pkcs12");
            KeyStore keystoreJKS = KeyStore.getInstance("jks");
            File outputKeyStore = new File("/home/krawler/NetBeansProjects/GSTpoc/Keys/keystore.jks");

//			System.out.println("loading pkcs12 keystore");
            keyStorePKCS12.load(stream, PFXPassword);
//			System.out.println("loading java keystore file");
            keystoreJKS.load((outputKeyStore.exists() ? ((InputStream) new FileInputStream(outputKeyStore)) : null), JKSPassword);

            Enumeration<String> eAliases = keyStorePKCS12.aliases();

            while (eAliases.hasMoreElements()) {
                String alias = eAliases.nextElement();

                if (keyStorePKCS12.isKeyEntry(alias)) {
                    Key key = keyStorePKCS12.getKey(alias, PFXPassword);
                    Certificate chain[] = keyStorePKCS12.getCertificateChain(alias);
                    keystoreJKS.setKeyEntry(alias, key, JKSPassword, chain);
                }
            }
            OutputStream outputStream = new FileOutputStream(outputKeyStore);
//			System.out.println("Writing the output java keystore file");
            keystoreJKS.store(outputStream, JKSPassword);
            outputStream.close();
            outputStream.flush();
//			System.out.println("KeyStore successfully created");
        } catch (Exception e) {
            System.out.println("createJavaKeyStoreFile====" + e.getCause());
        }
    }

    public String generateSignature(String data) throws Exception {
        try {
            //Adding Security Provider for PKCS 12
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			//Setting password for the e-Token

            //logging into token
            ks = KeyStore.getInstance("jks");

            String filePath = "/gstkeys/keystore.jks"; // accessing file from Resources folder
            URL url = getClass().getResource(filePath);
            FileInputStream fileInputStream = new FileInputStream(url.getFile());
            ks.load(fileInputStream, JKSPassword);
            Enumeration<String> e = ks.aliases();

            while (e.hasMoreElements()) {
                alias = e.nextElement();
				//System.out.println("Alias of the e-Token : "+ alias);

                UserCert = (X509Certificate) ks.getCertificate(alias);

                UserCertPubKey = (PublicKey) ks.getCertificate(alias).getPublicKey();

//				System.out.println("loading Private key");
                UserCertPrivKey = (PrivateKey) ks.getKey(alias, JKSPassword);
            }

            //Method Call to generate Signature
            return MakeSignature(data);
        } catch (Exception e) {
            System.out.println("generateSignature" + e.getCause());
            throw new Exception(e);
        }

    }

    public String MakeSignature(String data) throws Exception {

	//logger.debug("MakeSignature called on data:"+data);
        try {
            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, JKSPassword);
            myPubCert = (X509Certificate) ks.getCertificate(alias);
            Store certs = new JcaCertStore(Arrays.asList(myPubCert));

            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

            generator.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider("BC").build("SHA256withRSA", privateKey, myPubCert));

            generator.addCertificates(certs);

            CMSTypedData data1 = new CMSProcessableByteArray(data.getBytes());

            CMSSignedData signed = generator.generate(data1, true);

       // signed = new CMSSignedData(data1, signed.getEncoded());
            // System.out.println("data=="+signed.toString());
            BASE64Encoder encoder = new BASE64Encoder();

            String signedContent = encoder.encode((byte[]) signed.getSignedContent().getContent());
            //System.out.println("Signed content: " + signedContent + "\n");

            String envelopedData = encoder.encode(signed.getEncoded());

            return envelopedData;
        } catch (Exception e) {
            System.out.println("MakeSignature ==" + e.getCause());
            throw new Exception();
        }
    }

    public boolean verifySignedContent(String bytes) {
        boolean verify = false;
        try {
            CMSSignedData signedData = new CMSSignedData(new BASE64Decoder().decodeBuffer(bytes));
            byte[] byte_out = null;
            ByteArrayOutputStream out = null;
            out = new ByteArrayOutputStream();
            signedData.getSignedContent().write(out);;
            byte_out = out.toByteArray();
            String s = new String(byte_out);
            System.out.println("Original Content-->" + s);
            System.out.println("asp id-->" + s.substring(0, 8));
            System.out.println("timestamp-->" + s.substring(18));
            Store store = signedData.getCertificates();
            SignerInformationStore signers = signedData.getSignerInfos();
            Collection c = signers.getSigners();
            Iterator it = c.iterator();
            while (it.hasNext()) {
                SignerInformation signer = (SignerInformation) it.next();
                System.out.println(signer);

                Collection certCollection = store.getMatches(signer.getSID());
                Iterator certIt = certCollection.iterator();
                X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
                X509Certificate cert;

                cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);

                String ca = "";
                String certNum = cert.getSerialNumber() + "";
                System.out.println(cert.getIssuerDN());
                String issuerDetails = cert.getIssuerDN() + "";
                String temp[] = issuerDetails.split(",");
                for (int i = 0; i < temp.length; i++) {
                    if (temp[i].startsWith("CN")) {
                        String temp2[] = temp[i].split("=");
                        ca = temp2[1];
                    }
                }

                System.out.println(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert));
                if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert))) {
                    System.out.println("verified");
                    verify = true;
                }
                System.out.println(ca);
                System.out.println(certNum);
            }

        } catch (IOException e) {
            System.out.println(e.getCause());
            verify = false;
        } catch (CertificateException e) {
            System.out.println(e.getCause());
            verify = false;
        } catch (OperatorCreationException e) {
            System.out.println(e.getCause());
            verify = false;
        } catch (CMSException e) {
            System.out.println(e.getCause());
            verify = false;
        }
        return verify;
    }

    public static String getOrignalContent(String bytes) {

        CMSSignedData signedData;
        String s = null;
        try {
            signedData = new CMSSignedData(new BASE64Decoder().decodeBuffer(bytes));
            byte[] byte_out = null;
            ByteArrayOutputStream out = null;
            out = new ByteArrayOutputStream();
            signedData.getSignedContent().write(out);;
            byte_out = out.toByteArray();
            s = new String(byte_out);
            System.out.println("Original Content-->" + s);
            System.out.println("asp id-->" + s.substring(0, 18));
        } catch (CMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(Encryptor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return s;

    }

}
