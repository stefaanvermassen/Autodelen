package models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Cedric on 4/11/2014.
 */
public class FileGroup implements Iterable<File> {

    private List<File> files;

    public FileGroup(List<File> files){
        this.files = files;
    }

    public FileGroup(){
        this.files = new ArrayList<>();
    }

    @Override
    public Iterator<File> iterator() {
        return files.iterator();
    }

    public void addFile(File file){
        files.add(file);
    }

    public boolean removeFile(File file){
        return files.remove(file);
    }

    public List<File> toList(){
        return new ArrayList<>(files); //deep copy
    }
}