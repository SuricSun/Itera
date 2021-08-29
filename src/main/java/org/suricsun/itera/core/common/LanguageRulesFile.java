package org.suricsun.itera.core.common;

import java.io.*;
import java.util.regex.Pattern;

/**
 * @author: SuricSun
 * @date: 2021/8/6
 */
public class LanguageRulesFile {

    /**
     * 定义在文件中的tokenName部分的格式
     */
    public static final Pattern TokenNameFormat = Pattern.compile("[a-zA-Z_]+[\\w_]*");
    /**
     * 词法解析中有个重要的“分隔符”，他们起分隔作用但是不会被加入到解析流中，如大多数语言的空格符号
     */
    public static final String __delimiterTokenNameFormat = "__d";
    public static final String ShouldReserveInAstTree = "@";
    public static final String ShouldNotReserveInAstTree = "#";

    String filePath;

    //格式化之后的Token配置
    StringBuilder formattedTokenDef;

    //格式化之后的Production配置
    StringBuilder formattedProductionDef;

    /**
     * 手动设置filePath和调用formatConfigFile
     */
    public LanguageRulesFile() {

    }

    public LanguageRulesFile(String filePath) throws LanguageRulesFileException {

        this.filePath = filePath;
        this.formatConfigFile();
    }

    /**
     * 调用之前先设置filePath
     *
     */
    public void formatConfigFile() throws LanguageRulesFileException {

        this.formattedTokenDef = new StringBuilder();
        this.formattedProductionDef = new StringBuilder();
        //核心代码
        try {
            FileReader reader = new FileReader(this.filePath);
            BufferedReader bufferedReader = new BufferedReader(reader);
            //依次读取所有行
            //:开头就是Token
            //>开头就是Production
            String line;
            while (true) {

                line = bufferedReader.readLine();

                if (line == null) {

                    break;
                }
                //如果此行有字符 最小为2个
                if (line.length() > 1) {

                    if (line.charAt(0) == ':') {

                        this.formattedTokenDef.append(line.substring(1)).append("\n");

                    } else if (line.charAt(0) == '>') {

                        this.formattedProductionDef.append(line.substring(1)).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(this.formattedTokenDef.length()<=0){

            throw new LanguageRulesFileException("No Token Def");
        }
        if(this.formattedProductionDef.length()<=0){

            throw new LanguageRulesFileException("No Production Def");
        }
    }

    public String getFilePath() {

        return this.filePath;
    }

    public void setFilePath(String filePath) {

        this.filePath = filePath;
    }

    public String getFormattedTokenDef() {
        return formattedTokenDef.toString();
    }

    public String getFormattedProductionDef() {
        return formattedProductionDef.toString();
    }
}
