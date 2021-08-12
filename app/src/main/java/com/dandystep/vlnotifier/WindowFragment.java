package com.dandystep.vlnotifier;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import static com.dandystep.vlnotifier.MainActivity.adapter;

public class WindowFragment extends android.app.DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") final View infoDialog = inflater.inflate(R.layout.caster_info_view, null, false);
        final TextView casterName = (TextView) infoDialog.findViewById(R.id.caster_info_dlg_name);
        final TextView casterLink = (TextView) infoDialog.findViewById(R.id.caster_info_dlg_link);
        final TextView deleteButton = (TextView) infoDialog.findViewById(R.id.caster_info_dlg_delete_button);
        final ImageView casterLogo = (ImageView) infoDialog.findViewById(R.id.caster_info_dlg_logo);

        casterName.setText(getArguments().getString("casterName")); //set caster name
        casterLink.setText(String.format(getString(R.string.vl_link_concatenate), casterName.getText()));
        casterLink.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);           //set underlined
        casterLink.setOnClickListener(new View.OnClickListener() {     //link clicked
            @Override
            public void onClick(View v) {
                Dialog dialog = getDialog();
                dialog.dismiss();
                final Context context = getActivity();
                Intent intent = MainActivity.OpenLink("https://vaughnlive.tv/" + casterName.getText());
                if (intent != null) {
                    context.startActivity(MainActivity.OpenLink("https://vaughnlive.tv/" + casterName.getText()));
                } else {
                    AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(context);
                    dlgBuilder.setMessage(R.string.no_puffin_warning);
                    dlgBuilder.setPositiveButton("Install", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=com.cloudmosa.puffinFree"));
                            context.startActivity(intent);
                        }
                    });
                    dlgBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dlgBuilder.setCancelable(true);
                    dlgBuilder.show();
                }
            }
        });

        //delete button (delete from db and unsubscribe from topic)
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.db.delete("casters", "casterName = ?", new String[] {casterName.getText().toString()});
                adapter.changeCursor(MainActivity.db.rawQuery("SELECT * FROM casters" + MainActivity.sortOrder, null));
                FirebaseMessaging.getInstance().unsubscribeFromTopic(casterName.getText() + MainActivity.topicSuffix);
                getDialog().dismiss();
                MainActivity.ToastCenteredText(getActivity(), "Successfully deleted " + casterName.getText() + " from list", Toast.LENGTH_LONG);
                if (adapter.getCount() == 0) {
                    //Restart MainActivity if we now have an empty list
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    getActivity().finish();
                    startActivity(intent);
                }
            }
        });

        //fetch logo from bundle
        byte[] logoBitmap = getArguments().getByteArray("logoBitmap");
        if (logoBitmap == null) {
            casterLogo.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.nophoto));
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(logoBitmap, 0, logoBitmap.length);
            casterLogo.setImageBitmap(bitmap);
        }
        return infoDialog;
    }
}
