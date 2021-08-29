package org.suricsun.itera.core.lexical;

import org.suricsun.itera.core.common.LanguageRulesFile;
import org.suricsun.itera.core.common.Token;
import org.suricsun.itera.core.common.LexicalException;
import org.suricsun.itera.core.common.RegexToken;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: SuricSun
 * @date: 2021/8/6
 */
public class Lexer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所有的从文件读取的非终止符<br>
     * 保存有顺序要求所以不用map用list
     */
    private List<RegexToken> allTerminals;

    /**
     * 为了快速求交集（与allTerminals）的副产品
     */
    transient private HashSet<String> allTerminalsHashMap;

    public Lexer() {

    }

    /**
     * 等于调用this.eat(tokenDef)
     *
     * @param tokenDef
     */
    public Lexer(String tokenDef) throws LexicalException {

        this.eat(tokenDef);
    }

    /**
     * 输入一个字符串，然后来配置更新这个Lexer。形式是每个token的定义用\n分开,比如<br>
     * id=[a-z] \n add=+ \n ...<br>
     *
     * @param tokenDef
     */
    public void eat(String tokenDef) throws LexicalException {

        String[] splitStr = tokenDef.split("\n", 0);

        //抛弃之前的allTerminals（如果有的话）
        this.allTerminals = new ArrayList<>();
        this.allTerminalsHashMap = new HashSet<>();
        //set不保存重复数据，保证了token name的唯一性

        Matcher tokenNameFormatMatcher = LanguageRulesFile.TokenNameFormat.matcher("iloveyou:)");

        int splitPoint;
        int end;

        for (String s : splitStr) {

            RegexToken regexToken = new RegexToken();
            regexToken.setType(Token.TokenType.Terminal);
            //读token name
            //reset
            tokenNameFormatMatcher.reset(s);
            if (tokenNameFormatMatcher.region(0, s.length()).lookingAt() == false) {

                throw new LexicalException("Token def format error : Token Name -> " + s);
            }

            end = tokenNameFormatMatcher.end();

            regexToken.setName(s.substring(0, end));

            //读取等号
            splitPoint = s.indexOf('=');

            if (splitPoint == -1) {

                throw new LexicalException("Token def format error : no '=' -> " + s);
            }

            if (splitPoint != end) {

                throw new LexicalException("Token def format error : Token Name -> " + s);
            }

            if (splitPoint >= s.length() - 1) {

                throw new LexicalException("Token def format error : Token Regex Body -> " + s);
            }

            //读取body
            regexToken.setPattern(
                    Pattern.compile(
                            s.substring(
                                    splitPoint + 1
                            )
                    )
            );

            //如果返回一个假,说明已经有了一个相同的name了，抛异常
            if (this.allTerminalsHashMap.add(regexToken.getName()) == false) {

                throw new LexicalException("Two or more tokens with the same name : " + regexToken.getName());
            }

            this.allTerminals.add(regexToken);
        }
    }

    /**
     * 开始分析
     *
     * @param inputProgram 输入程序文本
     * @return
     */
    public List<Token> lexicalAnalyze(String inputProgram) throws LexicalException {

        List<Token> tokenList = new ArrayList<>();

        String formattedInput = inputProgram.trim();
        //初始化所有Token的Matcher
        for (RegexToken regexToken : this.allTerminals) {

            regexToken.setMatcher(regexToken.getPattern().matcher(formattedInput));
        }

        int pos = 0;
        boolean hasMatch = false;
        //开始循环用find
        while (pos < formattedInput.length()) {

            //循环看哪个NonTerminal能够首先在pos位置match到
            hasMatch = false;
            for (RegexToken regexToken : this.allTerminals) {

                if (regexToken.getMatcher().region(pos, formattedInput.length()).lookingAt()) {

                    //match到了
                    hasMatch = true;
                    int originalPos = pos;
                    pos = regexToken.getMatcher().end();

                    //只有非delimiter才能加进去
                    if (regexToken.getName().equals(LanguageRulesFile.__delimiterTokenNameFormat) == false) {

                        Token token = new Token(regexToken);
                        token.setDetailedStr(formattedInput.substring(originalPos, pos));
                        tokenList.add(token);
                    }
                }
            }

            if (hasMatch == false) {

                throw new LexicalException("Error Parsing at [" + pos + "]");
            }
        }

        //在末尾加入EndMarker
        tokenList.add(new Token("$", Token.TokenType.EndMarker));
        return tokenList;
    }

    public List<RegexToken> getAllTerminals() {
        return this.allTerminals;
    }


    public HashSet<String> getAllTerminalsHashMap() {
        return allTerminalsHashMap;
    }
}
