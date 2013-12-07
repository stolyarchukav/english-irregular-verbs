package org.forzaverita.iverbs.pay.learn;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.forzaverita.iverbs.pay.data.Verb;
import org.forzaverita.iverbs.pay.service.AppService;

public class LearnPagerAdapter extends FragmentStatePagerAdapter {

    private AppService service;

    public LearnPagerAdapter(FragmentManager fm, AppService service) {
        super(fm);
        this.service = service;
    }

    @Override
    public Fragment getItem(int i) {
        Verb verb = service.getVerb(i + 1);
        return new LearnFragment(verb);
    }

    @Override
    public int getCount() {
        return service.getVerbsCount();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Verb verb = service.getVerb(position + 1);
        return verb.getForm1() + " / " + verb.getTranslation();
    }

}
