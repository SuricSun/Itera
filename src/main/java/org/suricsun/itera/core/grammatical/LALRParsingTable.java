package org.suricsun.itera.core.grammatical;

import org.suricsun.itera.core.common.*;

import java.util.*;

/**
 * 核心是构建Action和Goto表
 *
 * @author: SuricSun
 * @date: 2021/8/6
 */
public class LALRParsingTable {

    /**
     *
     */
    private AugGrammar augGrammar;
    /**
     * ！！注意是从token-name(String)映射而不是token(Token)映射<br>
     * 输入一个token name，输出是否可以derive blank
     * true就是可以derive blank
     */
    private HashMap<String, Boolean> deriveBlankMap;
    /**
     * ！！注意是从token-name(String)映射而不是token映射(Token)<br>
     * map from a non-terminal to terminals<br>
     * 这玩意儿是动态生成的，意思就是你调用first函数如果表里面没有的话就会实时计算然后加到这个表里，所以这个表里的数据不是总是齐全的
     */
    private HashMap<String, List<Token>> firstMap;

    private List<LALRState> allStates;
    private List<HashMap<String, LALRAction>> actionMapList;

    public LALRParsingTable(AugGrammar augGrammar) throws GrammaticalException {

        this.init(augGrammar);
    }

    public void init(AugGrammar augGrammar) throws GrammaticalException {

        this.augGrammar = augGrammar;
        this.initBlankMap();
        this.initFirstMap();
        this.item();
    }

    public void initBlankMap() {

        List<Token> allNonTerminals = this.augGrammar.getAllNonTerminals();
        Map<String, List<Production>> sameHeadProdMap = this.augGrammar.getSameHeadProdMap();

        this.deriveBlankMap = new HashMap<>();
        //遍历所有prod，检查是否derive blank
        boolean everFoundOne = true;
        while (true) {

            //如果循环一周没有新的blank被发现就退出，初始化完成
            if (everFoundOne == false) {

                break;
            } else {

                everFoundOne = false;
            }

            for (Token nonTerminal : allNonTerminals) {

                String curTokenName = nonTerminal.getName();

                //循环找到所有derive blank
                //如果这个token name已经检测过就可以跳过了
                //不存在与map中才要检测
                if (this.deriveBlankMap.containsKey(curTokenName) == false) {

                    List<Production> allProdOfThisNonTerminal = sameHeadProdMap.get(nonTerminal.getName());

                    for (Production prod : allProdOfThisNonTerminal) {

                        //-1 应该继续下一个prod，本prod不做行为
                        //0 本prod不派生blank
                        //1 本prod派生blank
                        //默认为1，即派生blank
                        int stateValue = 1;
                        Boolean mappedValue;
                        for (Token token : prod.getBody()) {

                            //如果是terminal肯定是不可能派生空
                            if (token.getType() == Token.TokenType.Terminal) {

                                stateValue = 0;
                                break;

                            } else {

                                mappedValue = this.deriveBlankMap.get(token.getName());
                                if (mappedValue == null) {

                                    //情况未知，跳过
                                    stateValue = -1;
                                    break;
                                }
                                if (mappedValue == false) {

                                    //不派生blank
                                    stateValue = 0;
                                    break;
                                }
                            }
                        }

                        if (stateValue == -1) {

                            continue;
                        }
                        if (stateValue == 0) {

                            //不派生blank,继续此non-terminal
                            everFoundOne = true;
                            this.deriveBlankMap.put(curTokenName, false);
                            continue;
                        }

                        //那否则stateValue就是1
                        //派生blank，跳过此non-terminal，继续下一个non-terminal
                        everFoundOne = true;
                        this.deriveBlankMap.put(curTokenName, true);
                        break;
                    }
                }
            }
        }
    }

