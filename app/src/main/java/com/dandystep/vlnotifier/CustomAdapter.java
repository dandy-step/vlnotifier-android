package com.dandystep.vlnotifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

class CustomAdapter extends SimpleCursorAdapter {

    CustomAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        final Bundle infoDialogFragmentArgs = new Bundle();

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!MainActivity.editMode) {
                    //show info dialog fragment
                    android.app.FragmentTransaction fragTrans = MainActivity.fragmentManager.beginTransaction();
                    WindowFragment windowFrag = new WindowFragment();
                    TextView casterName = (TextView)view.findViewById(R.id.label);
                    infoDialogFragmentArgs.putString("casterName", casterName.getText().toString()
                    );
                    windowFrag.setArguments(infoDialogFragmentArgs);
                    fragTrans.add(windowFrag, null);
                    fragTrans.commit();
                }
            }
        });

        final CheckBox ch = (CheckBox) view.findViewById(R.id.checkbox);
        final TextView tx = (TextView) view.findViewById(R.id.label);
        Integer checkbox_index = cursor.getColumnIndex("notificationState");
        final Integer value = cursor.getInt(checkbox_index);
        if (MainActivity.editMode) {
            ch.setVisibility(View.VISIBLE);
        } else {
            ch.setVisibility(View.INVISIBLE);
        }

        //load logo if in database, else display null
        ImageView img = (ImageView) view.findViewById(R.id.caster_logo);
        if (cursor.getBlob(cursor.getColumnIndex("logoBitmap")) == null) {
            img.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.nophoto));
            infoDialogFragmentArgs.putByteArray("logoBitmap", null);
        } else {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(cursor.getBlob(cursor.getColumnIndex("logoBitmap")));
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();  //create outstream so we can read from the inputstream coming from database to the outputstream, and then use it to create the bitmap here and also insert it in the bundle to pass to the info fragment
            inputStream.reset();
            int bytesRead;
            byte[] buffer = new byte[1024];
            try {
                while ((bytesRead = inputStream.read(buffer)) != -1){
                    outStream.write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(outStream.toByteArray(), 0, outStream.size());
            infoDialogFragmentArgs.putByteArray("logoBitmap", outStream.toByteArray());
            img.setImageBitmap(bitmap);
        }


        //edit mode and checkbox handling
        if (MainActivity.editMode) {
            if (value == 1) {
                ch.setChecked(true);
            } else {
                ch.setChecked(false);
            }

            ch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ch.isChecked()) {
                        FirebaseMessaging.getInstance().subscribeToTopic(tx.getText().toString() + MainActivity.topicSuffix);
                        ContentValues vl = new ContentValues();
                        vl.put("notificationState", 1);
                        MainActivity.db.update("casters", vl, "casterName = '" + tx.getText() + "'", null);
                        MainActivity.adapter.changeCursor(MainActivity.db.rawQuery("SELECT * FROM casters" + MainActivity.sortOrder, null));
                        MainActivity.ToastCenteredText(context, "Subscribed to " + tx.getText().toString(), Toast.LENGTH_SHORT);
                    } else {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(tx.getText().toString() + MainActivity.topicSuffix);
                        ContentValues vl = new ContentValues();
                        vl.put("notificationState", 0);
                        MainActivity.db.update("casters", vl, "casterName = '" + tx.getText() + "'", null);
                        MainActivity.adapter.changeCursor(MainActivity.db.rawQuery("SELECT * FROM casters" + MainActivity.sortOrder, null));
                        MainActivity.ToastCenteredText(context, "Unsubscribed from " + tx.getText().toString(), Toast.LENGTH_SHORT);
                    }
                }
            });
        }
        cursor.moveToNext();
    }
}
