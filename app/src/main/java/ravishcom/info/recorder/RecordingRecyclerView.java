package ravishcom.info.recorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class RecordingRecyclerView extends RecyclerView.Adapter<RecordingRecyclerView.ViewHolder> {
    private ArrayList<Recording> recordings;
    private Context context;
    private  boolean selectableScreen;
    private ActionMode actionMode;
    private ActionMode.Callback actionModeCallback;
    private int num_selected=0;
    public RecordingRecyclerView(final Context context, final ArrayList<Recording> recordings){

        this.context=context;
        this.recordings=recordings;
        actionMode=null;
        selectableScreen=false;
        actionModeCallback=new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.contextual_menu,menu);
                (menu.getItem(2)).setTitle("Select All");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
                Log.d("mode","c");
                Recording recording;
                File audio;
                switch (menuItem.getItemId())
                {
                   case R.id.delete:{
                       AlertDialog.Builder caution=new AlertDialog.Builder(context);
                       caution.setTitle("Delete "+num_selected+" recording?");
                       caution.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int j) {
                               File audio;
                               for(int i=0;i<recordings.size();i++)
                               {
                                   if(recordings.get(i).isSelected())
                                   {
                                       audio=new File(recordings.get(i).getUri());
                                       audio.delete();
                                       recordings.remove(i);
                                       i--;
                                   }
                               }
                               if(recordings.size()==0)
                               {

                                   TextView t= (TextView)(((Activity)context).findViewById(R.id.no_recording));
                                   t.setVisibility(View.VISIBLE);
                               }
                               num_selected=0;
                               actionMode.finish();
                           }
                       });
                       caution.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialogInterface, int i) {
                               dialogInterface.cancel();
                           }
                       });
                       AlertDialog dialog=caution.create();
                       dialog.show();
                       break;
                   }
                    case R.id.select_all:{

                        MenuItem share=actionMode.getMenu().getItem(0);
                        MenuItem delete=actionMode.getMenu().getItem(1);
                        MenuItem rename=actionMode.getMenu().getItem(3);
                        rename.setEnabled(false);
                        if(menuItem.getTitle().toString()=="Select All"){
                            share.setEnabled(true);
                            delete.setEnabled(true);
                            share.setIcon(R.drawable.share_item);
                            delete.setIcon(R.drawable.delete_item);
                            menuItem.setTitle("Deselect All");
                            num_selected=recordings.size();
                            actionMode.setTitle(String.valueOf(num_selected)+" File Selected");
                        for(int i=0;i<recordings.size();i++)
                        {
                           recordings.get(i).setSelected(true);
                        }
                        notifyDataSetChanged();
                        }else if(menuItem.getTitle().toString()=="Deselect All"){
                            share.setEnabled(false);
                            delete.setEnabled(false);
                            share.setIcon(R.drawable.disabled_share);
                            delete.setIcon(R.drawable.disabled_delete);
                            menuItem.setTitle("Select All");
                            num_selected=0;
                            actionMode.setTitle(String.valueOf(num_selected)+" File Selected");
                            for(int i=0;i<recordings.size();i++)
                            {
                             recordings.get(i).setSelected(false);
                            }

                        }
                        notifyDataSetChanged();
                        break;
                    }
                    case R.id.share:{
                        ArrayList<Uri> uri=new ArrayList<>();
                        int size=recordings.size();
                        for(int i=0;i<size;i++)
                        {
                            if(recordings.get(i).isSelected())
                            uri.add(Uri.parse(recordings.get(i).getUri()));
                        }
                        if(uri.size()!=0) {
                            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                            intent.setType("audio/*");
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uri);
                            ((Activity) context).startActivity(intent);
                            num_selected = 0;
                            actionMode.finish();
                        }
                        return true;
                    }
                    case R.id.rename:{


                        final AlertDialog.Builder builder=new AlertDialog.Builder(context);
                        View title=LayoutInflater.from(context).inflate(R.layout.save_recording_title,null);
                        ((TextView)title.findViewById(R.id.title)).setText("Rename To");
                        builder.setCustomTitle(title);
                        final View view=LayoutInflater.from(context).inflate(R.layout.save_dialog,null);
                        final EditText editText=(EditText)view.findViewById(R.id.save_by_name);
                        builder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int j) {
                                String file_uri=new String();
                                for(int i=0;i<recordings.size();i++){
                                    if((recordings.get(i)).isSelected()){
                                        file_uri=(recordings.get(i)).getUri();
                                        recordings.get(i).setName(editText.getText().toString()+".mp3");
                                        recordings.get(i).setUri(android.os.Environment.getExternalStorageDirectory()+
                                                "/AudioRecorder/"+editText.getText().toString()+".mp3");
                                        break;
                                    }
                                }
                                File file=new File(file_uri);
                                File renameto=new File(android.os.Environment.getExternalStorageDirectory()+
                                        "/AudioRecorder/"+editText.getText().toString()+".mp3");
                                file.renameTo(renameto);
                                num_selected=0;
                                actionMode.setTitle(String.valueOf(num_selected)+" File Selected");
                                for(int i=0;i<recordings.size();i++)
                                {
                                    recordings.get(i).setSelected(false);
                                }
                                notifyDataSetChanged();
                                actionMode.finish();
                            }
                        });
                        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                        builder.setView(view);
                        final AlertDialog dialog=builder.create();

                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                Button button=dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                                EditText text=(EditText)view.findViewById(R.id.save_by_name);
                                if(editable.length()==0)
                                    button.setEnabled(false);
                                else
                                    button.setEnabled(true);
                                File file=new File(android.os.Environment.getExternalStorageDirectory()+"/AudioRecorder/"+
                                        text.getText().toString()+".mp3");
                                if(file.exists())
                                {
                                    text.setError("File already exists");
                                    button.setEnabled(false);
                                }else{
                                    text.setError(null);
                                    button.setEnabled(true);
                                }


                            }
                        });

                        dialog.show();
                        (dialog.getButton(AlertDialog.BUTTON_POSITIVE)).setEnabled(false);
                    }

                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                Log.d("mode","d");
                selectableScreen=false;
                for(int i=0;i<recordings.size();i++)
                {
                    recordings.get(i).setSelected(false);
                }
                num_selected=0;
                notifyDataSetChanged();
             actionMode=null;
            }
        };
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d("name","here");
        View view= LayoutInflater.from(context).inflate(R.layout.layout_view_holder,parent,false);
      return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Recording recording=recordings.get(position);
        holder.file_name.setText(recording.getName());
        holder.file_name.setCheckMarkDrawable(null);
        File file=new File(android.os.Environment.getExternalStorageDirectory()+"/AudioRecorder/"+recording.getName());
        Date date=new Date(file.lastModified());
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);
        String d= String.valueOf(calendar.get(Calendar.DATE));
        String m= String.valueOf(calendar.get(Calendar.MONTH));
        String y= String.valueOf(calendar.get(Calendar.YEAR));
        holder.file_details.setText(d+"/"+m+"/"+y+"  "+String.valueOf(file.length()/1000)+"KB");

     if(selectableScreen)
     {
         if(recording.isSelected())
         {
             holder.file_name.setChecked(true);
             holder.file_name.setCheckMarkDrawable(R.drawable.box_checked);
         }else{
             holder.file_name.setChecked(false);
             holder.file_name.setCheckMarkDrawable(R.drawable.box_unchecked);
         }
     }else{

         holder.file_name.setChecked(false);
     }


    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        CheckedTextView file_name;
        TextView file_details;
        RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            file_name = (CheckedTextView) itemView.findViewById(R.id.file_name);
            file_details=(TextView)itemView.findViewById(R.id.file_date);
            relativeLayout=(RelativeLayout)itemView.findViewById(R.id.relative_layout);
           relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(selectableScreen==false) {
                    CustomMediaPlayer mediaPlayer = new CustomMediaPlayer(context);
                   mediaPlayer.play(recordings.get(getAdapterPosition()));

                }else {
                    Recording recording=recordings.get(getAdapterPosition());
                    MenuItem share=actionMode.getMenu().getItem(0);
                    MenuItem delete=actionMode.getMenu().getItem(1);
                    MenuItem select=actionMode.getMenu().getItem(2);
                    MenuItem rename=actionMode.getMenu().getItem(3);
                    rename.setEnabled(false);
                    Log.d("title",share.getTitle().toString());
                    if(file_name.isChecked())
                    {
                        num_selected--;
                        if(num_selected==0)
                        {
                            share.setEnabled(false);
                            delete.setEnabled(false);
                            share.setIcon(R.drawable.disabled_share);
                            delete.setIcon(R.drawable.disabled_delete);
                            select.setTitle("Select All");
                        }
                        if(num_selected==1)
                            rename.setEnabled(true);
                        actionMode.setTitle(String.valueOf(num_selected)+" File Selected");
                        recording.setSelected(false);
                        file_name.setChecked(false);
                        file_name.setCheckMarkDrawable(R.drawable.box_unchecked);
                    }else{
                        if(num_selected==0)
                        {
                            share.setEnabled(true);
                            delete.setEnabled(true);
                            share.setIcon(R.drawable.share_item);
                            delete.setIcon(R.drawable.delete_item);
                        }

                        num_selected++;
                        if(num_selected==1)
                            rename.setEnabled(true);

                        if(num_selected==recordings.size())
                        {
                            select.setTitle("Deselect All");
                        }
                        actionMode.setTitle(String.valueOf(num_selected)+" File Selected");
                        recording.setSelected(true);
                        file_name.setChecked(true);
                        file_name.setCheckMarkDrawable(R.drawable.box_checked);
                    }
                }

            }
        });
        relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(actionMode==null) {
                    Recording recording = recordings.get(getAdapterPosition());
                    recording.setSelected(true);
                    actionMode=((Activity)context).startActionMode(actionModeCallback);
                    num_selected++;
                    MenuItem select=actionMode.getMenu().getItem(2);
                    if(num_selected==recordings.size())
                    {
                        select.setTitle("Deselect All");
                    }
                    actionMode.setTitle(String.valueOf(num_selected)+" File Selected");
                    selectableScreen = true;
                    notifyDataSetChanged();
                }
                return true;
            }
        });

        }

    }
}

