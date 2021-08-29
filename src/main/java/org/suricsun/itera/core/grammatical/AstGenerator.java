package org.suricsun.itera.core.grammatical;

import org.suricsun.itera.core.common.Production;
import org.suricsun.itera.core.common.Token;
import org.suricsun.util.dsa.StackS;

import java.util.List;

/**
 * @author: SuricSun
 * @date: 2021/8/28
 */
public class AstGenerator {

    public AstGenerator() {

    }

    public static AstTreeNode GenerateTree(List<Token> parsedTokens, List<LALRAction> actions) {

        int curPosAtStack_Tmp = 0;
        int curPosAtParsedTokens = 0;
        AstTreeNode newlyGeneratedTreeNode;
        AstTreeNode curScanNode;
        int prodBodyLength = -1;

        Production curProd;
        List<Token> curProdBody;

        StackS<AstTreeNode> stack = new StackS<>(AstTreeNode.class, 100);

        //开始循环
        for (LALRAction action : actions) {

            if (action.action == LALRAction.ActionType.ShiftToState) {

                //把parsedToken里面的下一个Shift到Stack
                newlyGeneratedTreeNode = new AstTreeNode();
                newlyGeneratedTreeNode.setToken(parsedTokens.get(curPosAtParsedTokens));

                stack.push(newlyGeneratedTreeNode);

                curPosAtParsedTokens++;

            } else if (action.action == LALRAction.ActionType.ReduceWithProd) {

                //按照长度和shouldReserve标志对Stack里面的node进行合并生成
                curProd = action.reduceWithProd;
                curProdBody = curProd.getBody();
                prodBodyLength = curProd.getBody().size();
                curPosAtStack_Tmp = stack.size() - prodBodyLength;

                newlyGeneratedTreeNode = new AstTreeNode();
                newlyGeneratedTreeNode.setToken(curProd.getHead());

                for (int i = 0; i < prodBodyLength; i++) {

                    //如果当前位置的node是shouldReserve就加入
                    //加入的时候检查children数量，为1的话就把children直接加入
                    curScanNode = stack.get(curPosAtStack_Tmp + i);

                    if (curProdBody.get(i).isShouldReserveInAstTree()) {

                        //如果curScanNode的子node数量为0或者>1的话就正常加入，否则加入curScanNode的子node（因为就一个）
                        if (curScanNode.getChildNodes().size() == 1) {

                            newlyGeneratedTreeNode.getChildNodes().add(curScanNode.getChildNodes().get(0));

                        } else {

                            newlyGeneratedTreeNode.getChildNodes().add(curScanNode);
                        }
                    }
                }

                //使前面的失效
                stack.pop(prodBodyLength);
                //最后加入新的node,旧的node已经成为childNode了
                stack.push(newlyGeneratedTreeNode);

            } else {

                //那就是Acc了，到头了
                //什么都不做就好了
            }
        }

        return stack.get(0);
    }
}
