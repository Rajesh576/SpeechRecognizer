package demo.speech.speechrecognizer;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SpeechService extends Service {
    public SpeechService() {
    }
//    private static final int SPEECH_REQUEST_CODE = 0;
//    @Override
//    protected void onStartListening(Intent intent, Callback callback) {
//
//        Log.d("Inside service","speech started");
//    }
//
//    @Override
//    protected void onCancel(Callback callback) {
//        Log.d("Inside service","speech cancel");
//    }
//
//    @Override
//    protected void onStopListening(Callback callback) {
//        Log.d("Inside service","speech stopped");
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        Log.d("Inside service","onstart command");
//        return START_NOT_STICKY;
//    }

        private final static String TAG="SpeechService";
        protected static AudioManager mAudioManager;
        protected SpeechRecognizer mSpeechRecognizer;
        protected Intent mSpeechRecognizerIntent;
        protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

        protected boolean mIsListening;
        protected volatile boolean mIsCountDownOn;
        private static boolean mIsStreamSolo;

        static final int MSG_RECOGNIZER_START_LISTENING = 1;
        static final int MSG_RECOGNIZER_CANCEL = 2;

        @Override
        public void onCreate()
        {
            super.onCreate();
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
            mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    this.getPackageName());
            Log.d(TAG, "service on create"); //$NON-NLS-1$

            mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

        }

        protected static class IncomingHandler extends Handler
        {
            private WeakReference<SpeechService> mtarget;

            IncomingHandler(SpeechService target)
            {
                mtarget = new WeakReference<SpeechService>(target);
            }


            @Override
            public void handleMessage(Message msg)
            {
                final SpeechService target = mtarget.get();
                Log.d(TAG, "inside incoming handler"); //$NON-NLS-1$
                switch (msg.what)
                {
                    case MSG_RECOGNIZER_START_LISTENING:

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        {
                            // turn off beep sound
                            if (!mIsStreamSolo)
                            {
                                mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
                                mIsStreamSolo = true;
                            }
                        }
                        if (!target.mIsListening)
                        {
                            target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                            target.mIsListening = true;
                            Log.d(TAG, "message start listening"); //$NON-NLS-1$
                        }
                        break;

                    case MSG_RECOGNIZER_CANCEL:
                        if (mIsStreamSolo)
                        {
                            mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
                            mIsStreamSolo = false;
                        }
                        target.mSpeechRecognizer.cancel();
                        target.mIsListening = false;
                        Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
                        break;
                }
            }
        }

        // Count down timer for Jelly Bean work around
        protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000)
        {

            @Override
            public void onTick(long millisUntilFinished)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onFinish()
            {
                mIsCountDownOn = false;
                Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
                try
                {
                    mServerMessenger.send(message);
                    message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                    mServerMessenger.send(message);
                }
                catch (RemoteException e)
                {

                }
            }
        };

        @Override
        public void onDestroy()
        {
            super.onDestroy();

            if (mIsCountDownOn)
            {
                mNoSpeechCountDown.cancel();
            }
            if (mSpeechRecognizer != null)
            {
                mSpeechRecognizer.destroy();
            }
        }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected class SpeechRecognitionListener implements RecognitionListener
        {

            @Override
            public void onBeginningOfSpeech()
            {
                // speech input will be processed, so there is no need for count down anymore
                if (mIsCountDownOn)
                {
                    mIsCountDownOn = false;
                    mNoSpeechCountDown.cancel();
                }
                Log.d(TAG, "onBeginingOfSpeech"); //$NON-NLS-1$
            }

            @Override
            public void onBufferReceived(byte[] buffer)
            {

            }

            @Override
            public void onEndOfSpeech()
            {
                Log.d(TAG, "onEndOfSpeech"); //$NON-NLS-1$
            }

            @Override
            public void onError(int error)
            {
                if (mIsCountDownOn)
                {
                    mIsCountDownOn = false;
                    mNoSpeechCountDown.cancel();
                }
                mIsListening = false;
              //  Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                try
                {
                //    mServerMessenger.send(message);
                }
                catch (Exception e)
                {

                }
                Log.d(TAG, "error = " + error); //$NON-NLS-1$
            }

            @Override
            public void onEvent(int eventType, Bundle params)
            {

            }

            @Override
            public void onPartialResults(Bundle partialResults)
            {

            }

            @Override
            public void onReadyForSpeech(Bundle params)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                {
                    mIsCountDownOn = true;
                    mNoSpeechCountDown.start();

                }
                Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
            }

            @Override
            public void onResults(Bundle results)
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
                      //  Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            }

            @Override
            public void onRmsChanged(float rmsdB)
            {

            }

        }
    }

