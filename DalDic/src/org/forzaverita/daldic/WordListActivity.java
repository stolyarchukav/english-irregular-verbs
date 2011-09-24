package org.forzaverita.daldic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import org.forzaverita.daldic.preferences.AppPreferenceActivity;
import org.forzaverita.daldic.service.Constants;
import org.forzaverita.daldic.service.DalDicService;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WordListActivity extends ListActivity {
	
	private static final int MARGIN = 5;
    
	private DalDicService service;
	private LayoutInflater inflater;
	private LinearLayout parent;
	
	private class SearchTask extends AsyncTask<Void, Void, Map<Integer, String>> {
    	
    	ProgressDialog dialog;
    	String queryString;
    	
    	@Override
    	protected void onPreExecute() {
    		dialog = ProgressDialog.show(WordListActivity.this, 
    				getString(R.string.progress_title), getString(R.string.progress_text));
    	}
    	
    	@Override
    	protected Map<Integer, String> doInBackground(Void... paramArrayOfParams) {
    		service = (DalDicService) getApplicationContext();
    		Map<Integer, String> words = null;
    		Intent intent = getIntent();
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            	String query = intent.getStringExtra(SearchManager.QUERY);
            	words = service.getWordsBeginWith(query);
            	queryString = query;
            }
            else {
            	Character letter = (Character) intent.getExtras().get(Constants.LETTER);
                if (letter != null) {
                	words = service.getWordsBeginWith(letter);
                	queryString = String.valueOf(letter);
                }
                else {
                	String begin = (String) intent.getExtras().get(Constants.BEGIN);
                	words = service.getWordsBeginWith(begin);
                	queryString = begin;
                }
            }
            return words;
    	}
    	
    	@Override
    	protected void onPostExecute(Map<Integer, String> words) {
    		dialog.dismiss();
    		TextView textView = (TextView) parent.findViewById(R.id.word_not_found);
    		if (words != null && ! words.isEmpty()) {
    			ArrayList<Entry<Integer, String>> wordList = new ArrayList<Entry<Integer, String>>(
    					words.entrySet());
    			Collections.sort(wordList, new Comparator<Entry<Integer, String>>() {
    				@Override
    				public int compare(Entry<Integer, String> object1,
    						Entry<Integer, String> object2) {
    					return object1.getValue().compareTo(object2.getValue());
    				}
				});
    			setListAdapter(new ArrayAdapter<Entry<Integer, String>>(WordListActivity.this, R.layout.wordlist_item, wordList) {
                	@Override
                	public View getView(int position, View convertView, ViewGroup parent) {
                		View row;
                        if (convertView == null) {
                        	row =  inflater.inflate(R.layout.wordlist_item, null);
                        }
                        else {
                        	row = convertView;
                        }
                        
                        TextView tv = (TextView) row.findViewById(android.R.id.text1);
                        tv.setText(getItem(position).getValue());
                        tv.setTypeface(service.getFont());
                        tv.setTextColor(Color.BLACK);
                        
                        row.setTag(getItem(position).getKey());
                        row.setOnClickListener(new View.OnClickListener() {
    						@Override
    						public void onClick(View paramView) {
    							startWordActivity((Integer) paramView.getTag());
    						}
    					});
                        row.setPadding(MARGIN, MARGIN, MARGIN, MARGIN);
                        return row;
                	}
                });
    			textView.setVisibility(View.GONE);
            }
            else {
            	textView.setText(getString(R.string.word_not_found) + ": " + queryString);
            	textView.setTypeface(service.getFont());
            	textView.setVisibility(View.VISIBLE);
            }
    		configureSearchFullButton();
    	}

		private void configureSearchFullButton() {
			Button searchFull = (Button) parent.findViewById(R.id.search_full);
    		searchFull.setTypeface(service.getFont());
    		searchFull.setVisibility(View.VISIBLE);
    		searchFull.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View paramView) {
					// TODO Auto-generated method stub
					
				}
			});
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wordlist);
        
        inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        parent = (LinearLayout) findViewById(R.id.wordlist);
        
        new SearchTask().execute();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			startActivity(new Intent(this, AppPreferenceActivity.class));
			return true;
		case R.id.menu_seacrh:
			onSearchRequested();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void startWordActivity(Integer wordId) {
		Intent intent = new Intent(this, WordActivity.class);
		intent.putExtra(Constants.WORD_ID, wordId);
		startActivity(intent);
	}
	
}