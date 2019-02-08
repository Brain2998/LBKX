package su.os3.lbkx;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

public class ConnectionTest {
    @Test
    public void RegTest1() throws IOException, GeneralSecurityException {
        Connection conn = new Connection("10.1.1.167", 8642);
        byte[] certEnd = Files.readAllBytes(new File("./certificate.pem").toPath());
        byte[] result=conn.sendRegisterRequest("79172445462".getBytes(), "MamaLeshi".getBytes(), "pass".getBytes(),
                Crypto.getPublicFromCert(certEnd));
        //assertArrayEquals(new byte[]{});
        System.out.println(result);
        conn.closeConnection();
    }
    @Test
    public void RegTest2() throws IOException, GeneralSecurityException {
        Connection conn = new Connection("10.1.1.167", 8642);
        byte[] certEnd = Files.readAllBytes(new File("./certificate.pem").toPath());
        byte[] result=conn.sendRegisterRequest("79047600751".getBytes(), "Damir".getBytes(), "pass".getBytes(),
                Crypto.getPublicFromCert(certEnd));
        //assertArrayEquals(new byte[]{});
        System.out.println(result);
        conn.closeConnection();
    }
    @Test
    public void RegTestDomain() throws IOException, GeneralSecurityException {
        Connection conn = new Connection("std26.os3.su", 8642);
        byte[] certEnd = Files.readAllBytes(new File("./certificate.pem").toPath());
        byte[] result=conn.sendRegisterRequest("79047600751".getBytes(), "Damir".getBytes(), "pass".getBytes(),
                Crypto.getPublicFromCert(certEnd));
        //assertArrayEquals(new byte[]{});
        System.out.println(result);
        conn.closeConnection();
    }
    @Test
    public void PosTest() throws IOException, GeneralSecurityException {
        Connection conn = new Connection("10.1.1.167", 8642);
        byte[] certEnd = Files.readAllBytes(new File("./certificate.pem").toPath());
        byte[] pk=Crypto.getPublicFromCert(certEnd);
        byte[] result=conn.sendPositionRequest(Crypto.encryptRSA(Crypto.getRandom(32), pk), 0.001, 0.001, 1.123,
                1.123, Crypto.getRandom(32), "79178847208".getBytes(), "Very cool Kanye".getBytes(), pk);
        System.out.println(result);
        conn.closeConnection();
    }

}
