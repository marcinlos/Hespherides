package hesp.gui;

import javax.swing.DefaultListModel;

public class BoundedListModel<T> extends DefaultListModel<T> {
    
    private int maxSize;
    
    public BoundedListModel(int maxSize) {
        this.maxSize = maxSize;
    }
    
    public BoundedListModel() {
        this(Integer.MAX_VALUE);
    }
    
    @Override
    public void addElement(T item) {
        super.addElement(item);
        if (size() > maxSize) {
            remove(0);
        }
    }

}
