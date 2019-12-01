package net.dm73.plainpress.util;


import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public abstract class UrlEncoder {

    private static String encode(String data, String idToken) {

        Mac crypt = null;
        try {
            crypt = Mac.getInstance(Constants.DECRYPTED);
            SecretKeySpec secret_key = new SecretKeySpec(idToken.getBytes(Constants.CODE_BASE), Constants.DECRYPTED);
            crypt.init(secret_key);

            return new String(Hex.encodeHex(crypt.doFinal(data.getBytes(Constants.CODE_BASE))));

        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (InvalidKeyException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }

    }

    public static String decodedURL(String url, String uId, String idToken){
        String apiUrl = Constants.API_URL;
        String newURL = apiUrl+ url + "?u="+uId;
        newURL = encode(newURL, idToken);
        return (apiUrl+url+"?u="+uId+"&sign="+newURL);
    }



}
