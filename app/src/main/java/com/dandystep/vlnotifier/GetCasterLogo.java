package com.dandystep.vlnotifier;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class GetCasterLogo extends AsyncTask<String, Integer, Bitmap> {
    //task to fetch caster logo
    private String mCasterName = null;

    GetCasterLogo(String caster) {
        mCasterName = caster;
    }

    protected Bitmap doInBackground(String... sourceURL) {
        Bitmap downloadedLogo = null;
        try {
            URL url = new URL(sourceURL[0]);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream input = conn.getInputStream();
            downloadedLogo = BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return downloadedLogo;
    }

    @Override
    protected void onPostExecute(Bitmap bit) {
        if (bit != null) {
            SQLiteDatabase db = MainActivity.GetDabase();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            CasterLogoCompressionWork workerThread = new CasterLogoCompressionWork(bit, outStream);     //run in separate thread so we don't hang UI while we compress
            workerThread.run();
            ContentValues vl = new ContentValues();
            vl.put("logoBitmap", outStream.toByteArray());
            db.update("casters", vl, "casterName = '" + mCasterName + "'", null);
            MainActivity.adapter.changeCursor(db.rawQuery("SELECT * FROM casters" + MainActivity.sortOrder, null));
        }
    }
}

class CasterLogoCompressionWork implements Runnable {
    Bitmap bit;
    ByteArrayOutputStream outStream;

    CasterLogoCompressionWork(Bitmap _bit, ByteArrayOutputStream _outStream) {
        bit = _bit;
        outStream = _outStream;
    }

    @Override
    public void run() {
        bit.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
    }
}
