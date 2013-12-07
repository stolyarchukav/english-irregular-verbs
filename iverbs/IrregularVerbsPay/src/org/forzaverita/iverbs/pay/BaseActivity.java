package org.forzaverita.iverbs.pay;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.forzaverita.iverbs.pay.data.Constants;
import org.forzaverita.iverbs.pay.learn.LearnActivity;
import org.forzaverita.iverbs.pay.preference.AppPreferenceActivity;
import org.forzaverita.iverbs.pay.preference.SelectLangDialog;
import org.forzaverita.iverbs.pay.service.AppService;
import org.forzaverita.iverbs.pay.train.TrainActivity;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import static android.speech.tts.TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS;

public abstract class BaseActivity extends FragmentActivity implements OnInitListener {

	private static final int MIN_COUNT_IN_TRAINING = 5;
	
	private TextToSpeech tts;
	protected AppService service;
	
	private Date lastPreferencesCheck = new Date();
	
	@Override
	protected void onResume() {
		super.onResume();
		if (service.isPreferencesChanged(lastPreferencesCheck)) {
			lastPreferencesCheck = new Date();
			startActivity(new Intent(this, getClass()));
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		service = (AppService) getApplicationContext();
		tts = new TextToSpeech(this, this);
		tts.setSpeechRate(service.getSpeechRate());
		tts.setPitch(service.getPitch());
        tts.setLanguage(Locale.US);
		if (! service.isLanguagePrefered()) {
			startActivity(new Intent(this, SelectLangDialog.class));
		}
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	protected void onDestroy() {
		if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case R.id.menu_table :
                startActivity(new Intent(this, TableActivity.class));
                break;
            case R.id.menu_train :
                startActivity(new Intent(this, TrainActivity.class));
                break;
			case R.id.menu_scores :
                startActivity(new Intent(this, ScoresActivity.class));
                break;
            case R.id.menu_search :
                onSearchRequested();
                break;
            case R.id.menu_info :
                startActivity(new Intent(this, InfoActivity.class));
			    break;
            case R.id.menu_settings :
                startActivity(new Intent(this, AppPreferenceActivity.class));
                break;
            case R.id.menu_rate :
                rateApp();
                break;
            case R.id.menu_more_apps :
                moreApps();
                break;
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (upIntent != null) {
                    if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                        TaskStackBuilder.create(this)
                                .addNextIntentWithParentStack(upIntent)
                                .startActivities();
                    } else {
                        NavUtils.navigateUpTo(this, upIntent);
                    }
                }
                break;
		}
		return true;
	}

    public void onClickMode(View view) {
		switch (view.getId()) {
		case R.id.dashboard_button_table:
			startActivity(new Intent(getApplicationContext(),
					TableActivity.class));
			break;
		case R.id.dashboard_button_learn:
			startActivity(new Intent(getApplicationContext(),
					LearnActivity.class));
			break;
		case R.id.dashboard_button_train:
			tryStartTraining();
			break;
		case R.id.dashboard_button_scores:
			startActivity(new Intent(getApplicationContext(),
					ScoresActivity.class));
			break;
		}
	}

	private void tryStartTraining() {
		if (service.getInTrainingCount() < MIN_COUNT_IN_TRAINING) {
			String title = String.format(getString(R.string.train_min_dialog_title), MIN_COUNT_IN_TRAINING);
			new AlertDialog.Builder(this).setTitle(title).setPositiveButton(R.string.train_min_dialog_button,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(BaseActivity.this, ScoresActivity.class).
									addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP));
						}
					}).create().show();
		} else {
			startActivity(new Intent(getApplicationContext(),
					TrainActivity.class));
		}
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.ENGLISH);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e(Constants.LOG_TAG, "This Language is not supported");
			} else {
				setVolumeControlStream(AudioManager.STREAM_MUSIC);
			}
		} else {
			Log.e(Constants.LOG_TAG, "Initilization Failed!");
		}
	}

	public final void speak(String text) {
        HashMap<String, String> params = new HashMap<String, String>();
        Set<String> features = tts.getFeatures(Locale.US);
        if (features != null && features.contains(KEY_FEATURE_NETWORK_SYNTHESIS)) {
            params.put(KEY_FEATURE_NETWORK_SYNTHESIS, "true");
        }
        tts.speak(text, TextToSpeech.QUEUE_ADD, params);
	}

	public void onClickRateApp(View view) {
    	rateApp();
	}

    private void rateApp() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +
                    getApplicationInfo().packageName)));
        } catch (ActivityNotFoundException e) {
            Log.w(Constants.LOG_TAG, "Can't open market app page");
        }
    }

    private void moreApps() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://search?q=pub:ForzaVerita")));
        } catch (ActivityNotFoundException e) {
            Log.w(Constants.LOG_TAG, "Can't open market app page");
        }
    }

}
