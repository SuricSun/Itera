package org.suricsun.itera.core.grammatical;

import org.suricsun.itera.core.common.*;
import org.suricsun.itera.core.lexical.Lexer;

import java.util.*;
import java.util.regex.Matcher;

/**
 * 负责生成AugGrammar和FIRST表
 */
public class AugGrammar {

    /**
     * 有序的
     */
    private List<Production> productions;
    /**
     * 有序的
     */
    private List<Token> allNonTerminals;

    private HashMap<String, List<Production>> sameHeadProdMap;
    /**
     * 并非由此类初始化，而是由外部传入,有序的
     */
    private List<RegexToken> allTerminalsFromLexer;

    /**
     * 等同于调用eat
     *
     * @param prodDef
     * @see AugGrammar#eat(String, Lexer)
     */
    public AugGrammar(String prodDef, Lexer lxr) throws GrammaticalException {

        this.eat(prodDef, lxr);
    }

    /**
     * 用production定义来重新配置此AugGrammar
     *
     * @param prodDef
     */
    public void eat(String prodDef, Lexer lxr) throws GrammaticalException {

        this.allTerminalsFromLexer = lxr.getAllTerminals();
        HashSet<String> allTerminalTokenSet = lxr.getAllTerminalsHashMap();

        String[] splitStr = prodDef.split("\n");

        this.productions = new ArrayList<>();
        this.allNonTerminals = new ArrayList<>();
        this.sameHeadProdMap = new HashMap<>();
        //set不保存重复数据，保证了prod的唯一性
        HashSet<String> productionsBodySet = new HashSet<>();
        HashSet<String> hasAddedNonTerminalName = new HashSet<>();

        Matcher tokenNameFormatMatcher = LanguageRulesFile.TokenNameFormat.matcher("iloveyou:)");

        int splitPoint;
        int end;

        String curProdHeadTokenName;

        for (String prodRawStr : splitStr) {

            Production production = new Production();

            //读取head
            //reset
            tokenNameFormatMatcher.reset(prodRawStr);

            if (tokenNameFormatMatcher.region(0, prodRawStr.length()).lookingAt() == false) {

                throw new GrammaticalException("Production format error : Head -> " + prodRawStr);
            }

            end = tokenNameFormatMatcher.end();

            curProdHeadTokenName = prodRawStr.substring(0, end);

            //检查head在allTerminal里面存不存在
            if (allTerminalTokenSet.contains(curProdHeadTokenName)) {

                throw new GrammaticalException("Production format error : Duplicated token name -> " + curProdHeadTokenName);
            }

            production.setHead(new Token(curProdHeadTokenName, Token.TokenType.Non_Terminal));

            //读取=
            splitPoint = prodRawStr.indexOf('=');

            if (splitPoint == -1) {

                throw new GrammaticalException("Production format error : no '=' -> " + prodRawStr);
            }

            if (splitPoint != end) {

                throw new GrammaticalException("Production format error : Head -> " + prodRawStr);
            }

            if (splitPoint >= prodRawStr.length() - 1) {

                //没有body代表派生空
                //就不用读body了,直接下一步

            } else {

                //读取body
                int startPos = splitPoint + 1;
                int endInner;

                Token tokenTmp;

                while (true) {

                    tokenTmp = new Token();

                    //读取ShouldReserve符号
                    if (LanguageRulesFile.ShouldReserveInAstTree.charAt(0) == prodRawStr.charAt(startPos)) {

                        tokenTmp.setShouldReserveInAstTree(true);

                    } else if (LanguageRulesFile.ShouldNotReserveInAstTree.charAt(0) == prodRawStr.charAt(startPos)) {

                        tokenTmp.setShouldReserveInAstTree(false);

                    } else {

                        throw new GrammaticalException("Production format error : Body -> " + prodRawStr);
                    }

                    startPos++;

                    if (tokenNameFormatMatcher.region(startPos, prodRawStr.length()).lookingAt() == false) {

                        throw new GrammaticalException("Production format error : Body -> " + prodRawStr);
                    }

                    endInner = tokenNameFormatMatcher.end();

                    tokenTmp.setName(prodRawStr.substring(startPos, endInner));
                    tokenTmp.setType(Token.TokenType.Unknown);

                    production.getBody().add(tokenTmp);

                    //如果endInner到头了成功返回
                    if (endInner >= prodRawStr.length()) {

                        break;
                    }

                    startPos = endInner;
                }
            }

            //无冗余就加入
            if (productionsBodySet.add(prodRawStr) == false) {

                throw new GrammaticalException("Production format error : Same production -> " + prodRawStr);
            }

            //加入ProdList
            this.productions.add(production);
            String name = production.getHead().getName();

            //加入ProdHeadList
            if (hasAddedNonTerminalName.add(name) == true) {

                //AllNonTerminalList里面只能有一个NonTerminal，不能有重复
                this.allNonTerminals.add(production.getHead());
            }

            if (this.sameHeadProdMap.containsKey(name) == false) {

                this.sameHeadProdMap.put(name, new ArrayList<>());
            }
            this.sameHeadProdMap
                    .get(name)
                    .add(production);
        }

        //检查是否为aug grammar
        if (this.sameHeadProdMap.get(this.allNonTerminals.get(0).getName()).size() > 1) {

            throw new GrammaticalException("Not an Augmented Grammar");
        }

        //最后一步遍历所有prod的body，看body里面的所有token是否存在并且更新他们是终止符还是非终止符
        for (Production prod : this.productions) {

            for (Token token : prod.getBody()) {

                //现在non-terminal 和 terminal 不可能有交集
                if (allTerminalTokenSet.contains(token.getName())) {

                    token.setType(Token.TokenType.Terminal);

                } else if (hasAddedNonTerminalName.contains(token.getName())) {

                    token.setType(Token.TokenType.Non_Terminal);

                } else {

                    throw new GrammaticalException("Production format error, Token name was never defined : " + token.getName());
                }
            }
        }
    }

    public List<Production> getProductions() {
        return productions;
    }

    public List<Token> getAllNonTerminals() {
        return allNonTerminals;
    }

    public HashMap<String, List<Production>> getSameHeadProdMap() {
        return sameHeadProdMap;
    }
}
