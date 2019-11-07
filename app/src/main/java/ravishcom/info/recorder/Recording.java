package ravishcom.info.recorder;

public class Recording {
    private String uri,name;
    private boolean isSelected;

    public Recording(String uri,String name,boolean isPlaying)
    {
        this.uri=uri;
        this.name=name;
        isSelected=false;
    }
    public String getUri(){
        return  uri;
    }
    public String getName(){
        return name;
    }
    public  boolean isSelected(){
        return isSelected;
    }
    public  void setSelected(boolean isSelected){
        this.isSelected=isSelected;
    }
    public  void setUri(String u){uri=u;}
    public void setName(String n){name=n;}
}
