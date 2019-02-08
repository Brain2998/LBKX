package su.os3.lbkx;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;

import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class Crypto {

    private static KeyFactory keyFactory;
    private static Cipher rsaCipher;
    private static Cipher aesCipher;
    private static Signature rsaSign;

    static {
        Security.addProvider(new BouncyCastleProvider());

        try {
            keyFactory=KeyFactory.getInstance("RSA");
            rsaCipher=Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding","BC");
            aesCipher=Cipher.getInstance("AES/CBC/PKCS5PADDING", "BC");
            rsaSign=Signature.getInstance("SHA256withRSA","BC");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getRandom(int size){
        SecureRandom sr = new SecureRandom();
        byte[] rndBytes=new byte[size];
        sr.nextBytes(rndBytes);
        return rndBytes;
    }

    public static byte[] getLocKey(long latitude, long longitude, String id, byte[] nonce)
            throws NoSuchAlgorithmException{
        MessageDigest digest=MessageDigest.getInstance("SHA-256");
        byte[] finalArray = lbkxArrayUtil.byteArrayConc(lbkxArrayUtil.longToByte(latitude),
                lbkxArrayUtil.longToByte(longitude), id.getBytes(), nonce);
        return digest.digest(finalArray);
    }

    public static byte[][] genRSAKey() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen=KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(4096);
        KeyPair kp=keyGen.generateKeyPair();
        byte[] publicKey=kp.getPublic().getEncoded();
        byte[] privateKey=kp.getPrivate().getEncoded();
        return new byte[][]{publicKey, privateKey};
    }

    public static PublicKey getPublicKey(byte[] pubKey) throws InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKey);
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey getPrivKey(byte[] privKey) throws InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec=new PKCS8EncodedKeySpec(privKey);
        return keyFactory.generatePrivate(keySpec);
    }

    public static byte[] encryptRSA(byte[] message, byte[] publicKey)
            throws GeneralSecurityException {
        PublicKey pk=getPublicKey(publicKey);
        rsaCipher.init(Cipher.ENCRYPT_MODE, pk);
        return rsaCipher.doFinal(message);
    }

    public static byte[] decryptRSA(byte[] message, byte[] privateKey)
            throws  GeneralSecurityException {
        PrivateKey pk =getPrivKey(privateKey);
        rsaCipher.init(Cipher.DECRYPT_MODE, pk);
        return rsaCipher.doFinal(message);
    }

    public static byte[] signRSA(byte[] message, byte[] privateKey) throws
            GeneralSecurityException {
        PrivateKey pk=getPrivKey(privateKey);
        rsaSign.initSign(pk);
        rsaSign.update(message);
        return rsaSign.sign();
    }

    public static boolean verifyRSA(byte[] message, byte[] sign, byte[] publicKey) throws
            GeneralSecurityException{
        PublicKey pk=getPublicKey(publicKey);
        rsaSign.initVerify(pk);
        rsaSign.update(message);
        return rsaSign.verify(sign);
    }

    public static byte[] encryptAES(byte[] message, byte[] initVector, byte[] key)
            throws GeneralSecurityException{
        IvParameterSpec iv=new IvParameterSpec(initVector);
        SecretKeySpec keySpec=new SecretKeySpec(key, "AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
        return aesCipher.doFinal(message);
    }

    public static byte[] decryptAES(byte[] message, byte[] initVector, byte[] key)
            throws  GeneralSecurityException{
        IvParameterSpec iv=new IvParameterSpec(initVector);
        SecretKeySpec keySpec=new SecretKeySpec(key, "AES");
        aesCipher.init(Cipher.DECRYPT_MODE, keySpec, iv);
        return aesCipher.doFinal(message);
    }

    public static X509Certificate getCertFromBytes(byte[] cert)
            throws GeneralSecurityException{
        CertificateFactory certFactory=CertificateFactory.getInstance("X.509");
        InputStream in=new ByteArrayInputStream(cert);
        return (X509Certificate)certFactory.generateCertificate(in);
    }

    public static byte[] getPublicFromCert(byte[] cert)
            throws  GeneralSecurityException{
        return getCertFromBytes(cert).getPublicKey().getEncoded();
    }

    public static boolean validateServerCert(byte[] certEnd, byte[] certCA)
            throws GeneralSecurityException {
        X509Certificate certificateEnd= getCertFromBytes(certEnd);
        X509Certificate certificateCA= getCertFromBytes(certCA);

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore)null);
        for (TrustManager trustManager: trustManagerFactory.getTrustManagers()) {
            if (trustManager instanceof X509TrustManager) {
                try{
                    X509TrustManager x509TrustManager = (X509TrustManager)trustManager;
                    x509TrustManager.checkClientTrusted(new X509Certificate[]{certificateEnd, certificateCA},"RSA");
                }
                catch (CertificateException e){
                    return false;
                }
            }
        }
        return true;
    }

    public static byte[] genSelfCert(String phoneNumber, byte[] publicKey, byte[] privateKey) throws GeneralSecurityException,
            OperatorCreationException, IOException {
        X500Name name=new X500Name("CN="+phoneNumber+",L=os3.su,C=RU,O=LBKX");
        Calendar start = Calendar.getInstance();
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.YEAR, 1);
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(name, BigInteger.ONE,
                start.getTime(), expiry.getTime(), name, SubjectPublicKeyInfo.getInstance(publicKey));
        JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer=builder.build(getPrivKey(privateKey));
        return certBuilder.build(signer).getEncoded();
    }

    public static boolean validateClientCert(byte[] cert) throws GeneralSecurityException{
        X509Certificate clientCert=getCertFromBytes(cert);
        try {
            clientCert.verify(clientCert.getPublicKey());
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public static byte[] getAlicePrivateKey() throws IOException {
        File pkFile=new File(MainActivity.appDirPath+"/privateKey.der");
        if (pkFile.exists()){
            return FileUtils.readFileToByteArray(pkFile);
        }
        else {
            throw new FileExistsException("Can not find private key.");
        }
    }

    public static byte[] getAliceCert() throws IOException {
        File certFile=new File(MainActivity.certDirPath+"/selfcert.pem");
        if (certFile.exists()){
            return FileUtils.readFileToByteArray(certFile);
        }
        else {
            throw new FileExistsException("Can not find self-signed certificate.");
        }
    }
    //Change when multiple contacts will be available
    public static byte[] getBobCert() throws IOException {
        File certFile=MainActivity.mAbonentCert;
        if (certFile.exists()){
            return FileUtils.readFileToByteArray(certFile);
        }
        else {
            throw new FileExistsException("Can not find certificate for chosen abonent.");
        }
    }

    public static byte[] getTrentCert() throws IOException {
        String ttpName=MainActivity.prefs.getString("ttp_address", "std26.os3.su");
        File certFile=new File(MainActivity.certDirPath+"/"+ttpName+"_end.pem");
        if (certFile.exists()){
            return FileUtils.readFileToByteArray(certFile);
        }
        else {
            throw new FileExistsException("Can not find certificate for trusted third party.");
        }
    }
}
