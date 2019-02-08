package su.os3.lbkx;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Arrays;


public class Connection {

    private Socket socket;
    private OutputStream toHost;
    private InputStream fromHost;

    public String getSocketHost(){
        return socket.getInetAddress().getHostName();
    }

    Connection(String addr, int port) throws IOException {
        socket=new Socket(addr, port);
        toHost =socket.getOutputStream();
        fromHost =socket.getInputStream();
    }

    public boolean isAlive() throws IOException {
        try {
            writeMessageToServer(new byte[]{0, 0});
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
            return false;
        }
    }

    public boolean isDataAvailable() throws IOException {
        if (fromHost.available()>0){
            return true;
        }
        return false;
    }

    private void writeMessageToServer(byte[] message) throws IOException {
        toHost.write(message);

    }

    public void writeMessageToClient(byte[] message) throws IOException {
        toHost.write(message.length);
        toHost.write(message);
    }

    public byte[] readMessage() throws IOException {
        byte[] byteLength = new byte[2];
        fromHost.read(byteLength,0, 2);
        short length = lbkxArrayUtil.byteToShort(byteLength);
        byte[] message=new byte[length];
        fromHost.read(message, 0, length);
        return message;
    }

    public void closeConnection() throws IOException {
        toHost.close();
        fromHost.close();
        socket.close();
    }

    public byte[] sendChallange(byte[] phoneNumber, byte[] nonce, double scaleLat, double scaleLong, double offLat,
                              double offLong, byte[] challenge, byte[] AliceKey, byte[] BobKey, byte[] TrentKey)
            throws GeneralSecurityException, IOException {
        byte[] dScaleLat=lbkxArrayUtil.doubleToByte(scaleLat);
        byte[] dScaleLong=lbkxArrayUtil.doubleToByte(scaleLong);
        byte[] offsetLat=lbkxArrayUtil.doubleToByte(offLat);
        byte[] offsetLong=lbkxArrayUtil.doubleToByte(offLong);
        byte[] encNonce=Crypto.encryptRSA(nonce, TrentKey);
        byte[] message=lbkxArrayUtil.byteArrayConc(encNonce, dScaleLat, dScaleLong, offsetLat, offsetLong, challenge);
        byte[] signature=Crypto.signRSA(message, AliceKey);
        byte[] ciphertext=Crypto.encryptRSA(message, BobKey);
        message=lbkxArrayUtil.byteArrayConc(signature, ciphertext);
        sendChatMessage(phoneNumber, message);

        return readMessage();
    }

    public byte[] sendPositionRequest(byte[] encNonce, double scaleLat, double scaleLong, double offLat, double offLong,
                                    byte[] aesKey, byte[] phoneNumber, byte[] password, byte[] TrentKey)
            throws GeneralSecurityException, IOException {
        byte[] opCode=new byte[]{0};
        byte[] dScaleLat=lbkxArrayUtil.doubleToByte(scaleLat);
        byte[] dScaleLong=lbkxArrayUtil.doubleToByte(scaleLong);
        byte[] offsetLat=lbkxArrayUtil.doubleToByte(offLat);
        byte[] offsetLong=lbkxArrayUtil.doubleToByte(offLong);
        byte[] passLng=Arrays.copyOfRange(lbkxArrayUtil.intToByte(password.length), 3, 4);
        byte[] message=lbkxArrayUtil.byteArrayConc(encNonce, aesKey, dScaleLat, dScaleLong, offsetLat, offsetLong,
                phoneNumber, passLng, password);
        writeMessageToServer(lbkxArrayUtil.byteArrayConc(opCode,
                Crypto.encryptRSA(Arrays.copyOfRange(message, 0, 192), TrentKey),
                Crypto.encryptRSA(Arrays.copyOfRange(message, 192, message.length), TrentKey)));
        return readMessage();
    }

    public byte[] sendRegisterRequest(byte[] phoneNumber, byte[] nickname, byte[] password, byte[] TrentKey)
            throws GeneralSecurityException, IOException{
        byte[] opCode=new byte[]{2};
        byte[] nickLng=Arrays.copyOfRange(lbkxArrayUtil.intToByte(nickname.length), 3, 4);
        byte[] passLng=Arrays.copyOfRange(lbkxArrayUtil.intToByte(password.length), 3, 4);
        byte[] message=lbkxArrayUtil.byteArrayConc(phoneNumber,nickLng,nickname,passLng,password);
        writeMessageToServer(lbkxArrayUtil.byteArrayConc(opCode, Crypto.encryptRSA(message, TrentKey)));
        return readMessage();
    }

    public void sendCertRequest() throws IOException {
        writeMessageToServer(new byte[]{1});
        byte[] reply= readMessage();
        short lengthEnd=lbkxArrayUtil.byteToShort(Arrays.copyOfRange(reply, 0, 2));
        byte[] certEnd=Arrays.copyOfRange(reply, 2, lengthEnd+2);
        FileUtils.writeByteArrayToFile(new File(MainActivity.certDirPath + "/"+getSocketHost()+"_end.pem"), certEnd);
        short lengthCA=lbkxArrayUtil.byteToShort(Arrays.copyOfRange(reply, lengthEnd+2, lengthEnd+4));
        byte[] certCA=Arrays.copyOfRange(reply, lengthEnd+4, lengthCA+lengthEnd+4);
        FileUtils.writeByteArrayToFile(new File(MainActivity.certDirPath + "/"+getSocketHost()+"_ca.pem"), certCA);
    }

    public boolean connectToChatAttempt(String ID) throws IOException {
        //Send your ID and ID of contacts to check they availability
        writeMessageToServer(ID.getBytes("UTF-8"));
        byte[] result = readMessage();
        if(result[0] == 0){
            return true;

        }
        else {
            closeConnection();
            return false;
        }
    }

    public void sendChatMessage(byte[] receiverID, byte[] text) throws IOException {
        byte[] message=lbkxArrayUtil.byteArrayConc(receiverID, text);
        writeMessageToServer(message);
    }
}
