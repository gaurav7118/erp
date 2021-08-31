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
 * *****************************************************************************
 * DISCLAIMER: The sample code or utility or tool described herein is provided
 * on an "as is" basis, without warranty of any kind. GSTN does not warrant or
 * guarantee the individual success developers may have in implementing the
 * sample code on their environment.
 *
 * GSTN does not warrant, guarantee or make any representations of any kind with
 * respect to the sample code and does not make any representations or
 * warranties regarding the use, results of use, accuracy, timeliness or
 * completeness of any data or information relating to the sample code. UIDAI
 * disclaims all warranties, express or implied, and in particular, disclaims
 * all warranties of merchantability, fitness for a particular purpose, and
 * warranties related to the code, or any service or software related thereto.
 *
 * GSTN is not responsible for and shall not be liable directly or indirectly
 * for any direct, indirect damages or costs of any type arising out of use or
 * any action taken by you or others related to the sample code.
 *
 * THIS IS NOT A SUPPORTED SOFTWARE.
 *****************************************************************************
 */
import com.krawler.spring.accounting.gst.services.GSTRConstants;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

public class AESEncryption {

    public static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    public static final String AES_ALGORITHM = "AES";
    public static final int ENC_BITS = 256;
    public static final String CHARACTER_ENCODING = "UTF-8";

    private static Cipher ENCRYPT_CIPHER;
    private static Cipher DECRYPT_CIPHER;
    private static KeyGenerator KEYGEN;

