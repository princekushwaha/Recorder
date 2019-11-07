package ravishcom.info.recorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.ToolbarWidgetWrapper;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class RecordingListView extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView textView;
    private androidx.appcompat.widget.Toolbar toolbar;
    private ArrayList<Recording> recording_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list_view);
        initView();
        fetchRecordings();
    }


    void initView(){
        recyclerView=(RecyclerView)findViewById(R.id.recording_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration decoration=new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        textView=(TextView)findViewById(R.id.no_recording);
        toolbar=(Toolbar) findViewById(R.id.recording_toolbar);
        recording_list=new ArrayList<Recording>();
        toolbar.setTitle("Saved Recordings");
        toolbar.setTitleTextColor(getResources().getColor(R.color.black));
        toolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(toolbar);

    }
    void fetchRecordings(){

        File root=android.os.Environment.getExternalStorageDirectory();
        File directory=new File(root.getAbsolutePath(),"AudioRecorder");
        if(!directory.exists()){
            directory.mkdir();
        }
        File files[]=directory.listFiles();

        if(files.length!=0){
            for(int i=0;i<files.length;i++)
            {
                if((files[i].getName()).equals("REC.mp3"))
                {
                    if(files.length==1)
                        textView.setVisibility(View.VISIBLE);
                    continue;
                }
                Recording recording=new Recording(root.getAbsolutePath()+"/AudioRecorder/"+files[i].getName(), files[i].getName(),false);
               recording_list.add(recording);
               setAdapter();
            }
        }else{
            textView.setVisibility(View.VISIBLE);
        }
    }
    private void setAdapter()
    {
      recyclerView.setAdapter(new RecordingRecyclerView(this,recording_list));
    }
}
