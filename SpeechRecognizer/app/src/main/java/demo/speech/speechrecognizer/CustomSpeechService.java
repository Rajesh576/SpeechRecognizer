package demo.speech.speechrecognizer;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class CustomSpeechService extends Service implements RecognitionListener {
    public CustomSpeechService() {
    }

    private SpeechRecognizer stt;
    private Intent recognizer_intent;
    private int id;
    private String num;
    private final String TAG="CustomSpeechService";

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        stt = SpeechRecognizer.createSpeechRecognizer(this);
        stt.setRecognitionListener(this);
        recognizer_intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        start();
    }

    private void log1(String message){
        Log.d("moo", num + ": " + message);}
    private void log2(String message){Log.d("cow", num + ": " + message);}

    public void start()
    {
        num = Integer.toString(++id);
        log1("start");
        stt.startListening(recognizer_intent);
    }

    public void stop(View view)
    {
        log1("stop");
        stt.stopListening();
    }

    @Override public void onReadyForSpeech(Bundle params){log1("onReadyForSpeech");}
    @Override public void onBeginningOfSpeech(){log1("onBeginningOfSpeech");}
    @Override public void onRmsChanged(float rms_dB){log2("onRmsChanged");}
    @Override public void onBufferReceived(byte[] buffer){log1("onBufferReceived");}
    @Override public void onEndOfSpeech(){log1("onEndOfSpeech");}
    @Override public void onResults(Bundle results){log1("onResults"+results.toString());
        {
            Log.d(TAG, "onResults"); //$NON-NLS-1$
            ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (list != null) {
                Log.d(TAG, "result: " + list.get(0));
                Toast.makeText(getApplicationContext(),list.get(0),Toast.LENGTH_LONG).show();

                String appNmae="";
                Intent launchIntent=null;
                if(list.get(0).toLowerCase().contains("Verizon")) {
                }
                else if(list.get(0).toLowerCase().contains("facebook")) {
                    launchIntent = getPackageManager().getLaunchIntentForPackage("com.facebook.katana");
                }
                else if(list.get(0).toLowerCase().contains("twitter"))
                {
                    launchIntent = getPackageManager().getLaunchIntentForPackage("com.twitter.android");
                }
                else if(list.get(0).toLowerCase().contains("whatsapp"))
                {
                    launchIntent = getPackageManager().getLaunchIntentForPackage("com.whatsapp");

                }
                else if(list.get(0).toLowerCase().contains("instagram"))
                {
                    launchIntent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                }
                if(launchIntent!=null)
                    startActivity( launchIntent );
                try {
                      Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            stt.startListening(recognizer_intent);
        }




    }
    @Override public void onPartialResults(Bundle partialResults){log1("onPartialResults");}
    @Override public void onEvent(int eventType, Bundle params){log1("onEvent");}
    @Override public void onError(int error)
    {
        String message = "";

        if(error == SpeechRecognizer.ERROR_AUDIO)                           message = "audio";
        else if(error == SpeechRecognizer.ERROR_CLIENT)                     message = "client";
        else if(error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS)   message = "insufficient permissions";
        else if(error == SpeechRecognizer.ERROR_NETWORK)                    message = "network";
        else if(error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT)            message = "network timeout";
        else if(error == SpeechRecognizer.ERROR_NO_MATCH)                   message = "no match found";
        else if(error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY)            message = "recognizer busy";
        else if(error == SpeechRecognizer.ERROR_SERVER)                     message = "server";
        else if(error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)             message = "speech timeout";

        log1("error " + message);
        stt.startListening(recognizer_intent);
    }

    @Override
    public void onDestroy()
    {
        stt.stopListening();
    }
}
