package com.itsamsung.stdigor.peoplearoundyou;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PersonView extends RelativeLayout {

    String name, status, latitude, longitude;
    TextView nameLabel, statusLabel, locationLabel;

    public PersonView(Context context, Person person) {
        super(context);
        init(person);
    }

    private void init(Person person) {
        // Load attributes
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_person, this);
        name = person.nickname;
        status = person.status;
        latitude = Double.toString(person.latitude);
        longitude = Double.toString(person.longitude);
        nameLabel = findViewById(R.id.name);
        statusLabel = findViewById(R.id.status);
        locationLabel = findViewById(R.id.location);
        nameLabel.setText(name);
        statusLabel.setText(status);
        locationLabel.setText(latitude + "\n" + longitude);
    }
}
