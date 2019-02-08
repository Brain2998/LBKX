package su.os3.lbkx;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.view.View;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


interface onCertSelected {
    void returnCert(File cert);
}

public class lbkxUIUtil {

    private static String[] mFileList;
    private static File mPath = new File("/");
    public static File abonentCert;

    public static void showAlert(final Activity activity, final String title, final String message){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog error = new AlertDialog.Builder(activity).create();
                error.setTitle(title);
                error.setMessage(message);
                error.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                error.show();
            }
        });
    }

    public static void showAlertAndReturn(final Activity activity, String title, String message){
        AlertDialog error = new AlertDialog.Builder(activity).create();
        error.setTitle(title);
        error.setMessage(message);
        error.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.onBackPressed();
                    }
                });
        error.show();
    }

    public static void chooseCert(final Activity context, final onCertSelected onCert){
        abonentCert=null;
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File sel = new File(dir, filename);
                return filename.contains(".pem") || sel.isDirectory();
            }

        };
        mFileList = lbkxArrayUtil.stringArrayConc(new String[]{".."}, mPath.list(filter));
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Choose certificate");
        builder.setItems(mFileList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    if (mPath.getAbsolutePath()=="/")return;
                    String newPath = "/";
                    String[] splits = mPath.getAbsolutePath().split("/");
                    int pathLng=splits.length-1;
                    for(int i=0;i<pathLng;++i){
                        newPath+="/"+splits[i];
                    }
                    mPath = new File(newPath);
                    chooseCert(context, onCert);
                }else {
                    if (new File(mPath + "/" + mFileList[which]).isDirectory()) {
                        mPath = new File(mPath + "/" + mFileList[which]);
                        chooseCert(context, onCert);
                    }else{
                        onCert.returnCert( new File(mPath + "/" + mFileList[which]));
                    }
                }
                //you can do stuff with the file here too
            }
        });
        builder.show();
    }

    public static void showChallenge(final Activity activity, final byte[] from, final byte[] challenge) throws UnsupportedEncodingException {
        try {
            AlertDialog challengeAlert = new AlertDialog.Builder(activity).create();
            challengeAlert.setTitle("Challenge received");
            challengeAlert.setMessage("Location challenge from " + new String(from, "UTF-8"));
            challengeAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "Choose sender certificate",
                    new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, int which) {
                            chooseCert(activity, new onCertSelected() {
                                @Override
                                public void returnCert(final File cert) {
                                    Thread locationRequest = new Thread(){
                                        public void run(){
                                            try {
                                                Looper.prepare();
                                                byte[] signature=Arrays.copyOfRange(challenge, 0, 512);
                                                byte[] text=Crypto.decryptRSA(Arrays.copyOfRange(challenge, 512, challenge.length), Crypto.getAlicePrivateKey());
                                                if (Crypto.verifyRSA(text, signature, Crypto.getPublicFromCert(FileUtils.readFileToByteArray(cert)))) {
                                                    Connection ttpConnection = new Connection(MainActivity.prefs.getString("ttp_address", "std26.os3.su"),
                                                            Integer.parseInt(MainActivity.prefs.getString("ttp_port", "8642")));
                                                    byte[] challengeR=Arrays.copyOfRange(text, 288, 320);
                                                    byte[] aesKey=Crypto.getRandom(32);
                                                    byte[] result=ttpConnection.sendPositionRequest(Arrays.copyOfRange(text, 0, 256),
                                                            lbkxArrayUtil.byteToDouble(Arrays.copyOfRange(text, 256, 264)), lbkxArrayUtil.byteToDouble(Arrays.copyOfRange(text, 264, 272)),
                                                            lbkxArrayUtil.byteToDouble(Arrays.copyOfRange(text, 272, 280)), lbkxArrayUtil.byteToDouble(Arrays.copyOfRange(text, 280, 288)), aesKey,
                                                            MainActivity.prefs.getString("ttp_number", "").getBytes(), MainActivity.prefs.getString("ttp_password", "").getBytes(),
                                                            Crypto.getPublicFromCert(Crypto.getTrentCert()));
                                                    if (result[0]==0) {
                                                        byte[] ivFromTrent = Arrays.copyOfRange(result, 1, 17);
                                                        byte[] lbkey = Crypto.decryptAES(Arrays.copyOfRange(result, 17, result.length), ivFromTrent, aesKey);
                                                        byte[] ivToAlice = Crypto.getRandom(16);
                                                        MainActivity.chatConnection.sendChatMessage(from, lbkxArrayUtil.byteArrayConc(ivToAlice, Crypto.encryptAES(challengeR, ivToAlice, lbkey)));
                                                        Intent intent=new Intent(activity, SecretActivity.class);
                                                        intent.putExtra("incoming", true);
                                                        intent.putExtra("lbKey", lbkey);
                                                        activity.startActivity(intent);
                                                    }
                                                    else {
                                                        throw new Exception("Wrong credentials");
                                                    }
                                                }
                                                else throw new Exception("Signature is invalid");

                                            } catch (Exception e) {
                                                showAlert(activity, "Error", e.getMessage());
                                            }
                                        }
                                    };
                                    locationRequest.setPriority(3);
                                    locationRequest.start();
                                }
                            });

                            dialog.dismiss();
                        }
                    });
            challengeAlert.show();
        }
        catch (Exception e){
            showAlert(activity, "Error", e.getMessage());
        }
    }
}
