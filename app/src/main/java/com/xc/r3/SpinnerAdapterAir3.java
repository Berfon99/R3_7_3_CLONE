package com.xc.r3;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;

public class SpinnerAdapterAir3 extends BaseAdapter implements SpinnerAdapter {
    private final String[] libelles;
    private final int[] icones;
    private final Context context;

    public SpinnerAdapterAir3(Context mainActivity, int[] icones, String[] libelles) {
        super();
        this.context = mainActivity;
        this.icones = icones;
        this.libelles = libelles;
    }

    @Override
    public int getCount() {
        return this.icones.length;
    }

    @Override
    public Object getItem(int i) {
        return this.libelles[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        int imageView = this.icones[i];
        String libelleDevise = this.libelles[i];
        return new ViewLayoutTheme(this.context, imageView, libelleDevise);
    }
}
