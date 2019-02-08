package su.os3.lbkx;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CryptoTest {
    @Test
    public void nonceIsRandom(){
        byte[] firstNonce=Crypto.getRandom(32);
        byte[] secondNonce=Crypto.getRandom(32);
        assertFalse(Arrays.equals(firstNonce,secondNonce));
    }
    @Test
    public void RSATest() throws GeneralSecurityException {
        byte[][] keyPair=Crypto.genRSAKey();
        byte[] message=new byte[]{11, 12, 13, 14, 15, 16, 17, 18};
        assertArrayEquals(message, Crypto.decryptRSA(Crypto.encryptRSA(message, keyPair[0]), keyPair[1]));
    }
    @Test
    public void AESTest() throws  GeneralSecurityException {
        byte[] key=Crypto.getRandom(32);
        byte[] iv=lbkxArrayUtil.hexStringToByte("da39a3ee5e6b4b0d3255bfef95601890");
        byte[] message=new byte[]{11, 12, 13, 14, 15, 16, 17, 18};
        assertArrayEquals(message, Crypto.decryptAES(Crypto.encryptAES(message, iv, key), iv, key));
    }
    @Test
    public void certServerValid() throws GeneralSecurityException, IOException {
        byte[] certEnd=Files.readAllBytes(new File("./certificate.pem").toPath());
        byte[] certCA=Files.readAllBytes(new File("./ca.pem").toPath());
        assertTrue(Crypto.validateServerCert(certEnd, certCA));
    }
    @Test
    public void certServerInvalidUntrusted() throws GeneralSecurityException, IOException {
        byte[] certEnd=Files.readAllBytes(new File("./untrusted.pem").toPath());
        byte[] certCA=Files.readAllBytes(new File("./ca.pem").toPath());
        assertFalse(Crypto.validateServerCert(certEnd, certCA));
    }
    @Test
    public void certServerInvalidExpired() throws GeneralSecurityException, IOException {
        byte[] certEnd=Files.readAllBytes(new File("./expired.pem").toPath());
        byte[] certCA=Files.readAllBytes(new File("./ca.pem").toPath());
        assertFalse(Crypto.validateServerCert(certEnd, certCA));
    }
    @Test
    public void certServerInvalidSelfsigned() throws GeneralSecurityException, IOException {
        byte[] certEnd=Files.readAllBytes(new File("./selfsigned.pem").toPath());
        byte[] certCA=Files.readAllBytes(new File("./ca.pem").toPath());
        assertFalse(Crypto.validateServerCert(certEnd, certCA));
    }
    @Test
    public void certServerInvalidRevoked() throws GeneralSecurityException, IOException {
        byte[] certEnd=Files.readAllBytes(new File("./revoked.pem").toPath());
        byte[] certCA=Files.readAllBytes(new File("./ca.pem").toPath());
        assertFalse(Crypto.validateServerCert(certEnd, certCA));
    }
    @Test
    public void certClientValid() throws GeneralSecurityException, IOException {
        byte[] cert=Files.readAllBytes(new File("./root.pem").toPath());
        assertTrue(Crypto.validateClientCert(cert));
    }
    @Test
    public void certClientInvalid() throws GeneralSecurityException, IOException {
        byte[] cert=Files.readAllBytes(new File("./ca.pem").toPath());
        assertFalse(Crypto.validateClientCert(cert));
    }
    @Test
    public void signVerify() throws GeneralSecurityException {
        byte[][] keyPair=Crypto.genRSAKey();
        byte[] message=new byte[]{11, 12, 13, 14, 15, 16, 17, 18};
        assertTrue(Crypto.verifyRSA(message, Crypto.signRSA(message, keyPair[1]), keyPair[0]));
    }
    @Test
    public void createCerts() throws Exception {
        byte[][] keys = Crypto.genRSAKey();
        byte[] cert=Crypto.genSelfCert("testytest.com",keys[0],keys[1]);
        if (true){

        }
    }
}
