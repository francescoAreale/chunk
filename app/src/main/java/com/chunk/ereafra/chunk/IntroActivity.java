package com.chunk.ereafra.chunk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.github.paolorotolo.appintro.model.SliderPage;


public class IntroActivity extends AppIntro2 {


    public static final String PRESENTATION_DONE = "done3";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Instead of fragments, you can also use our default slide.
        // Just create a `SliderPage` and provide title, description, background and image.
        // AppIntro will do the rest.
        SliderPage sliderPage_welcome = new SliderPage();
        sliderPage_welcome.setTitle(getResources().getString(R.string.welcome));
        sliderPage_welcome.setDescription(getResources().getString(R.string.first_description));
        sliderPage_welcome.setImageDrawable(R.drawable.chunk_logo);
        sliderPage_welcome.setBgColor(getResources().getColor(R.color.colorPrimary));
        addSlide(AppIntroFragment.newInstance(sliderPage_welcome));

        SliderPage first_page = new SliderPage();
        first_page.setTitle(getResources().getString(R.string.title_1));
        first_page.setDescription(getResources().getString(R.string.description_1));
        first_page.setImageDrawable(R.drawable.map_chunk);
        first_page.setBgColor(getResources().getColor(R.color.colorPrimary));
        addSlide(AppIntroFragment.newInstance(first_page));

        SliderPage second_page = new SliderPage();
        second_page.setTitle(getResources().getString(R.string.title_2));
        second_page.setDescription(getResources().getString(R.string.description_2));
        second_page.setImageDrawable(R.drawable.menu_map);
        second_page.setBgColor(getResources().getColor(R.color.colorPrimary));
        addSlide(AppIntroFragment.newInstance(second_page));

        SliderPage thirth_page = new SliderPage();
        thirth_page.setTitle(getResources().getString(R.string.title_3));
        thirth_page.setDescription(getResources().getString(R.string.description_3));
        thirth_page.setImageDrawable(R.drawable.new_chunk);
        thirth_page.setBgColor(getResources().getColor(R.color.colorPrimary));
        addSlide(AppIntroFragment.newInstance(thirth_page));

        SliderPage fourth_page = new SliderPage();
        fourth_page.setTitle(getResources().getString(R.string.title_4));
        fourth_page.setDescription(getResources().getString(R.string.description_4));
        fourth_page.setImageDrawable(R.drawable.your_chunk);
        fourth_page.setBgColor(getResources().getColor(R.color.colorPrimary));
        addSlide(AppIntroFragment.newInstance(fourth_page));

        SliderPage fifth_page = new SliderPage();
        fifth_page.setTitle(getResources().getString(R.string.title_5));
        fifth_page.setDescription(getResources().getString(R.string.description_5));
        fifth_page.setImageDrawable(R.drawable.chat);
        fifth_page.setBgColor(getResources().getColor(R.color.colorPrimary));
        addSlide(AppIntroFragment.newInstance(fifth_page));

        // OPTIONAL METHODS
        // Override bar/separator color.
        setBarColor(getResources().getColor(R.color.colorPrimary));
        //setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);

        // Turn vibration on and set intensity.
        // NOTE: you will probably need to ask VIBRATE permission in Manifest.
        setVibrate(true);
        setVibrateIntensity(30);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PRESENTATION_DONE,PRESENTATION_DONE);
        editor.apply();     // This line is IMPORTANT !!!
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PRESENTATION_DONE,PRESENTATION_DONE);
        editor.apply();     // This line is IMPORTANT !!!
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    @Override
    public boolean onNavigateUp() {
        return super.onNavigateUp();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
