package com.revature.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.persistence.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
/**
 * Represents User Entity in SQL database
 * @param salt Randomly generated string to modify password-hash if salt is not previously defined
 **/
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String salt;

    /**
     * if salt is null or empty, a random salt will be generated. Otherwise, return previously defined salt
     *
     * @return string representation of securely randomized byte array
     */
    public String getSalt() {
        if (salt == null || salt.isEmpty()) {

            byte[] randBytes = new byte[16];
            boolean saltValid;
            SecureRandom random = new SecureRandom();
            //salt maker in loop on the off chance it fails the first time
            do {
                saltValid = true;
                random.nextBytes(randBytes);
                salt = new String(randBytes, StandardCharsets.ISO_8859_1);// ISO_8859 makes byte array reconstruct-able
                byte[] unSalt = salt.getBytes(StandardCharsets.ISO_8859_1);
                if (!Arrays.equals(randBytes, unSalt)) {
                    saltValid = false;
                }
            } while (!saltValid);//works first time in tests (tested 500 mil+ times)
        }
        return salt;
    }

    /**
     * returns byte array in ISO 8859 1 format as it preserves byte values
     *
     * @return Byte Array of salt
     */
    public byte[] getSaltBytes() {
        return getSalt().getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * Save byte array as string while preserving data
     *
     * @param byteArray Should be the byte array generated by SecretKeyFactory after encoding
     */
    public void setPassword(byte[] byteArray) {
        this.password = new String(byteArray, StandardCharsets.ISO_8859_1);
    }

    /**
     * Should be a Hashed string that was saved with StandardCharsets.ISO_8859_1
     * @param passwordString hashed string
     */
    public void setPassword(String passwordString) {
        this.password=passwordString;
    }

    /**
     * Encrypt an existing password
     */
    public void encryptPassword() throws RuntimeException
    {
        KeySpec spec = new PBEKeySpec(getPassword().toCharArray(),this.getSaltBytes(), 65536, 128);
        try {
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = f.generateSecret(spec).getEncoded();
            this.password = new String(hash, StandardCharsets.ISO_8859_1);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
