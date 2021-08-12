package com.dandystep.vlnotifier;

import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    static public SQLiteDatabase db;
    static public CustomAdapter adapter = null;
    ListView listView;
    private TextView emptyList = null;
    private FloatingActionButton fab = null;
    private static PackageManager pm = null;
    static String topicSuffix = "";
    static FragmentManager fragmentManager = null;
    static public boolean editMode = false;
    private MenuItem editIcon = null;
    private CoordinatorLayout rootView = null;

    //debug options
    final boolean logSubscriptionsWhenAppOpens = true;

    //app settings
    static private boolean usePuffin = true;
    static public String sortOrder = " ORDER BY _ID DESC";

    static public SQLiteDatabase GetDabase() {
        return db;
    }

    public void AboutButton(MenuItem v) {
        Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
        startActivity(intent);
    }

    public void PrivacyPolicy(MenuItem v) {
        Intent ppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dandystepco.tumblr.com/private/659239525754732544/tumblr_bxLeQDXn7EFc0mWxR"));
        startActivity(ppIntent);
    }

    static void ToastCenteredText(Context context, String text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        //the following code centered the text, but is deprecated as of Android R
        //TextView toastText = (TextView) toast.getView().findViewById(android.R.id.message);
        //toastText.setGravity(Gravity.CENTER_HORIZONTAL);
        toast.show();
    }

    static Intent OpenLink(String url) {
        Intent intent = null;
        if (usePuffin) {
            if (pm != null) {
                PackageInfo puffinInfo;
                try {
                    puffinInfo = pm.getPackageInfo("com.cloudmosa.puffin", 0);
                } catch (Exception e) {
                    try {
                        puffinInfo = pm.getPackageInfo("com.cloudmosa.puffinFree", 0);
                    } catch (Exception ee) {
                        puffinInfo = null;
                    }
                }

                if (puffinInfo != null) {
                    intent = pm.getLaunchIntentForPackage(puffinInfo.packageName);
                    if (intent != null) {
                        intent.setData(Uri.parse(url));
                    }
                }
            }
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
        }
        return intent;
    }

    private void AddCaster() {
        if (adapter == null) {
            PopulateList();
        }
        final AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setTitle("Enter the channel's name:\n");
        final EditText txt = new EditText(this);
        txt.setInputType(InputType.TYPE_CLASS_TEXT);
        dlgBuilder.setView(txt);
        dlgBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //check for length (18 chars max)
                if (txt.getText().length() > 18) {
                    ToastCenteredText(getApplicationContext(), "Channel name too long (18 chars max)", Toast.LENGTH_LONG);
                    return;
                }

                //check for whitespace
                boolean hasWhitespace = false;
                for (char c : (txt.getText().toString()).toCharArray()) {
                    if (Character.isWhitespace(c)) {
                        hasWhitespace = true;
                    }
                }

                if (hasWhitespace) {
                    ToastCenteredText(getApplicationContext(), "Channels can't have spaces in their names", Toast.LENGTH_LONG);
                    return;
                }

                //check for duplicate
                Cursor cursor = db.rawQuery("SELECT * FROM casters", null);
                for (int i = 0; i < cursor.getCount(); i++) {
                    cursor.moveToNext();
                    if (cursor.getString(cursor.getColumnIndex("casterName")).equals(txt.getText().toString())) {
                        ToastCenteredText(getApplicationContext(), "That channel is already on the list", Toast.LENGTH_LONG);
                        cursor.close();
                        dialog.dismiss();
                        return;
                    }
                }
                cursor.close();

                //check if channel exists and carry on if true
                if (CheckIfChannelExists(txt.getText().toString())) {
                    ContentValues vl = new ContentValues();
                    vl.put("casterName", txt.getText().toString());
                    vl.put("notificationState", true);
                    db.insert("casters", null, vl);
                    adapter.changeCursor(db.rawQuery("SELECT * FROM casters" + sortOrder, null));
                    FirebaseMessaging.getInstance().subscribeToTopic(txt.getText().toString() + topicSuffix);

                    //fetch logo
                    GetCasterLogo getLogo = new GetCasterLogo(txt.getText().toString());
                    ToastCenteredText(getApplicationContext(), "Subscribed to " + txt.getText().toString() + "!", Toast.LENGTH_SHORT);
                    getLogo.execute("https://cdn.vaughnsoft.com/vaughnsoft/vaughn/img_profiles/" + txt.getText().toString() + "_320.jpg");
                    if (emptyList.getVisibility() == View.VISIBLE) {
                        emptyList.setVisibility(View.INVISIBLE);
                    }

                    if (editIcon != null) {
                        editIcon.setVisible(true);
                    }
                    PopulateList();
                } else {
                    ToastCenteredText(getApplicationContext(), "ERROR: Channel \"" + txt.getText().toString() +"\" doesn't exist or has been closed!", Toast.LENGTH_LONG);
                }
            }
        });
        AlertDialog dialog = dlgBuilder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        dialog.show();
    }

    private Boolean CheckIfChannelExists(String channel) {
        boolean res;
        CheckChannelAsync task = new CheckChannelAsync();
        try {
            res = task.execute(channel).get();
        } catch (Exception e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }

    public void Clicked(View v) {
        //play animation
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        Animation fabAnim = AnimationUtils.loadAnimation(this, R.anim.fab_press);
        fabAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                AddCaster();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        fab.startAnimation(fabAnim);
    }

    private void PopulateList() {
        adapter = new CustomAdapter(getApplicationContext(), R.layout.subscription_item, db.rawQuery("SELECT * FROM casters" + sortOrder, null), new String[] {"casterName"}, new int[] {R.id.label}, 0);
        listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
    }

    private void SubscribeToTopics() {
        Cursor cur = db.rawQuery("SELECT * FROM casters WHERE notificationState = 1", null);
        cur.moveToNext();
        for (int i = 0; i < cur.getCount(); i++) {
            if (topicSuffix.equals("_dev") && logSubscriptionsWhenAppOpens) {
                ToastCenteredText(getApplicationContext(), "Subbing to " + cur.getString(cur.getColumnIndex("casterName")) + topicSuffix, Toast.LENGTH_SHORT);
            }
            FirebaseMessaging.getInstance().subscribeToTopic(cur.getString(cur.getColumnIndex("casterName")) + topicSuffix);
            cur.moveToNext();
        }
        cur.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem puffinItem = menu.findItem(R.id.puffin_menu_item);
        puffinItem.setChecked(usePuffin);
        editIcon = menu.findItem(R.id.edit_subs_button);
        if (emptyList.getVisibility() == View.INVISIBLE) {
            editIcon.setVisible(true);
        } else {
            editIcon.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.edit_subs_button) {
            editMode = !editMode;
            if (editMode) { //hide/show fab animation
                fab.hide();
            } else {
                fab.show();
            }
            adapter.changeCursor(db.rawQuery("SELECT * FROM casters" + sortOrder, null)); //redraw list
        } else if (id == R.id.puffin_menu_item) {
            usePuffin =! usePuffin;
            item.setChecked(usePuffin);
        } else if (id == R.id.alphabetical) {   //sorting menu
            switch(sortOrder) {
                case " ORDER BY casterName ASC":
                    sortOrder = " ORDER BY casterName DESC";
                    break;
                case " ORDER BY casterName DESC":
                    sortOrder = " ORDER BY casterName ASC";
                    break;
                default:
                    sortOrder = " ORDER BY casterName ASC";
            }
            if (adapter != null) {
                adapter.changeCursor(db.rawQuery("SELECT * FROM casters" + sortOrder, null));
            }
        } else if (id == R.id.date_added) {
            switch (sortOrder) {
                case " ORDER BY _ID ASC":
                    sortOrder = " ORDER BY _ID DESC";
                    break;
                case " ORDER BY _ID DESC":
                    sortOrder = " ORDER BY _ID ASC";
                    break;
                default:
                    sortOrder = " ORDER BY _ID DESC";
            }
            if (adapter != null) {
                adapter.changeCursor(db.rawQuery("SELECT * FROM casters" + sortOrder, null));
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(getApplicationContext());
        setContentView(R.layout.activity_main);

        //TODO: Keep caster input window up in case of failure
        //TODO: Failure cases for web requests

        //handle subscription suffix
        String buildType = BuildConfig.BUILD_TYPE;
        Log.i("APP", "App starting in " + buildType + " mode");
        if (buildType.equals("debug")) {
            topicSuffix = "_dev";
        } else {
            topicSuffix = "_release";
        }
        FirebaseMessaging.getInstance().subscribeToTopic("allusers" + topicSuffix);

        //handle preferences
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        usePuffin = prefs.getBoolean("usePuffin", true);
        sortOrder = prefs.getString("sortOrder", " ORDER BY _ID DESC");

        //get database and initialize variables
        deleteDatabase("testdb.db");
        pm = getPackageManager();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        pm = getPackageManager();
        fragmentManager = getFragmentManager();
        emptyList = (TextView) findViewById(R.id.emptyListWarning);
        listView = (ListView) findViewById(R.id.list);
        rootView = (CoordinatorLayout) findViewById(R.id.main_activity_root);

        //see if we have populated database and fill list, else show warning view
        Cursor cursor = db.rawQuery("SELECT * FROM casters" + sortOrder, null);

        if (cursor.getCount() == 0) {
            emptyList.setVisibility(View.VISIBLE);
        } else {
            emptyList.setVisibility(View.INVISIBLE);
            PopulateList();
            SubscribeToTopics();
        }
        cursor.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        editMode = false;       //get out of edit mode if we go to the background
    }

    @Override
    protected void onStop() {
        super.onStop();

        //write preferences
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("usePuffin", usePuffin);
        editor.putString("sortOrder", sortOrder);
        editor.apply();
    }
}