    public void initFirstMap() {

        this.firstMap = new HashMap<>();
        //初始化空map
        for (Token token : this.augGrammar.getAllNonTerminals()) {

            this.firstMap.put(token.getName(), new ArrayList<>());
        }

        Map<String, List<Production>> sameHeadProdMap = this.augGrammar.getSameHeadProdMap();

        for (Token prodHeadToken : this.augGrammar.getAllNonTerminals()) {

            String curTokenNameToFirst = prodHeadToken.getName();
            List<Token> curFirstMapEntry = this.firstMap.get(curTokenNameToFirst);
            Set<String> hasAddedTokenName = new HashSet<>();
            //计算这个token的first
            List<Production> calcStack = new ArrayList<>(sameHeadProdMap.get(curTokenNameToFirst));
            hasAddedTokenName.add(curTokenNameToFirst);
            for (int i = 0; i < calcStack.size(); i++) {

                //对于当前的prod
                //遍历身体
                for (Token curTokenInTheBody : calcStack.get(i).getBody()) {

                    //如果是terminal就加入
                    if (curTokenInTheBody.getType() == Token.TokenType.Terminal) {

                        if (curFirstMapEntry.contains(curTokenInTheBody) == false) {

                            curFirstMapEntry.add(curTokenInTheBody);
                        }
                        break;

                    } else {

                        //否则生成并检查blank情况
                        if (hasAddedTokenName.add(curTokenInTheBody.getName()) == true/*就应该是true嗷*/) {

                            calcStack.addAll(sameHeadProdMap.get(curTokenInTheBody.getName()));
                        }
                        Boolean deriveBlankState = this.deriveBlankMap.get(curTokenInTheBody.getName());
                        if (deriveBlankState == false) {

                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 返回的数据可以是内部使用的数据，小心使用,可以选择复制一份，见下面的参数<br>
     * 注意只是List是新创建的，list里面的元素的数据都是原来的，所以不能随便更改list里面的元素的数据<br>
     * 但是你可以添加删除里面的元素
     *
     * @param tokenName    要被first的token
     * @param createNewOne 返回的数据是否是内部数据的拷贝，拷贝数据以防止意外修改内部数据
     * @return
     */
    public List<Token> first(String tokenName, boolean createNewOne) {

        List<Token> tokenList = this.firstMap.get(tokenName);
        assert (tokenList != null);
        if (createNewOne) {

            List<Token> toBeReturned;
            toBeReturned = new ArrayList<>(tokenList);
            return toBeReturned;
        } else {

            return tokenList;
        }
    }

    /**
     * 返回的List始终是复制的，不过list里面的内容不是复制的<br>
     * 可以对list添加删除里面的元素但是不能修改list里面的元素的值
     *
     * @param tokenListIn
     * @param from         不对参数进行检查
     * @param to           不对参数进行检查
     * @param toBeReturned 需要调用方初始化
     * @return 访问范围为 [from,to)
     */
    public boolean first(List<Token> tokenListIn, int from, int to, List<Token> toBeReturned) {

        //对于每个token进行first直到遇到不可blank的
        //把每次的结果加入最终的集合
        List<Token> firstReturned;

        //因为我使用的是ArrayList所以用for进行随机访问性能不会有半点损失
        Token curToken;
        for (int i = from; i < to; i++) {

            curToken = tokenListIn.get(i);
            //如果curToken是终止符那么first就是她本身
            if (curToken.getType() == Token.TokenType.Terminal) {

                if (toBeReturned.contains(curToken) == false) {

                    toBeReturned.add(curToken);
                }
                //遇上break肯定是派生不了blank了，退出函数
                return false;

            } else {

                firstReturned = this.firstMap.get(curToken.getName());
                for (Token t : firstReturned) {

                    if (toBeReturned.contains(t) == false) {

                        toBeReturned.add(t);
                    }
                }
                Boolean isDeriveBlank = this.deriveBlankMap.get(curToken.getName());
                if (isDeriveBlank == false) {

                    //结束了，而且是提前结束，不能返回true，得返回false，这个list并不能派生blank
                    return false;
                }
            }
        }
        //运行到这里说明所有的token都是派生blank的
        //那么这个list就派生blank，就得返回true
        return true;
    }

    /**
     * 返回的List里面的Token是内部使用的
     *
     * @param inItem
     */
    public List<Token> first(LALRItem inItem) {

        List<Token> toBeReturned = new ArrayList<>();

        boolean deriveBlank = this.first(inItem.getProduction().getBody(), inItem.getPos() + 1, inItem.getProduction().getBody().size(), toBeReturned);
        if (deriveBlank == true) {

            for (Token scndCpnt : inItem.getScndCpnt()) {

                if (toBeReturned.contains(scndCpnt) == false) {

                    toBeReturned.add(scndCpnt);
                }
            }
        }

        return toBeReturned;
    }

    /**
     * 直接在输入数据上进行Closure
     *
     * @param inLALRState
     */
    public void closure(LALRState inLALRState) {

        List<LALRItem> inStateItems = inLALRState.getLalrItems();
        int curItemIdx = -1;
        LALRItem curItem;

        HashMap<String, List<Production>> sameHeadProdMap = this.augGrammar.getSameHeadProdMap();
        //对于里面的每个item,取closure
        List<Production> everyProdFind;
        List<Token> firstReturned;

        List<Integer>[] lists = new List[2];
        lists[0] = new ArrayList<>();
        lists[1] = new ArrayList<>();
        int curListIdx = 0;
        int nxtListIdx = (curListIdx + 1) % 2;
        List<Integer> curList;
        List<Integer> nxtList;

        //初始化
        for (int i = 0; i < inStateItems.size(); i++) {

            lists[curListIdx].add(i);
        }

        //Start Closure
        while (true) {

            curList = lists[curListIdx];
            nxtList = lists[nxtListIdx];

            if (curList.size() <= 0) {

                //当前list里面什么都没有，退出循环
                break;
            }

            curListIdx = (curListIdx + 1) % 2;
            nxtListIdx = (curListIdx + 1) % 2;

            //clear nxtList
            nxtList.clear();

            for (int i = 0; i < curList.size(); i++) {

                curItemIdx = curList.get(i);
                curItem = inStateItems.get(curItemIdx);
                //如果没有isOver就继续
                if (curItem.isOver() == false) {
                    //如果是Non-Terminal就继续
                    if (curItem.tokenAtCurPos().getType() == Token.TokenType.Non_Terminal) {
                        //查找对应的所有prod并计算相应的scndCpnt，加入之前看看是否item已经存在，存在的话就merge item
                        //对于当前item生成的新的prod，转化为item，如果已存在就融合否则加入
                        firstReturned = this.first(curItem);
                        everyProdFind = sameHeadProdMap.get(curItem.tokenAtCurPos().getName());
                        for (Production p : everyProdFind) {

                            LALRItem itemCreated = new LALRItem(p);
                            itemCreated.getScndCpnt().addAll(firstReturned);
                            //检测是否存在
                            int foundIdx = 0;
                            for (; foundIdx < inStateItems.size(); foundIdx++) {

                                if (itemCreated.coreEqual(inStateItems.get(foundIdx))) {

                                    break;
                                }
                            }

                            if (foundIdx >= inStateItems.size()) {

                                //didnt found
                                //add one
                                //加入
                                inStateItems.add(itemCreated);
                                //加入nxtList
                                if (nxtList.contains(foundIdx) == false) {

                                    nxtList.add(foundIdx);
                                }

                            } else {

                                //found , merge
                                //如果是真merge
                                if (inStateItems.get(foundIdx).mergeToSelf(itemCreated)) {

                                    //如果是在前面
                                    if (foundIdx < curItemIdx) {

                                        //add to nxtList only when
                                        if (nxtList.contains(foundIdx)) {

                                            nxtList.add(foundIdx);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public LALRState go_to(LALRState inLALRState, String gotoTokenName) {

        LALRState toBeReturnedState = new LALRState();
        //在state里面找到所有tokenAtPos名称为gotoTokenName的Item，把他们复制一份加到新的state里面并closure
        for (LALRItem item : inLALRState.getLalrItems()) {

            //如果没有over
            if (item.isOver() == false) {

                if (item.tokenAtCurPos().getName().equals(gotoTokenName)) {

                    LALRItem newItem = new LALRItem(item);
                    newItem.posAddOne();
                    toBeReturnedState.getLalrItems().add(newItem);
                }
            }
        }

        this.closure(toBeReturnedState);

        return toBeReturnedState;
    }

    /**
     * 生成LALR ITEM
     */
    public void item() throws GrammaticalException {

        List<Integer> toBeAfter = new ArrayList<>();

        this.allStates = new ArrayList<>();
        this.actionMapList = new ArrayList<>();
        LALRItem initItem = new LALRItem();
        LALRState initState = new LALRState();
        //开始生成
        //首先创建初始State
        initItem.setProduction(this.augGrammar.getProductions().get(0));
        initItem.getScndCpnt().add(new Token("$", Token.TokenType.EndMarker));
        initState.getLalrItems().add(initItem);
        this.closure(initState);

        this.allStates.add(initState);
        this.actionMapList.add(new HashMap<>());

        //已经有第一个state了，开始循环
        Set<String> hasBeenGoto = new HashSet<>();
        String curGotoTokenName;
        LALRState curState;
        LALRState newGotoState;

        //对于每个state
        for (int everyState = 0; everyState < this.allStates.size(); everyState++) {

            curState = this.allStates.get(everyState);
            hasBeenGoto.clear();

            //对于每个在state中的item
            for (LALRItem item : curState.getLalrItems()) {

                //是否有资格goto
                if (item.isOver()) {

                    continue;
                }

                curGotoTokenName = item.tokenAtCurPos().getName();

                //没有goto过才能继续
                if (hasBeenGoto.add(curGotoTokenName) == false) {

                    continue;
                }

                newGotoState = this.go_to(curState, curGotoTokenName);
                //检查新产生的newGotoState是否CoreExist
                int foundIdx = 0;
                for (; foundIdx < this.allStates.size(); foundIdx++) {

                    if (this.allStates.get(foundIdx).coreEqual(newGotoState)) {

                        break;
                    }
                }

                if (foundIdx >= this.allStates.size()) {

                    //不存在就加入
                    this.allStates.add(newGotoState);
                    this.actionMapList.add(new HashMap<>());

                } else {

                    //存在就融合
                    //如果是真merge，融合之后这个item需要加入afterItem用来再次更新()
                    if (this.allStates.get(foundIdx).mergeToSelf(newGotoState)) {

                        //如果foundState在当前state前面
                        if (foundIdx < everyState) {

                            if(toBeAfter.contains(foundIdx) == false){

                                toBeAfter.add(foundIdx);
                            }
                        }
                    }
                }

                //添加Shift Map
                LALRAction lalrAction = new LALRAction();
                lalrAction.action = LALRAction.ActionType.ShiftToState;
                lalrAction.shiftToState = foundIdx;
                this.actionMapList.get(everyState).put(curGotoTokenName, lalrAction);
            }
        }

        //必不可少
        this.afterItem(toBeAfter);
    }

//    public void item() throws GrammaticalException {
//
//        //初始化变量
//        this.allStates = new ArrayList<>();
//        this.actionMapList = new ArrayList<>();
//        LALRItem initItem = new LALRItem();
//        LALRState initState = new LALRState();
//
//        //初始化第一个State
//        initItem.setProduction(this.augGrammar.getProductions().get(0));
//        initItem.getScndCpnt().add(new Token("$", Token.TokenType.EndMarker));
//        initState.getLalrItems().add(initItem);
//        this.closure(initState);
//        this.allStates.add(initState);
//        this.actionMapList.add(new HashMap<>());
//
//        //其他辅助变量
//        Set<String> hasBeenGoto = new HashSet<>();
//        String curGotoTokenName;
//        int curStateIdx = -1;
//        LALRState curState;
//        LALRState newGotoState;
//
//        //开始建立循环
//        List<Integer>[] lists = new List[2];
//        lists[0] = new ArrayList<>();
//        lists[1] = new ArrayList<>();
//        int curListIdx = 0;
//        int nxtListIdx = (curListIdx + 1) % 2;
//        List<Integer> curList;
//        List<Integer> nxtList;
//
//        //初始化循环
//        //加入第一个State的index
//        lists[curListIdx].add(0);
//
//        //循环
//        while (true) {
//
//            curList = lists[curListIdx];
//            nxtList = lists[nxtListIdx];
//
//            if (curList.size() <= 0) {
//
//                //当前list里面什么都没有，退出循环
//                break;
//            }
//
//            curListIdx = (curListIdx + 1) % 2;
//            nxtListIdx = (curListIdx + 1) % 2;
//
//            //clear nxtList
//            nxtList.clear();
//
//            //对于每个state
//            for (int i = 0; i < curList.size(); i++) {
//
//                curStateIdx = curList.get(i);
//                curState = this.allStates.get(curStateIdx);
//                hasBeenGoto.clear();
//
//                //对于每个在state中的item
//                for (LALRItem item : curState.getLalrItems()) {
//
//                    //是否有资格goto
//                    if (item.isOver()) {
//
//                        continue;
//                    }
//
//                    curGotoTokenName = item.tokenAtCurPos().getName();
//
//                    //没有goto过才能继续
//                    if (hasBeenGoto.add(curGotoTokenName) == false) {
//
//                        continue;
//                    }
//
//                    newGotoState = this.go_to(curState, curGotoTokenName);
//                    //检查新产生的newGotoState是否CoreExist
//                    int foundIdx = 0;
//                    for (; foundIdx < this.allStates.size(); foundIdx++) {
//
//                        if (newGotoState.coreEqual(this.allStates.get(foundIdx))) {
//
//                            break;
//                        }
//                    }
//
//                    if (foundIdx >= this.allStates.size()) {
//
//                        //不存在就加入
//                        this.allStates.add(newGotoState);
//                        this.actionMapList.add(new HashMap<>());
//                        //加入nxtList
//                        if(nxtList.contains(foundIdx) == false){
//
//                            nxtList.add(foundIdx);
//                        }
//
//                    } else {
//
//                        //存在就融合
//                        //如果是真merge，融合之后这个item需要加入afterItem用来再次更新()
//                        if (this.allStates.get(foundIdx).mergeToSelf(newGotoState)) {
//
//                            //如果foundState在当前state前面
//                            if (foundIdx < curStateIdx) {
//
//                                if(nxtList.contains(foundIdx) == false){
//
//                                    nxtList.add(foundIdx);
//                                }
//                            }
//                        }
//                    }
//
//                    //添加Shift Map
//                    LALRAction lalrAction = new LALRAction();
//                    lalrAction.action = LALRAction.ActionType.ShiftToState;
//                    lalrAction.shiftToState = foundIdx;
//                    this.actionMapList.get(curStateIdx).put(curGotoTokenName, lalrAction);
//                }
//            }
//        }
//
//        this.initReduceMap();
//    }

    public void afterItem(List<Integer> toBeAfter) throws GrammaticalException {

        LALRState curState;
        HashMap<String, LALRAction> curStateActionMap;

        List<Integer>[] lists = new List[2];
        //初始化
        lists[0] = new ArrayList<>(toBeAfter);
        lists[1] = new ArrayList<>();
        int curListIdx = 0;
        int nxtListIdx = (curListIdx + 1) % 2;
        List<Integer> curList;
        List<Integer> nxtList;

        while (true) {

            curList = lists[curListIdx];
            nxtList = lists[nxtListIdx];

            if (curList.size() <= 0) {

                //当前list里面什么都没有，退出循环
                break;
            }

            curListIdx = (curListIdx + 1) % 2;
            nxtListIdx = (curListIdx + 1) % 2;

            //clear nxtList
            nxtList.clear();

            //每一次循环，更新当前list里面的所有state，并初始化下一个list(如果有的话),直到list是空的话就退出
            for (int i = 0; i < curList.size(); i++) {

                int stateIdx = curList.get(i);

                curState = this.allStates.get(stateIdx);
                curStateActionMap = this.actionMapList.get(stateIdx);
                //对state进行重新closure以及重新goto
                this.closure(curState);
                //重新goto
                //遍历这个state的actionMap因为此时这个actionMap里面全是shift操作（reduce操作还没有做）
                for (String gotoTokenName : curStateActionMap.keySet()) {

                    int mappedTo = curStateActionMap.get(gotoTokenName).shiftToState;
                    boolean realMerge = this.allStates.get(mappedTo).mergeToSelf(this.go_to(curState, gotoTokenName));

                    if (realMerge) {

                        //在真merge的情况下如果没有加入的情况下，加入此需要更新的stateIdx，即mappedTo
                        if (nxtList.contains(mappedTo) == false) {

                            nxtList.add(mappedTo);
                        }
                    }
                }
            }
        }

        this.initReduceMap();
    }

    public void initReduceMap() throws GrammaticalException {

        //初始化Reduce表
        //遍历所有State的item，并添加map，如果有重复就抛异常

        //对于每个State
        LALRState curState;
        LALRAction lalrAction;
        HashMap<String, LALRAction> curActionMap;
        for (int everyStateIdx = 0; everyStateIdx < this.allStates.size(); everyStateIdx++) {

            curActionMap = this.actionMapList.get(everyStateIdx);
            //变量复用
            curState = this.allStates.get(everyStateIdx);

            //对于state里面的每个item
            for (LALRItem item : curState.getLalrItems()) {

                //检查是否有资格reduce
                if (item.isOver()) {

                    //开始添加Reduce的action
                    for (Token scndCpnt : item.getScndCpnt()) {

                        lalrAction = new LALRAction();
                        //如果当前是endmaker和item prod idx = 0，就能Acc了
                        if (item.getProduction().equals(this.augGrammar.getProductions().get(0))
                                && scndCpnt.getType() == Token.TokenType.EndMarker) {

                            lalrAction.action = LALRAction.ActionType.Accept;

                        } else {

                            lalrAction.action = LALRAction.ActionType.ReduceWithProd;
                            lalrAction.reduceWithProd = item.getProduction();
                        }

                        //开始加入
                        if (curActionMap.containsKey(scndCpnt.getName())) {

                            throw new GrammaticalException("Not a LALR grammar");

                        } else {

                            curActionMap.put(scndCpnt.getName(), lalrAction);
                        }
                    }
                }
            }
        }
    }

    public AugGrammar getAugGrammar() {
        return augGrammar;
    }

    public void setAugGrammar(AugGrammar augGrammar) {
        this.augGrammar = augGrammar;
    }

    public HashMap<String, Boolean> getDeriveBlankMap() {
        return deriveBlankMap;
    }

    public HashMap<String, List<Token>> getFirstMap() {
        return firstMap;
    }

    public List<LALRState> getAllStates() {
        return allStates;
    }

    public List<HashMap<String, LALRAction>> getActionMapList() {
        return actionMapList;
    }
}

/**
 * LALR Item，包含一个prod和pos
 */
class LALRItem {

    /**
     * 直接引用LALRParsingTable的Production, 不要改变它的值
     */
    private Production production;

    /**
     * LALR pos
     */
    private int pos = 0;
    /**
     * 预测位
     */
    private List<Token> scndCpnt = new ArrayList<>();

    public LALRItem() {

    }

    /**
     * 用production来初始化this.production，其他的还是默认值，pos=0，scndCpnt=new ArrayList<>()
     *
     * @param production
     */
    public LALRItem(Production production) {

        this.production = production;
    }

    /**
     * 除了production是直接复制引用, pos和scndCpnt都是复制一个新的
     *
     * @param inItem
     */
    public LALRItem(LALRItem inItem) {

        this.production = inItem.production;
        this.pos = inItem.pos;
        this.scndCpnt = new ArrayList<>(inItem.scndCpnt);
    }

    /**
     * 是否IsOver
     *
     * @return
     */
    public boolean isOver() {

        return (this.pos >= this.production.getBody().size());
    }

    public boolean coreEqual(LALRItem inItem) {

        return this.production.equals(inItem.production) && this.pos == inItem.pos;
    }

    /**
     * 返回的Token是内部使用的token不要擅自更改
     *
     * @return
     */
    public Token tokenAtCurPos() {

        return this.production.getBody().get(this.pos);
    }

    public void posAddOne() {

        this.pos++;
    }

    /**
     * 不会检查两个lalrItem会不会CoreEqual，只会单纯merge
     *
     * @param lalrItem
     * @return true就是真merge，否则是假merge
     */
    public boolean mergeToSelf(LALRItem lalrItem) {

        boolean realMerge = false;
        for (Token scndCpnt : lalrItem.getScndCpnt()) {

            if (this.scndCpnt.contains(scndCpnt) == false) {

                realMerge = true;
                this.scndCpnt.add(scndCpnt);
            }
        }

        return realMerge;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(this.production.getHead().getName()).append("=");

        for (int i = 0; i < this.production.getBody().size(); i++) {

            if (this.pos == i) {

                sb.append("@");
            }
            sb.append(this.production.getBody().get(i)).append(" ");
        }

        return sb.toString();
    }

    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }

    public List<Token> getScndCpnt() {
        return scndCpnt;
    }

    public void setScndCpnt(List<Token> scndCpnt) {
        this.scndCpnt = scndCpnt;
    }

    public int getPos() {
        return pos;
    }
}

/**
 * LARA State，包括LALR Items,仅此而已
 */
class LALRState {

    private List<LALRItem> lalrItems = new ArrayList<>();

    public LALRState() {

    }

    public boolean coreEqual(LALRState inLALRState) {

        if (this.lalrItems.size() != inLALRState.lalrItems.size()) {

            return false;
        }

        LALRItem found;
        for (LALRItem thisItem : this.lalrItems) {
            //找到至少一个coreEqual
            found = inLALRState.lalrItems.stream()
                    .filter(inItem -> inItem.coreEqual(thisItem))
                    .findFirst()
                    .orElse(null);
            if (found == null) {

                return false;
            }
        }

        return true;
    }

    /**
     * 不会检查两个state是否coreEqual，单纯合并两个state中core equal items的scndCpnt
     *
     * @param inLALRState
     * @return true就是真merge，否则就是假merge
     */
    public boolean mergeToSelf(LALRState inLALRState) {

        boolean realMerge = false;

        LALRItem found;
        for (LALRItem thisItem : this.lalrItems) {

            found = inLALRState.lalrItems.stream()
                    .filter(inItem -> inItem.coreEqual(thisItem))
                    .findFirst()
                    .orElse(null);

            if (found != null) {

                //merge
                if (thisItem.mergeToSelf(found)) {

                    realMerge = true;
                }
            }
        }

        return realMerge;
    }

    public List<LALRItem> getLalrItems() {
        return lalrItems;
    }

    public void setLalrItems(List<LALRItem> lalrItems) {
        this.lalrItems = lalrItems;
    }
}

