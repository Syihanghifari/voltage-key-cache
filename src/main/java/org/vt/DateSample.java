package org.vt; /***********************************************************************
 * (c) Copyright 2021-2022 Micro Focus or one of its affiliates.       *
 *                                                                     *
 * The only warranties for products and services of Micro Focus and    *
 * its affiliates and licensors ("Micro Focus") are as may be set      *
 * forth in the express warranty statements accompanying such products *
 * and services. Nothing herein should be construed as constituting an *
 * additional warranty. Micro Focus shall not be liable for technical  *
 * or editorial errors or omissions contained herein. The information  *
 * contained herein is subject to change without notice.               *
 *                                                                     *
 * Except as specifically indicated otherwise, this document contains  *
 * confidential information and a valid license is required for        *
 * possession, use or copying. If this work is provided to the U.S.    *
 * Government, consistent with FAR 12.211 and 12.212, Commercial       *
 * Computer Software, Computer Software Documentation, and Technical   *
 * Data for Commercial Items are licensed to the U.S. Government under *
 * vendor's standard commercial license.                               *
 ***********************************************************************
 *
 * Date sample
 *
 * The try-catch in the main function constitutes the bulk of this sample
 * file and the functionality included. The first section sets up the
 * library context. The second section does a very basic protect and access.
 */
import com.voltage.securedata.enterprise.LibraryContext;
import com.voltage.securedata.enterprise.FPE;
import com.voltage.securedata.enterprise.VeException;

import java.util.Arrays;

public class DateSample {

    public static void main(String[] args) {
        int exitStatus = 1;

        System.out.println("Sample: Date");

        // Load the JNI library
        System.loadLibrary("vibesimplejava");

        // Print the API version
        System.out.println("Version: " + LibraryContext.getVersion());
        System.out.println();

        // Sample configuration
        String policyURL      = "https://"
                              + "voltage-pp-0000.dataprotection.voltage.com"//voltage-pp-0000.voltage.multisoft.co.id
                              + "/policy/clientPolicy.xml";
        String identity       = "sampleidentity";//admin@voltage.co.id
        char[] sharedSecret   = new char[] {'v','o','l','t','a','g','e','1','2','3'};//qwerty123
        String format         = "SAMPLE-DATE-FPE";//alphanumeric
        String trustStorePath = "../trustStore";
        String cachePath      = "../cache";

        LibraryContext library = null;
        FPE            fpe     = null;

        char[] plaintext  = new char[] {'1','2','-','1','0','-','2','0','0','5',
                                        ' ','1','0',':','2','7',':','3','3'};
        char[] result     = null;

        try {
            // Create the context for crypto operations
            library = new LibraryContext.Builder()
                                .setPolicyURL(policyURL)
                                .setFileCachePath(cachePath)
                                .setTrustStorePath(trustStorePath)
                                .setClientIdProduct("Simple_API_Java_Date_Sample",
                                                    "6.22.0.0")
                                .build();

            // Protect and access the date
            fpe = library.getFPEBuilder(format)
                         .setSharedSecret(sharedSecret)
                         .setIdentity(identity)
                         .build();


            char[] ciphertext = fpe.protect(plaintext);
            result            = fpe.access(ciphertext);

            // Converting to String for display only; not best practice for sensitive data.
            System.out.println();
            System.out.println("Original plaintext:  " + String.valueOf(plaintext));
            System.out.println("Ciphertext:          " + String.valueOf(ciphertext));
            System.out.println("Recovered plaintext: " + String.valueOf(result));

            if (!Arrays.equals(plaintext, result)) {
                // Converting to String is for display only.
                // This is not best practice for sensitive data.
                throw new Exception("Recovered plaintext '" + String.valueOf(result) + "'"
                                    + " does not match original plaintext '"
                                    + String.valueOf(plaintext) + "';"
                                    + " ciphertext is '" + String.valueOf(ciphertext) + "'.");
            }

            System.out.println();
            System.out.println("Recovered plaintext matches original plaintext.");
            System.out.println();
            System.out.println("Completed successfully.");
            exitStatus = 0;

        } catch (VeException ex) {
            System.out.println("Failed: " + ex.getDetailedMessage());
        } catch (Throwable ex) {
            System.out.println("Failed: Unexpected exception" + ex);
            ex.printStackTrace();
        } finally {
            // Clear the arrays to wipe out sensitive information.
            Arrays.fill(sharedSecret, '0');
            Arrays.fill(plaintext, '0');
            if (result != null) Arrays.fill(result, '0');

            // Explicit delete, required for JNI
            if (fpe != null) {
                fpe.delete();
            }

            // Explicit delete, required for JNI
            if (library != null) {
                library.delete();
            }
        }

        System.exit(exitStatus);
    }
}

