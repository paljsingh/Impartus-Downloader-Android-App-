package jp.id.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.id.LectureAdapter;
import jp.id.R;
import jp.id.SettingsFragment;
import jp.id.core.Impartus;
import jp.id.core.Utils;
import jp.id.model.LectureItem;

public class VideoActivity extends AppCompatActivity {

    private List<LectureItem> lectureItems;
    private Impartus impartus;

    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int SDK_INT = Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        setContentView(R.layout.activity_video);

        if (savedInstanceState == null) {
            getAsyncLectures();
        }

        if (!hasStoragePermission()) {
            requestStoragePermission();
        }

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelableArrayList("lectureitems",
                (ArrayList<? extends Parcelable>) lectureItems);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        lectureItems = savedInstanceState.getParcelableArrayList("lectureitems");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (! (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "The app was not allowed to write to your storage." +
                        " Hence, it cannot function properly. Please consider granting it this permission",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean hasStoragePermission() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission() {
            this.requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
    }

    private void getAsyncLectures() {
        String baseUrl = Utils.getUrlFromPrefs(this);
        String sessionToken = Utils.getSessionTokenFromPrefs(this);

        impartus = new Impartus(baseUrl, this.getCacheDir(), sessionToken);

        PopulateLectures asyncTask = new PopulateLectures();
        asyncTask.execute(impartus);
    }

    protected void attachAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(VideoActivity.this));
        LectureAdapter lectureAdapter = new LectureAdapter(lectureItems, impartus);
        recyclerView.setAdapter(lectureAdapter);
    }

    public void onClickSettingsButton(View view) {
        SettingsFragment settingsFragment = new SettingsFragment();
        setContentView(R.layout.settings);

//        settingsFragment
        getSupportFragmentManager().beginTransaction().replace(R.id.settings, settingsFragment).commit();
    }

    private class PopulateLectures extends AsyncTask<Impartus, Void, Void> {
        private final ProgressDialog progressbar = new ProgressDialog(VideoActivity.this);
        private List<LectureItem> lectureItems;

        private Impartus impartus;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressbar.setMessage("Please wait...");
            progressbar.setIndeterminate(true);
            progressbar.setCancelable(false);
            progressbar.setInverseBackgroundForced(true);
            progressbar.show();
        }

        @Override
        protected Void doInBackground(Impartus... imps) {
            impartus = imps[0];
            lectureItems = impartus.getLectures();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            VideoActivity.this.lectureItems = lectureItems;
            progressbar.hide();
            progressbar.dismiss();

            attachAdapter();
        }
    }

}