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
import java.io.FileInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.apache.commons.codec.binary.Base64;


/**
 * This class is used to encrypt the string using a public key
 *
 * @date 16th September, 2016
 */
public class EncryptionUtil {

    //public key url or location on the desktop for testing
//    public static String publicKeyUrl1 = "/home/krawler/NetBeansProjects/GSTpoc/Keys/DeskeraCA.cer";
    public static String publicKeyUrl1 = "/gstkeys/GSTN_G2A_SANDBOX_UAT_public.cer";
    private static String file;

    private static PublicKey readPublicKey(String filename) throws Exception {
        FileInputStream fin = new FileInputStream(filename);
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) f.generateCertificate(fin);
        PublicKey pk = certificate.getPublicKey();
        return pk;

    }

    /**
     * This method is used to encrypt the string , passed to it using a public
     * key provided
     *
     * @param planTextToEncrypt : Text to encrypt
     * @return :encrypted string
     */
    public static String encrypt(byte[] plaintext) throws Exception, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        PublicKey key = readPublicKey(publicKeyUrl1);
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByte = cipher.doFinal(plaintext);
        String encodedString = new String(Base64.encodeBase64(encryptedByte));
        return encodedString;
    }

    public static String generateEncAppkey(byte[] key) {
        try {
            return encrypt(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
