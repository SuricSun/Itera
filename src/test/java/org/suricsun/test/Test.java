package org.suricsun.test;

import org.suricsun.itera.core.common.*;
import org.suricsun.itera.core.grammatical.*;
import org.suricsun.itera.core.lexical.Lexer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.text.NumberFormat;
import java.util.List;

/**
 * @author: SuricSun
 * @date: 2021/8/8
 */
public class Test {

    @org.junit.Test
    public void test() {

        long globalTime = 0;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(10);

        long startTime = System.nanoTime();
        LanguageRulesFile languageRulesFile = new LanguageRulesFile();
        languageRulesFile.setFilePath("C:\\Users\\suric\\Desktop\\Itera Doc.txt");
        try {
            languageRulesFile.formatConfigFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("lan file parse: " + nf.format((double) (System.nanoTime() - startTime) * 0.000001d));
//        System.out.println(languageRulesFile.getFormattedTokenDef());
//        System.out.println(languageRulesFile.getFormattedProductionDef());

        try {

            startTime = System.nanoTime();
            Lexer lexer = new Lexer(languageRulesFile.getFormattedTokenDef());
            System.out.println("init lexer: " + nf.format((double) (System.nanoTime() - startTime) * 0.000001d));
            //Serializer.Serialize(lexer, "C:\\Users\\suric\\Desktop\\lexer.baked");

            //Lexer dlexer = (Lexer) Deserializer.deserialize("C:\\Users\\suric\\Desktop\\lexer.baked");
            startTime = System.nanoTime();
            List<Token> tokens = lexer.lexicalAnalyze("id=f(f(a*b+f()*c-v-f()*d/f(a)))");
            System.out.println("lex input: " + nf.format((double) (System.nanoTime() - startTime) * 0.000001d));
            startTime = System.nanoTime();
            AugGrammar ag = new AugGrammar(languageRulesFile.getFormattedProductionDef(), lexer);
            System.out.println("init aug: " + nf.format((double) (System.nanoTime() - startTime) * 0.000001d));
            startTime = System.nanoTime();
            LALRParsingTable lalrParsingTable = new LALRParsingTable(ag);
            System.out.println("init parsing table: " + nf.format((double) (System.nanoTime() - startTime) * 0.000001d));

            LALRParser parser = new LALRParser();
            parser.eat(lalrParsingTable);
            //LALRParser parser = (LALRParser) Deserializer.deserialize("C:\\Users\\suric\\Desktop\\parser.baked");

            //Serializer.Serialize(parser,"C:\\Users\\suric\\Desktop\\parser.baked");

            startTime = System.nanoTime();
            List<LALRAction> actionList = parser.parse(tokens);
            System.out.println("parse input: " + nf.format((double) (System.nanoTime() - startTime) * 0.000001d));

            for (LALRAction action : actionList) {

                if (action.action == LALRAction.ActionType.ShiftToState) {

                    System.out.println("Shift");

                } else if (action.action == LALRAction.ActionType.ReduceWithProd) {

                    System.out.println("Reduce With : " + action.reduceWithProd.toString());

                } else {

                    System.out.println("Acc");
                }
            }

            startTime = System.nanoTime();
            AstTreeNode node = AstGenerator.GenerateTree(tokens, actionList);
            System.out.println("Gen Ast: " + nf.format((double) (System.nanoTime() - startTime) * 0.000001d));

            //显示AST
            AstTreeNode curNode = node;
            DefaultMutableTreeNode curTn = new DefaultMutableTreeNode(node.getToken().toString());

            visitTree(curNode, curTn);


            JFrame f;
            f = new JFrame();
            JTree jt = new JTree(curTn);
            for (int i = 0; i < jt.getRowCount(); i++) {
                jt.expandRow(i);
            }
            f.add(jt);
            f.setSize(500, 500);
            f.setVisible(true);

            Thread.sleep(1000 * 60);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //遍历每个node

    }

    public static void visitTree(AstTreeNode node, DefaultMutableTreeNode treeNode) {

        AstTreeNode childNode;
        for (int i = 0; i < node.getChildNodes().size(); i++) {

            childNode = node.getChildNodes().get(i);
            //对于每个child，遍历
            DefaultMutableTreeNode tn = new DefaultMutableTreeNode(childNode.getToken().toString());
            treeNode.add(tn);
            visitTree(childNode, tn);
        }
    }
}
