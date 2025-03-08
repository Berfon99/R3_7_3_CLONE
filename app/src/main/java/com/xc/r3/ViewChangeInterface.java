package com.xc.r3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.CheckBox;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.Objects;

@SuppressLint("ViewConstructor")
public class ViewChangeInterface extends ConstraintLayout {

    private final CheckBox cbMode;

    public ViewChangeInterface(Context context, String mode, String theme) {
        super(context);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Objects.requireNonNull(layoutInflater).inflate(R.layout.vue_confirmation_changement, this, true);

        cbMode = findViewById(R.id.cbMode);

        cbMode.setText(mode);
    }

    public boolean isModeChecked() {
        return this.cbMode.isChecked();
    }

}
