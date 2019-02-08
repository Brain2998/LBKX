package su.os3.lbkx;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

public class SecretActivity extends AppCompatActivity {

    private TextView mSecret;
    private TextView mExchangeResult;
    private Button mSendChallenge;
    private TextView mExResult;
    private byte[] nonce=Crypto.getRandom(32);
    private byte[] randomChallenge=Crypto.getRandom(32);
    private byte[] lbKey;
    private byte[] exchangeResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret);
        mSecret=findViewById(R.id.lbSecret);
        mExchangeResult=findViewById(R.id.exchangeResult);
        mSendChallenge=findViewById(R.id.send_challenge);
        mExResult=findViewById(R.id.exResultLabel);
        Intent intent=getIntent();
        if (intent.getBooleanExtra("incoming", false)){
            lbKey=intent.getByteArrayExtra("lbKey");
            mSendChallenge.setVisibility(View.GONE);
            Thread exchangeResult=new Thread(){
                public void run() {
                    try {
                        final byte[] result = MainActivity.chatConnection.readMessage();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (Arrays.areEqual(Arrays.copyOfRange(result, 11, result.length), new byte[]{0})) {
                                    mExchangeResult.setText("Exchange succeed");
                                } else {
                                    mExchangeResult.setText("Exchange failed, wrong location");
                                }
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            exchangeResult.setPriority(3);
            exchangeResult.start();
        }
        else {
            try {
                lbKey = Crypto.getLocKey(LocationAdjustmentActivity.latitudeTrans, LocationAdjustmentActivity.longitudeTrans,
                        MainActivity.contactsList.get(0), nonce);

            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        Base64 encoder = new Base64();
        mSecret.setText(new String(encoder.encode(lbKey)));
    }

    public void sendChallenge(View v) throws IOException, GeneralSecurityException, InterruptedException {
        Thread sendChallenge=new Thread() {
            public void run() {
                try {
                    final byte[] recepient=MainActivity.contactsList.get(0).getBytes();
                    exchangeResult = MainActivity.chatConnection.sendChallange(recepient, nonce, LocationAdjustmentActivity.latScale,
                            LocationAdjustmentActivity.longScale, LocationAdjustmentActivity.latOffset, LocationAdjustmentActivity.longOffset,
                            randomChallenge, Crypto.getAlicePrivateKey(), Crypto.getPublicFromCert(Crypto.getBobCert()), Crypto.getPublicFromCert(Crypto.getTrentCert()));
                    if (Arrays.areEqual(Arrays.copyOfRange(exchangeResult, 0, 11),recepient)) {
                        final byte[] iv = Arrays.copyOfRange(exchangeResult, 11, 27);
                        //Exchange succeed, Alice should send result to Bob
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (Arrays.areEqual(Crypto.decryptAES(Arrays.copyOfRange(exchangeResult, 27, exchangeResult.length), iv, lbKey), randomChallenge)) {
                                        mExchangeResult.setText("Exchange succeed");
                                        MainActivity.chatConnection.sendChatMessage(recepient, new byte[]{0});
                                    } else {
                                        mExchangeResult.setText("Exchange failed, wrong location");
                                        MainActivity.chatConnection.sendChatMessage(recepient, new byte[]{1});
                                    }
                                } catch (GeneralSecurityException | IOException e) {
                                    e.printStackTrace();
                                    mExchangeResult.setText("Exchange failed, wrong location");
                                    try {
                                        MainActivity.chatConnection.sendChatMessage(recepient, new byte[]{1});
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                } catch (GeneralSecurityException | IOException e) {
                    lbkxUIUtil.showAlert(SecretActivity.this, "Error", e.getMessage());
                    mExchangeResult.setText("Exchange failed, wrong location");
                }
            }
        };
        sendChallenge.setPriority(3);
        sendChallenge.start();
    }
}
