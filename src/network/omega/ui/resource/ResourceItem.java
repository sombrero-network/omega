package network.omega.ui.resource;

import javafx.beans.property.*;
import javafx.scene.control.Button;

import java.util.ArrayList;

public class ResourceItem implements Comparable<ResourceItem> {
    public ResourceDescription rd;
    
    private ReadOnlyBooleanWrapper caution;
    public StringProperty name;
    public StringProperty os;
    public StringProperty cores;
    public StringProperty ram;
    public StringProperty disk;
    public StringProperty vm;
    
    public DoubleProperty doubleVal;
    
    public ResourceItem() {
        
    }
    
    public ResourceItem(String name, String os, String cores, String ram, String disk, String vm,
            ResourceDescription rd) {
        this.name = new SimpleStringProperty(name);
        this.os = new SimpleStringProperty(os);
        this.cores = new SimpleStringProperty(cores);
        this.ram = new SimpleStringProperty(ram);
        this.disk = new SimpleStringProperty(disk);
        this.vm = new SimpleStringProperty(vm);
        this.rd = rd;
    }
    
    public ResourceItem(String name, Double doubleVal) {
        this.name = new SimpleStringProperty(name);
        this.doubleVal = new SimpleDoubleProperty(doubleVal);
        
        // trigger css style on table cell level
        this.caution = new ReadOnlyBooleanWrapper();
        this.caution.bind(this.doubleVal.lessThan(1.0d));
        
    }
    
    public String getName() {
        return name.get();
    }
    
    public StringProperty nameProperty() {
        return name;
    }
    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public double getDoubleVal() {
        return doubleVal.get();
    }
    
    public void setDoubleVal(double doubleVal) {
        this.doubleVal.set(doubleVal);
    }
    
    public final ReadOnlyBooleanProperty cautionProperty() {
        return this.caution.getReadOnlyProperty();
    }
    
    public final boolean isCaution() {
        return this.cautionProperty().get();
    }
    
    @Override
    public int compareTo(ResourceItem o) {
        if (this.getDoubleVal() == o.getDoubleVal()) {
            return 0;
        } else if (this.getDoubleVal() > o.getDoubleVal()) {
            return 1;
        } else {
            return -1;
        }
    }
    
    @Override
    public String toString() {
        return name.toString();
    }
    
    public void updateResourceItem(ResourceItem newData) {
        this.setDoubleVal(newData.getDoubleVal());
    }
    
}