package hesp.gui;

import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * @author marcinlos
 *
 * Model for two spinners representing interval.
 */
class RangeSpinnerModel {
    
    private SpinnerModel lowerModel;
    private SpinnerModel upperModel;

    /**
     * Creates a model representing range contained in {@code [min, max]}.
     * 
     * @param min Upper bound for maximal element
     * @param max Lower bound for minimal element
     * @param initialLowerValue initial value for lower bound of 
     * represented range
     * @param initialUpperValue initiial value for upper bound of
     * represented range
     */
    public RangeSpinnerModel(int min, int max, int initialLowerValue, 
            int initialUpperValue) {
        
        lowerModel = new SpinnerNumberModel(initialLowerValue, min, max, 1) {

            @Override
            public void setValue(Object value) {
                int newValue;
                try {
                    newValue = (Integer) value;
                    if (newValue <= (Integer) upperModel.getValue()) {
                        super.setValue(newValue);
                    } else {
                        throw new IllegalArgumentException("Invalid " +
                                "value: got " + newValue + 
                                " value in [" + getMinimum() + ", " + 
                                upperModel.getValue() + "]");
                    }
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Got " + 
                            value.getClass().getCanonicalName() +
                            " instead of integer");
                }
            }
            
            @Override
            public Object getNextValue() {
                Integer newValue;
                if ((newValue = (Integer) super.getNextValue()) != null) {
                    int upperValue = (int) upperModel.getValue();
                    return newValue <= upperValue ? newValue : null;
                } else {
                    return null;
                }
            }
        };

        upperModel = new SpinnerNumberModel(initialUpperValue, min, max, 1) {

            @Override
            public void setValue(Object value) {
                int newValue;
                // According to SpinnerModel doc this should throw
                // IllegalArgumentException for values outside the range
                try {
                    newValue = (Integer) value;
                    if (newValue >= (Integer) lowerModel.getValue()) {
                        super.setValue(newValue);
                    } else {
                        throw new IllegalArgumentException("Invalid " +
                                "value: got " + newValue + 
                                " value in [" + lowerModel.getValue() + ", " + 
                                getMaximum() + "]");
                    }
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Got " + 
                            value.getClass().getCanonicalName() +
                            " instead of integer");
                }
            }
            
            @Override
            public Object getPreviousValue() {
                Integer newValue;
                if ((newValue = (Integer) super.getPreviousValue()) != null) {
                    int lowerValue = (int) lowerModel.getValue();
                    return newValue >= lowerValue ? newValue : null;
                } else {
                    return null;
                }
            }
            
        };
    }
    
    /**
     * @return model for spinner corresponding to lower range bound
     */
    public SpinnerModel getLowerModel() {
        return lowerModel;
    }
    
    /**
     * @return model for spinner corresponding to upper range bound
     */
    public SpinnerModel getUpperModel() {
        return upperModel;
    }
    
    /**
     * @return lower bound of represented range
     */
    public int getLowerValue() {
        return (int) lowerModel.getValue();
    }
    
    /**
     * @return upper bound of represented range
     */
    public int getUpperValue() {
        return (int) upperModel.getValue();
    }
    
}