class CustomMediaPlayer implements Runnable{
    private Context context;
    private View view;
    private Thread seekThread;
    private  AlertDialog play_dialog;
    private MediaPlayer mediaPlayer;
    private  SeekBar seekBar;
    private boolean isPlaying;
    private CustomMediaPlayer current;
    public CustomMediaPlayer(){}
    public CustomMediaPlayer(Context con)
    {
        current=this;
        isPlaying=false;
        context=con;
        mediaPlayer=new MediaPlayer();
        view=(LayoutInflater.from(context)).inflate(R.layout.play_dialog,null);
        seekBar=((SeekBar)view.findViewById(R.id.audio_seekbar));
        seekThread=new Thread(this);
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setView(view);
        play_dialog=builder.create();
        ((ImageButton)view.findViewById(R.id.audio_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying)
                {
                    mediaPlayer.pause();
                    ((ImageButton)view).setImageResource(R.drawable.small_play_button);
                    isPlaying=false;
                    try {
                        seekThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    mediaPlayer.start();
                    isPlaying=true;
                    ((ImageButton)view).setImageResource(R.drawable.small_pause_button);
                    seekThread=new Thread(current);
                    seekThread.start();

                }
            }
        });
       seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           @Override
           public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

           }

           @Override
           public void onStartTrackingTouch(SeekBar seekBar) {
               int pos=seekBar.getProgress();
               isPlaying=false;
               try {
                   seekThread.join();
               } catch (InterruptedException e){
                   e.printStackTrace();
               }
               seekBar.setProgress(pos);
               mediaPlayer.pause();
           }

           @Override
           public void onStopTrackingTouch(SeekBar seekBar) {
               mediaPlayer.seekTo(seekBar.getProgress());
               mediaPlayer.start();
               isPlaying=true;
               ((ImageButton)view.findViewById(R.id.audio_button)).setImageResource(R.drawable.small_pause_button);
               seekThread=new Thread(current);
               seekThread.start();
           }
       });
       play_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
           @Override
           public void onCancel(DialogInterface dialogInterface) {
               mediaPlayer.stop();
               mediaPlayer.release();
               isPlaying=false;

               try {
                   seekThread.join();
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
       });
       mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
           @Override
           public void onCompletion(MediaPlayer mediaPlayer) {
               play_dialog.cancel();
               isPlaying=false;
           }
       });
    }

    public void play(Recording recording)
    {
     String uri=recording.getUri();
     String name=recording.getName();
        ((TextView)view.findViewById(R.id.audio_name)).setText(name);
        try {
            mediaPlayer.setDataSource(uri);
            mediaPlayer.prepare();
            mediaPlayer.start();}
             catch (IOException e) {
            e.printStackTrace();
        }
        seekBar.setMax(mediaPlayer.getDuration());
        isPlaying=true;
        play_dialog.show();
        seekThread.start();

    }

    @Override
    public void run() {

        Log.d("break", String.valueOf(isPlaying));
        while (isPlaying)
        {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());

        }
        Log.d("break","B");

    }
}




















