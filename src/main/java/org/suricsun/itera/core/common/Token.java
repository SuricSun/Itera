package org.suricsun.itera.core.common;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * @author: SuricSun
 * @date: 2021/8/8
 */
public class Token implements Serializable, Comparable<String> {

    /**
     * 终止符与非终止符
     */
    public enum TokenType {

        Unknown,
        Terminal,
        Non_Terminal,
        EndMarker
    }

    /**
     * 在配置文件中的名字
     */
    private String name;
    /**
     * Token类型，终止符or非终止符
     */
    private TokenType type;
    /**
     * 这个被这个token解析的片段
     */
    private String detailedStr;
    /**
     * 是否在AST Tree中保留此node，仅在Production Body中有意义
     */
    private boolean shouldReserveInAstTree = false;

    public Token() {

    }

    public Token(String name, TokenType type) {

        this.name = name;
        this.type = type;
    }

    public Token(RegexToken notFastToken) {

        this.name = notFastToken.getName();
        this.type = notFastToken.getType();
    }

    @Override
    public int compareTo(@NotNull String str) {

        return this.name.compareTo(str);
    }

    @Override
    public boolean equals(Object in) {

        if (this == in) {

            return true;

        } else {

            if (in instanceof Token) {

                Token typedIn = (Token) in;
                return this.name.equals(typedIn.name);
            }
        }

        return false;
    }

    @Override
    public String toString() {

        return this.name + " : " + this.detailedStr;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TokenType getType() {
        return this.type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public String getDetailedStr() {
        return this.detailedStr;
    }

    public void setDetailedStr(String detailedStr) {
        this.detailedStr = detailedStr;
    }

    /**
     * 是否在AST Tree中保留此node，仅在Production Body中有意义
     */
    public boolean isShouldReserveInAstTree() {
        return shouldReserveInAstTree;
    }

    /**
     * 是否在AST Tree中保留此node，仅在Production Body中有意义
     */
    public void setShouldReserveInAstTree(boolean shouldReserveInAstTree) {
        this.shouldReserveInAstTree = shouldReserveInAstTree;
    }
}