    static {
        try {
            ENCRYPT_CIPHER = Cipher.getInstance(AES_TRANSFORMATION);
            DECRYPT_CIPHER = Cipher.getInstance(AES_TRANSFORMATION);
            KEYGEN = KeyGenerator.getInstance(AES_ALGORITHM);
            KEYGEN.init(ENC_BITS);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(AESEncryption.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is used to encode bytes[] to base64 string.
     *
     * @param bytes : Bytes to encode
     * @return : Encoded Base64 String
     */
    public static String encodeBase64String(byte[] bytes) {
        return new String(Base64.encodeBase64(bytes));
    }

    /**
     * This method is used to decode the base64 encoded string to byte[]
     *
     * @param stringData : String to decode
     * @return : decoded String
     * @throws UnsupportedEncodingException
     */
    public static byte[] decodeBase64StringTOByte(String stringData) throws Exception {
        return Base64.decodeBase64(stringData.getBytes(CHARACTER_ENCODING));
    }

    /**
     * This method is used to encrypt the string which is passed to it as byte[]
     * and return base64 encoded encrypted String
     *
     * @param plainText : byte[]
     * @param secret : Key using for encrypt
     * @return : base64 encoded of encrypted string.
     *
     */
    public static String encryptEK(byte[] plainText, byte[] secret) {
        try {

            SecretKeySpec sk = new SecretKeySpec(secret, AES_ALGORITHM);
            ENCRYPT_CIPHER.init(Cipher.ENCRYPT_MODE, sk);
            return Base64.encodeBase64String(ENCRYPT_CIPHER
                    .doFinal(plainText));

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
	public static String base64EncryptEk(String plainText, byte[] secret)
	{
		try
		{
			SecretKeySpec sk = new SecretKeySpec(secret, AES_ALGORITHM);
			String encodedString = Base64.encodeBase64String( plainText.getBytes() );
			ENCRYPT_CIPHER.init(Cipher.ENCRYPT_MODE,  sk);
			return Base64.encodeBase64String(ENCRYPT_CIPHER.doFinal(encodedString.getBytes()));
			
		}
		catch(Exception ex)
		{
			System.out.println(" Error : ");
			ex.printStackTrace();
			return null;
		}
		
	}
    /**
     * This method is used to decrypt base64 encoded string using an AES 256 bit
     * key.
     *
     * @param plainText : plain text to decrypt
     * @param secret : key to decrypt
     * @return : Decrypted String
     * @throws IOException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    public static byte[] decrypt(String plainText, byte[] secret)
            throws InvalidKeyException, IOException, IllegalBlockSizeException,
            BadPaddingException, Exception {
        SecretKeySpec sk = new SecretKeySpec(secret, AES_ALGORITHM);
        DECRYPT_CIPHER.init(Cipher.DECRYPT_MODE, sk);
        return DECRYPT_CIPHER.doFinal(Base64.decodeBase64(plainText));
    }

    /**
     * This method is used to generate the base64 encoded secure AES 256 key
     *
     *
     * @return : base64 encoded secure Key
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    private static String generateSecureKey() throws Exception {
        SecretKey secretKey = KEYGEN.generateKey();
        return encodeBase64String(secretKey.getEncoded());
    }

    //This is generate enc_app_key which is encrypted by public GSTN Certificate
    public static String encryptedAppkey() {
        try {

            System.out.println("@@inside produceSampleData..");
            // Generation of app key. this will be in encoded.
            String appkey = generateSecureKey();
            System.out.println("App key in encoded : " + appkey);
            // Encrypt with GSTN public key
            String encryptedAppkey = EncryptionUtil.generateEncAppkey(decodeBase64StringTOByte(appkey));
            System.out.println("Encrypted App Key ->" + encryptedAppkey);

            return encryptedAppkey;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String args[]) throws Exception {
        System.out.println("encryptOTP(\"071093\") :: " + encryptOTP("071093"));
//        encryptedAppkey();
        String encKey = "w/kP0zFvchC2Sss/ZX2csSXFNB+r47kYMBeLVMMhzVyJ4W1Z1tnGk1aFKhBp5rvs";

        String asp_secret = GSTRConstants.aspSecret;
//		String encKey = "bvNHdKKvDMOVQ52e9lD7tVrOt+X60hPj2k+ml4/8m4LqiQSOoWwrDDt7oV4GJPss";
//		String asp_secret="pxwiaspnzeypkgsavpnkaunxuoyddxil";	
        byte[] enc_key = decrypt(encKey, asp_secret.getBytes());

        String enc_asp_secret = encryptEK(asp_secret.getBytes(), decodeBase64StringTOByte(encodeBase64String(enc_key)));

        System.out.println("asp secret encrypted:");
        System.out.println(enc_asp_secret);
        

    }

    public static String encryptOTP(String otp) {
        String encryptedOtp = null;
        try {

            encryptedOtp = encryptEK(otp.getBytes(), decodeBase64StringTOByte(GSTRConstants.appKey));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedOtp;
    }

    public static byte[] generatePaddedSek(String sek, String decAppKey) throws Exception {
        byte[] decSek = decrypt(sek, decodeBase64StringTOByte(decAppKey));
        return decSek;
    }

    public static String BCHmac(byte[] data, byte[] Ek) {
        HMac hmac = new HMac(new SHA256Digest());

        byte[] resBuf = new byte[hmac.getMacSize()];
        hmac.init(new KeyParameter(Ek));
        hmac.update(data, 0, data.length);
        hmac.doFinal(resBuf, 0);

        return encodeBase64String(resBuf);
    }

    public static byte[] getJsonBase64Payload(String str) {
        //String data ="{\"gstin\":\"27GSPMH1272G1ZO\",\"fp\":\"042016\",\"gt\":3782969.01,\"b2b\":[{\"ctin\":\"27GSPMH1271G1ZP\",\"inv\":[{\"inum\":\"S008400\",\"idt\":\"24-11-2016\",\"val\":729248.16,\"pos\":\"06\",\"rchrg\":\"N\",\"prs\":\"Y\",\"od_num\":\"DR008400\",\"od_dt\":\"20-11-2016\",\"etin\":\"01AABCE5507R1Z4\",\"itms\":[{\"num\":1,\"itm_det\":{\"ty\":\"G\",\"hsn_sc\":\"G1221\",\"txval\":10000,\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500}}]}]}],\"b2ba\":[{\"ctin\":\"01AABCE2207R1Z5\",\"inv\":[{\"oinum\":\"S008400\",\"oidt\":\"24-11-2016\",\"inum\":\"S008400\",\"idt\":\"24-11-2016\",\"val\":729248.16,\"pos\":\"06\",\"rchrg\":\"N\",\"prs\":\"Y\",\"od_num\":\"DR008400\",\"od_dt\":\"20-11-2016\",\"etin\":\"01AABCE5507R1Z4\",\"itms\":[{\"num\":1,\"itm_det\":{\"ty\":\"G\",\"hsn_sc\":\"G1221\",\"txval\":10000,\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500}}]}]}],\"b2cl\":[{\"state_cd\":\"05\",\"inv\":[{\"cname\":\"R_Glasswork Enterprise\",\"inum\":\"B129840\",\"idt\":\"14-04-2016\",\"val\":1000.03,\"pos\":\"06\",\"prs\":\"Y\",\"od_num\":\"DR008400\",\"od_dt\":\"20-11-2016\",\"etin\":\"27AHQPA8875L1ZU\",\"itms\":[{\"num\":1,\"itm_det\":{\"ty\":\"S\",\"hsn_sc\":\"S249\",\"txval\":10000,\"irt\":3,\"iamt\":833.33,\"csrt\":2,\"csamt\":500}}]}]}],\"b2cla\":[{\"state_cd\":\"05\",\"inv\":[{\"oinum\":\"9266\",\"oidt\":\"10-02-2016\",\"cname\":\"Glass store shop\",\"inum\":\"92661\",\"idt\":\"10-01-2016\",\"val\":784586.33,\"pos\":\"01\",\"prs\":\"N\",\"od_num\":\"DR008400\",\"od_dt\":\"20-11-2016\",\"etin\":\"27AHQPA8875L1ZU\",\"itms\":[{\"num\":1,\"itm_det\":{\"ty\":\"S\",\"hsn_sc\":\"S2469\",\"txval\":10000,\"irt\":3,\"iamt\":833.33,\"csrt\":2,\"csamt\":500}}]}]}],\"b2cs\":[{\"state_cd\":\"05\",\"ty\":\"G\",\"hsn_sc\":\"G2469\",\"txval\":10000,\"irt\":3,\"iamt\":500,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":3,\"csamt\":833,\"prs\":\"Y\",\"od_num\":\"DR008400\",\"od_dt\":\"20-11-2016\",\"etin\":\"20ABCDE7588L1ZJ\",\"typ\":\"E\"}],\"b2csa\":[{\"omon\":\"122016\",\"oty\":\"S\",\"ohsn_sc\":\"S2811\",\"osupst_cd\":\"05\",\"ty\":\"G\",\"hsn_sc\":\"G811\",\"state_cd\":\"05\",\"txval\":10000,\"irt\":3,\"iamt\":500,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":3,\"csamt\":833,\"prs\":\"Y\",\"od_num\":\"DR008400\",\"od_dt\":\"20-11-2016\",\"etin\":\"20ABCDE7588L1ZJ\",\"typ\":\"E\"}],\"cdnr\":[{\"ctin\":\"01AAAAP1208Q1ZS\",\"nt\":[{\"ntty\":\"C\",\"nt_num\":\"533515\",\"nt_dt\":\"23-09-2016\",\"rsn\":\"Not mentioned\",\"inum\":\"915914\",\"idt\":\"23-09-2016\",\"rchrg\":\"N\",\"val\":10000,\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500,\"etin\":\"01AAAAP1208Q1Z7\"}]}],\"cdnra\":[{\"ctin\":\"01AAAAP1208Q1ZS\",\"nt\":[{\"ntty\":\"C\",\"rsn\":\"Not mentioned\",\"ont_num\":\"533515\",\"ont_dt\":\"23-09-2016\",\"nt_num\":\"533515\",\"nt_dt\":\"23-09-2016\",\"inum\":\"915914\",\"idt\":\"23-09-2016\",\"rchrg\":\"N\",\"val\":5225.28,\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500,\"etin\":\"01AAAAP1208Q1Z7\"}]}],\"at\":[{\"typ\":\"B2B\",\"cpty\":\"12DEFPS5555D1Z2\",\"state_cd\":\"12\",\"doc_num\":\"100001\",\"doc_dt\":\"10-03-2016\",\"itms\":[{\"ty\":\"S\",\"hsn_sc\":\"S9043\",\"ad_amt\":100,\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500}]}],\"ata\":[{\"typ\":\"B2B\",\"ocpty\":\"R_Glasswork Enterprise\",\"odoc_num\":\"A100001\",\"odoc_dt\":\"10-03-2016\",\"cpty\":\"Glasswork Enterprise\",\"state_cd\":\"12\",\"doc_num\":\"100001\",\"doc_dt\":\"10-03-2016\",\"itms\":[{\"ty\":\"S\",\"hsn_sc\":\"S9043\",\"ad_amt\":10,\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500}]}],\"exp\":[{\"ex_tp\":\"WPAY\",\"inv\":[{\"inum\":\"81542\",\"idt\":\"12-02-2016\",\"val\":995048.36,\"sbpcode\":\"ASB9950\",\"sbnum\":\"84298\",\"sbdt\":\"04-10-2016\",\"prs\":\"Y\",\"od_num\":\"DR008400\",\"od_dt\":\"20-11-2016\",\"itms\":[{\"ty\":\"G\",\"hsn_sc\":\"G9207\",\"txval\":10000,\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500}]}]}],\"expa\":[{\"ex_tp\":\"WPAY\",\"inv\":[{\"oinum\":\"81542\",\"oidt\":\"12-02-2016\",\"inum\":\"815421\",\"idt\":\"22-02-2016\",\"val\":995048.36,\"sbpcode\":\"ASB9950\",\"sbnum\":\"84298\",\"sbdt\":\"04-10-2016\",\"prs\":\"Y\",\"od_num\":\"DR008400\",\"od_dt\":\"20-11-2016\",\"itms\":[{\"ty\":\"G\",\"hsn_sc\":\"G9207\",\"txval\":10000,\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500}]}]}],\"nil\":[{\"g\":[{\"sply_ty\":\"INTRB2B\",\"expt_amt\":123.45,\"nil_amt\":1470.85,\"ngsup_amt\":1258.5},{\"sply_ty\":\"INTRB2C\",\"expt_amt\":123.45,\"nil_amt\":1470.85,\"ngsup_amt\":1258.5},{\"sply_ty\":\"INTRAB2B\",\"expt_amt\":123.45,\"nil_amt\":1470.85,\"ngsup_amt\":1258.5},{\"sply_ty\":\"INTRAB2C\",\"expt_amt\":123.45,\"nil_amt\":1470.85,\"ngsup_amt\":1258.5}],\"s\":[{\"sply_ty\":\"INTRB2B\",\"expt_amt\":123.45,\"nil_amt\":1470.85,\"ngsup_amt\":1258.5},{\"sply_ty\":\"INTRB2C\",\"expt_amt\":123.45,\"nil_amt\":1470.85,\"ngsup_amt\":1258.5},{\"sply_ty\":\"INTRAB2B\",\"expt_amt\":123.45,\"nil_amt\":1470.85,\"ngsup_amt\":1258.5},{\"sply_ty\":\"INTRAB2C\",\"expt_amt\":123.45,\"nil_amt\":1470.85,\"ngsup_amt\":1258.5}]}],\"hsn\":[{\"data\":[{\"num\":1,\"ty\":\"G\",\"hsn_sc\":\"1009\",\"txval\":10.23,\"irt\":12.52,\"iamt\":14.52,\"crt\":78.52,\"camt\":78.52,\"srt\":12.34,\"samt\":12.9,\"csrt\":2,\"csamt\":500,\"desc\":\"Goods Description\",\"uqc\":\"1\",\"qty\":2.05,\"sply_ty\":\"INTRB2B\"}]}],\"txpd\":[{\"typ\":\"B2B\",\"cpty\":\"27ABCDE7588L1ZJ\",\"inum\":\"533515\",\"idt\":\"20-10-2016\",\"doc_num\":\"533515\",\"doc_dt\":\"23-09-2016\",\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500}],\"cdnur\":[{\"chksum\":\"AflJufPlFStqKBZ\",\"mon\":\"012017\",\"cname\":\"R Glass Factory\",\"state_cd\":\"06\",\"ntty\":\"C\",\"rsn\":\"Not mentioned\",\"val\":10000,\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500,\"etin\":\"01AAAAP1208Q1Z7\"}],\"cdnura\":[{\"chksum\":\"AflJufPlFStqKBZ\",\"omon\":\"012017\",\"ocname\":\"R Glass Factory\",\"mon\":\"012017\",\"cname\":\"R Glass Factory\",\"state_cd\":\"06\",\"ntty\":\"C\",\"rsn\":\"Not mentioned\",\"val\":10000,\"irt\":3,\"iamt\":833.33,\"crt\":4,\"camt\":500,\"srt\":5,\"samt\":900,\"csrt\":2,\"csamt\":500,\"etin\":\"01AAAAP1208Q1Z7\"}]}";
        JSONObject obj = null;
        try {
            obj = new JSONObject(str);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Base64.encodeBase64(obj.toString());
        //String en64 = encodeBase64String(str.toString().getBytes(StandardCharsets.UTF_8));
        //return en64.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeBase64(obj.toString().getBytes());
    }

    public static byte[] genDecryptedREK(String rek, byte[] ek) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, Exception {
        byte[] encRek = decrypt(rek, ek);
        System.out.println("Decrypted Rek : " + encodeBase64String(encRek));

        return encRek;
    }

    public static String decryptGstrData(String gstrResp, byte[] encRek) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, Exception {
        String originalData = new String(decodeBase64StringTOByte((new String((decrypt(gstrResp, encRek))))));
        return originalData;
    }
}
