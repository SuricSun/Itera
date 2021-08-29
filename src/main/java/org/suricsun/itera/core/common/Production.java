package org.suricsun.itera.core.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: SuricSun
 * @date: 2021/8/6
 */

/**
 * 代表一个语法规则
 */
public class Production implements Serializable {

    /**
     * Production Head
     */
    Token head;
    /**
     * Production Body
     */
    List<Token> body = new ArrayList<>();

    /**
     * 初始化一个body，但里面没东西,head为null，这两个是不一样的
     */
    public Production() {

    }

    /**
     * 浅复制
     *
     * @param head
     * @param body
     */
    public Production(Token head, List<Token> body) {

        this.head = head;
        this.body = body;
    }

    @Override
    public boolean equals(Object in) {

        if (this == in) {

            return true;

        } else {

            if (in instanceof Production) {

                Production typedIn = (Production) in;
                return this.head.equals(typedIn.head) && this.body.equals(typedIn.body);
            }
        }

        return false;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(this.head.getName()).append("=");

        for (Token token : this.body) {

            sb.append(token.getName()).append(" ");
        }

        return sb.toString();
    }

    public Token getHead() {
        return head;
    }

    public void setHead(Token head) {
        this.head = head;
    }

    public List<Token> getBody() {
        return body;
    }

    public void setBody(List<Token> body) {
        this.body = body;
    }
}
