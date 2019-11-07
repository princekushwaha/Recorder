package ravishcom.info.recorder;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {



   private ImageButton play_record_pause,save;
   private Chronometer chronometer;
   private Toolbar toolbar;
   private TextView recording;
   private ImageView img;
   private int buttonIdentifier;
    private View view;
   private MediaRecorder mediaRecorder;
   private long lastStopped=0;
   private AlertDialog save_by_name;
    private int RECORD_AUDIO_REQUEST_CODE=100;
    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        audio_recording_permissions();
        initViews();

        play_record_pause.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                switch(buttonIdentifier)
                {
                    case 0:{
                        mediaRecorder=new MediaRecorder();;
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

                        File root=android.os.Environment.getExternalStorageDirectory();
                        File file=new File(root.getAbsolutePath()+"/AudioRecorder/");
                        if(!file.exists())
                            file.mkdir();
                        mediaRecorder.setOutputFile(root.getAbsolutePath()+"/AudioRecorder/"+"REC.mp3");
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        Log.d("time",String.valueOf(System.currentTimeMillis()));
                        try {
                            mediaRecorder.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mediaRecorder.start();
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        Log.i("time",String.valueOf(SystemClock.elapsedRealtime()));
                        chronometer.setVisibility(View.VISIBLE);
                        save.setVisibility(View.VISIBLE);
                        recording.setText("Recording...");
                        recording.setVisibility(View.VISIBLE);
                        img.setVisibility(View.VISIBLE);
                        chronometer.start();
                        play_record_pause.setImageResource(R.drawable.pause_button);
                        buttonIdentifier=1;
                        break;
                    }
                    case 1:{
                        mediaRecorder.pause();
                        Log.i("base",String.valueOf(chronometer.getBase()));
                        lastStopped=chronometer.getBase()-SystemClock.elapsedRealtime();
                        chronometer.stop();
                        play_record_pause.setImageResource(R.drawable.play_button);
                        recording.setText("Recoding Paused");
                        buttonIdentifier=2;
                        break;
                    }
                    case 2:{
                        play_record_pause.setImageResource(R.drawable.pause_button);
                        mediaRecorder.resume();
                        chronometer.setBase(lastStopped+SystemClock.elapsedRealtime());
                        chronometer.start();
                        recording.setText("Recording...");
                        buttonIdentifier=1;
                        break;
                    }

                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                mediaRecorder.pause();
                chronometer.stop();
                save_by_name.show();
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    void audio_recording_permissions()
    {
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED)||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)||
        (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED))
        {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.
                            WRITE_EXTERNAL_STORAGE}, RECORD_AUDIO_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==RECORD_AUDIO_REQUEST_CODE)
        {
            if(grantResults.length==3&&grantResults[0]==PackageManager.PERMISSION_GRANTED&&
                    grantResults[1]==PackageManager.PERMISSION_GRANTED &&grantResults[2]==PackageManager.PERMISSION_GRANTED)
            {

            }else{
                Toast.makeText(this,"You must give permissions to use this app. App is exiting.",Toast.LENGTH_LONG).show();
                finishAffinity();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void initViews()
    {
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        view=getLayoutInflater().inflate(R.layout.save_dialog,null);
        builder.setView(view);
        builder.setCustomTitle(getLayoutInflater().inflate(R.layout.save_recording_title,null));
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText e=(EditText)view.findViewById(R.id.save_by_name);
                String file_name=e.getText().toString()+".mp3";
                Log.d("filename",file_name);
                try{
                    mediaRecorder.stop();
                }catch (IllegalStateException ex)
                {
                    ex.printStackTrace();
                }
                mediaRecorder.release();

                mediaRecorder=null;
                File directory=new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath(), "AudioRecorder");
                File from=new File(directory,"REC.mp3");
                File to=new File(directory,file_name);
                from.renameTo(to);
                play_record_pause.setImageResource(R.drawable.record_button);
                save.setVisibility(View.GONE);
                recording.setVisibility(View.GONE);
                img.setVisibility(View.GONE);
                buttonIdentifier=0;
                chronometer.stop();
                chronometer.setVisibility(View.GONE);

            }
        });
        builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mediaRecorder.release();
                File audio_file=new File(android.os.Environment.getExternalStorageDirectory()+"/AudioRecorder/REC.mp3");
                audio_file.delete();
                play_record_pause.setImageResource(R.drawable.record_button);
                save.setVisibility(View.GONE);
                recording.setVisibility(View.GONE);
                img.setVisibility(View.GONE);
                buttonIdentifier=0;
                chronometer.stop();
                chronometer.setVisibility(View.GONE);
                dialogInterface.cancel();
            }
        });

       builder.setCancelable(false);
        EditText e=(EditText)view.findViewById(R.id.save_by_name);
        save_by_name=builder.create();
        save_by_name.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                EditText e=(EditText)view.findViewById(R.id.save_by_name);
                e.setText("REC"+String.valueOf((android.os.SystemClock.currentThreadTimeMillis())));
            }
        });

        e.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            Button button=save_by_name.getButton(AlertDialog.BUTTON_POSITIVE);
            if(editable.length()==0)
                button.setEnabled(false);
            else
                button.setEnabled(true);

                EditText e=(EditText)view.findViewById(R.id.save_by_name);
                String text=e.getText().toString()+".mp3";
            File file=new File(android.os.Environment.getExternalStorageDirectory()+"/AudioRecorder/"+text);
            if(!text.equals("REC.mp3")) {
                if ((file.exists())) {
                    button.setEnabled(false);
                    e.setError("File Already Exists");
                } else {
                    e.setError(null);
                }
            }


            }
        });



        buttonIdentifier=0;
        recording=(TextView)findViewById(R.id.recording);
        img=(ImageView)findViewById(R.id.recording_image);
       play_record_pause=(ImageButton)findViewById(R.id.play_pause_recored);
       save=(ImageButton)findViewById(R.id.save);
       chronometer=(Chronometer)findViewById(R.id.chronometer);
       toolbar=(androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
       toolbar.setTitle("Sound Recorder");
       toolbar.setTitleTextColor(getResources().getColor(R.color.black));
       setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i=new Intent(this,RecordingListView.class);
        startActivity(i);
        return true;
    }
}
