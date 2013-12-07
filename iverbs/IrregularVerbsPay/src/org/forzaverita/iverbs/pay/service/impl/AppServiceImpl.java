package org.forzaverita.iverbs.pay.service.impl;

import android.app.Application;
import android.database.Cursor;

import org.forzaverita.iverbs.pay.data.Lang;
import org.forzaverita.iverbs.pay.data.StatItem;
import org.forzaverita.iverbs.pay.data.Verb;
import org.forzaverita.iverbs.pay.database.Database;
import org.forzaverita.iverbs.pay.database.impl.SqliteDatabase;
import org.forzaverita.iverbs.pay.service.AppService;
import org.forzaverita.iverbs.pay.service.PreferencesService;
import org.forzaverita.iverbs.pay.train.TrainMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class AppServiceImpl extends Application implements AppService {
	
	private Database database;
	
	private PreferencesService preferences;
	
	private Random random = new Random();
	
	private int currentId = 1;
	
	private int maxId;
	
	private Date preferenceChangeDate;
	
	private List<Integer> verbsInTraining = new ArrayList<Integer>();
	
	@Override
	public void onCreate() {
		database = new SqliteDatabase(this);
		database.open();
		maxId = database.getMaxId();
		preferences = new PreferencesServiceImpl(this);
		List<Verb> verbs = database.getVerbs();
		for (Verb verb : verbs) {
			if (preferences.isInTraining(verb)) {
				verbsInTraining.add(verb.getId());				
			}			
		}
		super.onCreate();
	}
	
	@Override
	public List<Verb> getVerbs() {
		return database.getVerbs();
	}
	
	@Override
	public Verb getVerb(int id) {
		if (id != 0) {
			currentId = id;
		}
		return database.getVerb(currentId);
	}

	@Override
	public Verb getPreviousVerb() {
		if (--currentId < 1) {
			currentId = maxId;
		}
		return database.getVerb(currentId);
	}

	@Override
	public Verb getNextVerb() {
		if (++currentId > maxId) {
			currentId = 1;
		}
		return database.getVerb(currentId);
	}
	
	@Override
	public Cursor getCursorVerbsContains(String search) {
		return database.getCursorVerbsContains(search);
	}

	@Override
	public List<Verb> searchVerbs(String query) {
		return database.searchVerbs(query);
	}
	
	@Override
	public Verb getRandomVerb(Verb... excludes) {
		int index = random.nextInt(verbsInTraining.size());
		Verb verb = database.getVerb(verbsInTraining.get(index));
		if (verb != null && 
				! (excludes != null && Arrays.asList(excludes).contains(verb))) {
			return verb;
		}	
		return getRandomVerb(excludes);	
	}
	
	@Override
	public void addCorrect(int formQuest, Verb verb, TrainMode select) {
		preferences.addCorrect(formQuest, verb, select);
	}
	
	@Override
	public void addWrong(int formQuest, Verb verb, TrainMode select) {
		preferences.addWrong(formQuest, verb, select);
	}
	
	@Override
	public Lang getLanguage() {
		String lang = preferences.getLanguage();
		Lang result = null;
		if (lang != null) {
			result = Lang.valueOf(lang);			
		}
		if (result == null) {
			String locale = Locale.getDefault().getLanguage();
			result = Lang.tryValueOf(locale);
		}
		if (result == null) {
			result = Lang.RU;
		}
		return result;
	}
	

	@Override
	public void setLanguage(Lang lang) {
		preferences.setLanguage(lang.name());
	}
	
	@Override
	public boolean isLanguagePrefered() {
		return preferences.getLanguage() != null;
	}
	
	@Override
	public boolean isPreferencesChanged(Date lastPreferencesCheck) {
		return preferenceChangeDate != null && preferenceChangeDate.after(lastPreferencesCheck);
	}
	
	@Override
	public void preferencesChanged() {
		preferenceChangeDate = new Date();
	}
	
	@Override
	public float getSpeechRate() {
		return preferences.getSpeechRate();
	}
	
	@Override
	public float getPitch() {
		return preferences.getPitch();
	}
	
	@Override
	public List<StatItem> getStats() {
		List<StatItem> stats = new ArrayList<StatItem>();
		for (Verb verb : getVerbs()) {
			stats.add(preferences.getStat(verb));			
		}
		return stats;
	}
	
	@Override
	public void resetStats() {
		for (Verb verb : getVerbs()) {
			preferences.resetStat(verb);			
		}
	}
	
	@Override
	public void setInTraining(Verb verb, boolean inTraining) {
		preferences.setInTraining(verb, inTraining);
		Integer id = verb.getId();
		if (inTraining) {
			if (! verbsInTraining.contains(id)) {
				verbsInTraining.add(id);
			}	
		}
		else {
			verbsInTraining.remove(id);
		}
	}

    @Override
    public void setInTrainingAll(boolean inTraining) {
        List<Verb> verbs = getVerbs();
        for (Verb verb : verbs) {
            setInTraining(verb, inTraining);
        }
    }

    @Override
	public int getInTrainingCount() {
		return verbsInTraining.size();
	}
	
	@Override
	public float getFontSize() {
		return preferences.getFontSize();
	}

    @Override
    public int getVerbsCount() {
        return maxId;
    }

}
