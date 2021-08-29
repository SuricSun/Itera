package org.suricsun.itera.core.grammatical;

import org.suricsun.itera.core.common.GrammaticalException;
import org.suricsun.itera.core.common.Token;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * @author: SuricSun
 * @date: 2021/8/11
 */
public class LALRParser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * actionMap旨在根据当前所在State和输入，得到一个action<br>
     * Action action = actionMap[state].get(inputTokenName);
     */
    private List<HashMap<String, LALRAction>> actionMapList;

    public LALRParser() {

    }

    public void eat(LALRParsingTable lalrParsingTable) {

        this.actionMapList = lalrParsingTable.getActionMapList();
    }

    public List<LALRAction> parse(List<Token> code) throws GrammaticalException {

        List<Integer> stateStack = new ArrayList<>(100);
        List<LALRAction> actionStack = new ArrayList<>(100);

        stateStack.add(0);

        LALRAction action;
        Iterator<Token> codeIterator = code.iterator();
        Token curParseToken = codeIterator.next();

        while (true) {

            //查表
            action = this.actionMapList
                    .get(stateStack.get(stateStack.size() - 1))
                    .get(curParseToken.getName());

            if (action != null) {

                if (action.action == LALRAction.ActionType.ShiftToState) {

                    stateStack.add(action.shiftToState);
                    actionStack.add(action);
                    curParseToken = codeIterator.next();

                } else if (action.action == LALRAction.ActionType.ReduceWithProd) {

                    actionStack.add(action);
                    //回退stateStack栈
                    for (int i = 0; i < action.reduceWithProd.getBody().size(); i++) {

                        stateStack.remove(stateStack.size() - 1);
                    }
                    stateStack.add(
                            this.actionMapList
                                    .get(stateStack.get(stateStack.size() - 1))
                                    .get(action.reduceWithProd.getHead().getName())
                                    .shiftToState);

                } else if (action.action == LALRAction.ActionType.Accept) {

                    actionStack.add(action);
                    return actionStack;
                }

            } else {

                throw new GrammaticalException("Error parsing");
            }
        }
    }
}
