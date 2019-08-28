package com.example.rajat.rxjavaexmpl;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
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
    String mString = "<ul>\n" +
            "\t&#9679;\n" +
            "We see many different plants in our surroundings.<br/><br/>\t&#9679;\n" +
            "Plants are<strong> </strong>broadly classified into trees, shrubs, herbs, climbers and creepers.<br/><br/>\t&#9679;\n" +
            "Plants have two parts - root and shoot.<br/><br/>\t&#9679;\n" +
            "The root usually grows towards the soil and shoot grows towards the sunlight.<br/><br/></ul>\n";
    // String mString = "an enlightening experience. the process of receiving or giving systematic instruction, especially at a school or university. the theory and practice of teaching. Sachin ji Bhai. Shelendra Sir. Bhai bhai";
    /*String mString = "This technology is rapidly changing the world around us, many people worry that technology will replace human intelligence. Some educators worry that there will be no students to teach anymore in the near future as technology might take over a lot of tasks and abilities that we have been teaching our students for decades.";*/
    String[] mWordStrings, mSentenceString;
    int mSpan = 0;
    ImageButton mButton;
    boolean mStopTTS = false;
    private TextToSpeech mTextToSpeech;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.nameView);
        mButton = findViewById(R.id.buttonClick);
        mString = mString.replaceAll("  ", " ").trim();
        mTextView.setText(Html.fromHtml(mString));
        mWordStrings = android.text.Html.fromHtml(mString).toString().split(" ");
        /*Pattern p = Pattern.compile("(?<=\\w[\\w\\)\\]][\\.\\?\\!]\\s)");
        mSentenceString = p.split(mString);*/
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

    @SuppressLint("CheckResult")
    private void highlight(final String string) {

        Observable.create(new ObservableOnSubscribe<SpannableStringBuilder>() {
            @Override
            public void subscribe(final ObservableEmitter<SpannableStringBuilder> emitter) throws Exception {
                if (!TextUtils.isEmpty(string)) {
                    for (int j = 0; j < mWordStrings.length; j++) {
                        String speakWord = mWordStrings[j];

                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(android.text.Html.fromHtml(mString).toString());
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
                        // Toast.makeText(MainActivity.this,"s"+s, Toast.LENGTH_SHORT).show();
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
                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder((mString));
                        stringBuilder.setSpan(new StyleSpan(Typeface.NORMAL), 0, mString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mTextView.setText(Html.fromHtml(stringBuilder.toString()));
                        mSpan = 0;
                    }
                });
    }


    @SuppressLint("CheckResult")
    private void highlightSentence(final String string) {

        Observable.create(new ObservableOnSubscribe<SpannableStringBuilder>() {
            @Override
            public void subscribe(final ObservableEmitter<SpannableStringBuilder> emitter) throws Exception {
                if (!TextUtils.isEmpty(string)) {
                    for (int j = 0; j < mSentenceString.length; j++) {
                        String speakSentence = mSentenceString[j];
                        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(mString);
                        stringBuilder.setSpan(new ForegroundColorSpan(Color.BLUE), mSpan, mSpan + speakSentence.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mSpan = mSpan + speakSentence.length() + 1;
                        emitter.onNext(stringBuilder);

                        synchronized (emitter) {
                            /*For English*/
                           /* if (speakSentence.contains("etc.") || speakSentence.contains(".") || speakSentence.contains("?") || speakSentence.contains("!!!") || speakSentence.contains("।") || speakSentence.contains("|")) {
                                emitter.wait(1000);

                            } else {
                                emitter.wait(speakSentence.length() * 70);
                            }*/
                            emitter.wait(speakSentence.length() * 100);

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
            mButton.setVisibility(View.VISIBLE);
            mButton.setEnabled(true);
        } else {
            Log.e("Error", "onInit: Some kind of error");
            mButton.setVisibility(View.GONE);
            mButton.setEnabled(false);
        }
    }

    private void speakLoud(String s) {
        final HashMap<String, String> hash = new HashMap<String, String>();
        hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
        hash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "FINISH");
        if (!TextUtils.isEmpty(s)) {

            mTextToSpeech.speak(android.text.Html.fromHtml(s).toString(), TextToSpeech.QUEUE_ADD, hash);
            mTextToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String s) {
                    highlight(mString);
                }

                @Override
                public void onDone(String s) {

                }

                @Override
                public void onError(String s) {

                }
            });

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

    @Override
    protected void onStop() {
        super.onStop();
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
