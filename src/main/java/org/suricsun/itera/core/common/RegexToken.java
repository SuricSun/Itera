package org.suricsun.itera.core.common;

/**
 * @author: SuricSun
 * @date: 2021/8/6
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 有终止符和非终止符之分
 * 只有终止符才能参加Lexer分析
 */
public class RegexToken extends Token {

    /**
     * 编译的regex字符，只有type为终止符时才有意义
     */
    Pattern pattern;

    transient Matcher matcher;

    public RegexToken() {

    }

    public RegexToken(String name, TokenType type) {

        super(name, type);
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) { this.pattern = pattern; }

    public Matcher getMatcher() {
        return matcher;
    }

    public void setMatcher(Matcher matcher) { this.matcher = matcher; }
}
