package org.suricsun.itera.core.grammatical;

import org.suricsun.itera.core.common.Production;

import java.io.Serializable;

public class LALRAction implements Serializable {

    public enum ActionType {

        ShiftToState,
        ReduceWithProd,
        Accept
    }

    public ActionType action;
    /**
     * 根据action的值有意义
     */
    public int shiftToState;
    /**
     * 根据action的值有意义
     */
    public Production reduceWithProd;
}
