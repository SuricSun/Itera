package org.suricsun.itera.core.common;

/**
 * 用来序列化Lexer和LALRParsingTable，后续执行就可以直接反序列化得到类而不用每次都重新生成Lexer和LALRParsingTable类
 * 因为他们的初始化太过昂贵而且每次的初始化得到的结果都是一样的，因此我们需要提前序列化他们，这里起名为“烘焙”，引用自计算机图形学
 * @author: SuricSun
 * @date: 2021/8/6
 */
public class BakingTool {

    static void Bake(String rulesFile, String outputPath){


    }
}
