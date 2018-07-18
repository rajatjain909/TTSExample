package com.example.rajat.rxjavaexmpl;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

    TextView mTextView;
    String mString = "As technology is rapidly changing the world around us, many people worry that technology will replace human intelligence. Some educators worry that there will be no students to teach anymore in the near future as technology might take over a lot of tasks and abilities that we have been teaching our students for decades.";
    String[] mWordStrings;
    int mSpan = 0;
    private TextToSpeech mTextToSpeech;
    ImageButton mButton;
    boolean mStopTTS = false;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.nameView);
        mButton = findViewById(R.id.buttonClick);
        mString = mString.replaceAll("  ", " ").trim();
        mTextView.setText(mString);
        mWordStrings = mString.split(" ");
        mTextToSpeech = new TextToSpeech(this, this);
        mButton.setTag("play");
        mButton.setImageResource(R.drawable.action_play);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mButton.getTag().equals("play")) {
                    speakLoud(mString);
                    mButton.setTag("stop");
                    mButton.setImageResource(R.drawable.action_stop);
                    highlight(mString);
                    mStopTTS = false;
                } else if (mButton.getTag().equals("stop")) {
                    stopSpeech();
                    mButton.setTag("play");
                    mButton.setImageResource(R.drawable.action_play);
                    mStopTTS = true;

                }
            }
        });
    }

    private void highlight(final String string) {

        Observable.create(new ObservableOnSubscribe<SpannableStringBuilder>() {
            @Override
            public void subscribe(final ObservableEmitter<SpannableStringBuilder> emitter) throws Exception {
                if (!TextUtils.isEmpty(string)) {
                    for (int j = 0; j < mWordStrings.length; j++) {
                        String speakWord = mWordStrings[j];
                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(mString);
                        stringBuilder.setSpan(new ForegroundColorSpan(Color.BLUE), mSpan, mSpan + speakWord.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mSpan = mSpan + speakWord.length() + 1;
                        emitter.onNext(stringBuilder);

                        synchronized (emitter) {
                            /*For English*/
                            if (speakWord.contains("etc.") || speakWord.contains(".") || speakWord.contains("?") || speakWord.contains("!!!") || speakWord.contains("।") || speakWord.contains("|")) {
                                emitter.wait(1000);

                            } else {
                                emitter.wait(speakWord.length() * 70);
                            }

                            /*for hindi*/
                           /* if (speakWord.contains(".") || speakWord.contains("?") || speakWord.contains("!!!") || speakWord.contains("।") || speakWord.contains("|")) {
                                emitter.wait(900);
                            } else {
                                emitter.wait(speakWord.length() * 90);
                            }*/
                        }
                        if (mStopTTS) {
                            break;
                        }

                    }
                }

                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<SpannableStringBuilder>() {
                    @Override
                    public void accept(SpannableStringBuilder s) throws Exception {
                        mTextView.setText(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        //Toast.makeText(MainActivity.this,"Success", Toast.LENGTH_SHORT).show();
                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(mString);
                        stringBuilder.setSpan(new StyleSpan(Typeface.NORMAL), 0, mString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mTextView.setText(stringBuilder);
                        mSpan = 0;
                    }
                }, new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {

                    }
                });
    }


    @Override
    public void onInit(int i) {
        if (i == TextToSpeech.SUCCESS) {
            int result;
            if (mTextToSpeech.isLanguageAvailable(new Locale("hi_IN")) == TextToSpeech.LANG_AVAILABLE) {
                result = mTextToSpeech.setLanguage(new Locale("hi_IN"));
            } else {
                result = mTextToSpeech.setLanguage(Locale.US);
            }
            //  int result = mTextToSpeech.setLanguage(Locale.ENGLISH);
            mTextToSpeech.setPitch(1.0f);
            mTextToSpeech.setSpeechRate(0.9f);
            mTextToSpeech.setOnUtteranceCompletedListener(this);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("After Success", "onInit: lang no available");
            }
            mButton.setEnabled(true);
        } else {
            Log.e("Error", "onInit: Some kind of error");
        }
    }

    private void speakLoud(String s) {
        final HashMap<String, String> hash = new HashMap<String, String>();
        hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
        hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FINISH");
        if (!TextUtils.isEmpty(s)) {
            mTextToSpeech.speak(s, TextToSpeech.QUEUE_ADD, hash);

        } else {
            mTextToSpeech.speak("No Data", TextToSpeech.QUEUE_ADD, hash);
        }
    }

    @Override
    protected void onDestroy() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        super.onDestroy();
    }

    public void stopSpeech() {
        mTextToSpeech.playSilentUtterance(1500, TextToSpeech.QUEUE_FLUSH, null);

    }


    @Override
    public void onUtteranceCompleted(String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButton.setTag("play");
                mButton.setImageResource(R.drawable.action_play);
            }
        });
    }
}
