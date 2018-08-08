package io.github.giusan82.easytrip.ui;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.giusan82.easytrip.BuildConfig;
import io.github.giusan82.easytrip.R;
import io.github.giusan82.easytrip.data.WeatherData;
import io.github.giusan82.easytrip.services.Tasks;
import timber.log.Timber;

public class SplashActivity extends AppCompatActivity {
    private static final int TIMEOUT = 3000;

    private Window mWindow;

    @BindView(R.id.iv_splash_background)
    ImageView mSplashBackground;
    @BindView(R.id.tv_citation)
    TextView mCitation;
    @BindView(R.id.tv_author)
    TextView mAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        mWindow = this.getWindow();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + "|" + element.getMethodName() + "|" + element.getLineNumber();
                }
            });
        }
        final Intent intent = new Intent(this, MainActivity.class);

        if(Build.VERSION.SDK_INT >= 21){
            Palette.from(((BitmapDrawable) mSplashBackground.getDrawable()).getBitmap()).generate(new Palette.PaletteAsyncListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onGenerated(Palette palette) {
                    mWindow.setStatusBarColor(palette.getDarkMutedSwatch().getRgb());
                }
            });
        }

        String weather_uri = WeatherData.getUri(this);
        if(!weather_uri.isEmpty()){
            Timber.d("JobDispatcher starting...");
            Tasks.sheduleUpdateWeather(this, true);
        }

        String[] citations = getResources().getStringArray((R.array.citations_sentence));
        String[] authors = getResources().getStringArray((R.array.citation_authors));
        int id = new Random().nextInt(citations.length);
        mCitation.setText(citations[id]);
        mAuthor.setText(authors[id]);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, TIMEOUT);
    }
}
