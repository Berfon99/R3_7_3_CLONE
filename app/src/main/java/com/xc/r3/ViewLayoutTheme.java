package com.xc.r3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

@SuppressLint("ViewConstructor")
public class ViewLayoutTheme extends ConstraintLayout {

    public ViewLayoutTheme(Context context, int imageView, String libelleDevise) {
        super(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Objects.requireNonNull(layoutInflater).inflate(R.layout.vue_layout_theme, this, true);

        ImageView imageViewDevise = findViewById(R.id.imageViewSpinner);
        TextView textViewDevise = findViewById(R.id.textViewSpinner);

        @SuppressLint("UseCompatLoadingForDrawables") Drawable image = context.getResources().getDrawable(imageView, null);
        imageViewDevise.setImageDrawable(image);
        textViewDevise.setText(libelleDevise);
    }
}
