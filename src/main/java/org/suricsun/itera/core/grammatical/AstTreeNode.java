package org.suricsun.itera.core.grammatical;

import org.suricsun.itera.core.common.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * 非常轻量级
 *
 * @author: SuricSun
 * @date: 2021/8/28
 */
public class AstTreeNode {

    /**
     * 这个node代表的token
     */
    private Token token;
    /**
     * 这个node的子node
     */
    private List<AstTreeNode> childNodes = new ArrayList<>();

    public AstTreeNode() {

    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public List<AstTreeNode> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<AstTreeNode> childNodes) {
        this.childNodes = childNodes;
    }
}
