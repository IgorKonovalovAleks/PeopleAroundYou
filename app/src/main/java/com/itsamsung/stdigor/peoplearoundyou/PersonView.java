package com.itsamsung.stdigor.peoplearoundyou;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

public class PersonView extends View {
    String name, status, latitude, longitude;

    public PersonView(Context context, Person person) {
        super(context);
        name = person.nickname;
        status = person.status;
        latitude = Double.toString(person.latitude);
        longitude = Double.toString(person.longitude);
        init(null, 0);
    }

    public PersonView(Context context, AttributeSet attrs, Person person) {
        super(context, attrs);
        name = person.nickname;
        status = person.status;
        latitude = Double.toString(person.latitude);
        longitude = Double.toString(person.longitude);
        init(attrs, 0);
    }

    public PersonView(Context context, AttributeSet attrs, int defStyle, Person person) {
        super(context, attrs, defStyle);
        name = person.nickname;
        status = person.status;
        latitude = Double.toString(person.latitude);
        longitude = Double.toString(person.longitude);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}
