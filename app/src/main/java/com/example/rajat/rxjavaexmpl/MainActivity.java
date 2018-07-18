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
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener {

    TextView mTextView;
    String mString = "Paragraphs are the building blocks of papers. Many students define paragraphs in terms of length: a paragraph is a group of at least five sentences, a paragraph is half a page long, etc. In reality, though, the unity and coherence of ideas among sentences is what constitutes a paragraph. A paragraph is defined as “a group of sentences or a single sentence that forms a unit” (Lunsford and Connors 116).";

    String[] mWordStrings;
    int mSpan = 0;
    private TextToSpeech mTextToSpeech;
    ImageButton mButton;

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
                } else if (mButton.getTag().equals("stop")) {
                    stopSpeech();
                    mButton.setTag("play");
                    mButton.setImageResource(R.drawable.action_play);
                    highlight("");
                }
            }
        });
    }

    private void highlight(final String string) {

        Observable.create(new ObservableOnSubscribe<SpannableStringBuilder>() {
            @Override
            public void subscribe(final ObservableEmitter<SpannableStringBuilder> emitter) throws Exception {
                if (!TextUtils.isEmpty(string)) {
                    int i, j;
                    for (j = 0; j < mWordStrings.length; j++) {
                    /*new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SpannableStringBuilder stringBuilder = new SpannableStringBuilder(mString);
                            stringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0,5,*//*mSpan, mSpan + mWordStrings[finalJ].length(),*//* Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            emitter.onNext(stringBuilder);
                            emitter.onComplete();
                            mSpan = mSpan + mWordStrings[finalJ].length();
                        }
                    }, 2000);*/

                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(mString);
                        stringBuilder.setSpan(new ForegroundColorSpan(Color.BLUE), mSpan, mSpan + mWordStrings[j].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mSpan = mSpan + mWordStrings[j].length() + 1;
                        emitter.onNext(stringBuilder);

                        synchronized (emitter) {
                            /*For English*/
                            if (mWordStrings[j].contains("etc.") || mWordStrings[j].contains(".") || mWordStrings[j].contains("?") || mWordStrings[j].contains("!!!") || mWordStrings[j].contains("।") || mWordStrings[j].contains("|")) {
                                emitter.wait(900);

                            } else {
                                emitter.wait(mWordStrings[j].length() * 75);
                            }

                            /*for hindi*/
                           /* if (mWordStrings[j].contains(".") || mWordStrings[j].contains("?") || mWordStrings[j].contains("!!!") || mWordStrings[j].contains("।") || mWordStrings[j].contains("|")) {
                                emitter.wait(900);
                            } else {
                                emitter.wait(mWordStrings[j].length() * 90);
                            }*/
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
            mTextToSpeech.setSpeechRate(0.8f);

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("After Success", "onInit: lang no available");
            }
            mButton.setEnabled(true);
        } else {
            Log.e("Error", "onInit: Some kind of error");
        }
    }

    private void speakLoud(String s) {
        HashMap<String, String> hash = new HashMap<String, String>();
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
        //stopSpeech();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mButton.setTag("play");
                mButton.setImageResource(R.drawable.action_play);
            }
        });
        // highlight("");
    }
}
