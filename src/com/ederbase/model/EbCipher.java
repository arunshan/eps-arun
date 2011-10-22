package com.ederbase.model;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class EbCipher
{
  Cipher ecipher;
  Cipher dcipher;
  private String stError = "";

  EbCipher(SecretKey key, String algorithm)
  {
    try
    {
      this.ecipher = Cipher.getInstance(algorithm);
      this.dcipher = Cipher.getInstance(algorithm);
      this.ecipher.init(1, key);
      this.dcipher.init(2, key);
    }
    catch (NoSuchPaddingException e) {
      this.stError = (this.stError + "<br>EXCEPTION: NoSuchPaddingException: " + e);
    }
    catch (NoSuchAlgorithmException e) {
      this.stError = (this.stError + "<br>EXCEPTION: NoSuchAlgorithmException: " + e);
    }
    catch (InvalidKeyException e) {
      this.stError = (this.stError + "<br>EXCEPTION: InvalidKeyException: " + e);
    }
  }

  EbCipher(String passPhrase)
  {
    byte[] salt = { -87, -101, -56, 50, 86, 52, -29, 3 };

    int iterationCount = 19;
    try
    {
      KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
      SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);

      this.ecipher = Cipher.getInstance(key.getAlgorithm());
      this.dcipher = Cipher.getInstance(key.getAlgorithm());

      AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

      this.ecipher.init(1, key, paramSpec);
      this.dcipher.init(2, key, paramSpec);
    }
    catch (InvalidAlgorithmParameterException e)
    {
      this.stError = (this.stError + "<br>EXCEPTION: InvalidAlgorithmParameterException: " + e);
    }
    catch (InvalidKeySpecException e) {
      this.stError = (this.stError + "<br>EXCEPTION: InvalidKeySpecException: " + e);
    }
    catch (NoSuchPaddingException e) {
      this.stError = (this.stError + "<br>EXCEPTION: NoSuchPaddingException: " + e);
    }
    catch (NoSuchAlgorithmException e) {
      this.stError = (this.stError + "<br>EXCEPTION: NoSuchAlgorithmException: " + e);
    }
    catch (InvalidKeyException e) {
      this.stError = (this.stError + "<br>EXCEPTION: InvalidKeyException: " + e);
    }
  }

  public String encrypt(String str)
  {
    try
    {
      byte[] utf8 = str.getBytes("UTF8");

      byte[] enc = this.ecipher.doFinal(utf8);

      return new BASE64Encoder().encode(enc);
    }
    catch (BadPaddingException e)
    {
    }
    catch (IllegalBlockSizeException e) {
    }
    catch (UnsupportedEncodingException e) {
    }
    catch (IOException e) {
    }
    return null;
  }

  public String decrypt(String str)
  {
    try
    {
      byte[] dec = new BASE64Decoder().decodeBuffer(str);

      byte[] utf8 = this.dcipher.doFinal(dec);

      return new String(utf8, "UTF8");
    }
    catch (BadPaddingException e)
    {
    }
    catch (IllegalBlockSizeException e) {
    }
    catch (UnsupportedEncodingException e) {
    }
    catch (IOException e) {
    }
    return null;
  }

  public String getError()
  {
    return this.stError;
  }
}