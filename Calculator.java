/******************************************************************************                                        
 *  Compilation:  javac Calculator.java                                                                                
 *  Execution:    java Calculator                                                                                      
 *  Dependencies: FlexStack.java                                                                                           
 *                                                                                                                     
 *  Adapted from EvaluteDeluxe.java at                                                                                 
 *    https://algs4.cs.princeton.edu/13stacks/EvaluateDeluxe.java.html                                                 
 *                                                                                                                     
 *  A command-line four-function calculator that evaluates arithmetic                                                  
 *  expressions using Dijkstra's two-stack algorithm.                                                                  
 *  Handles the following binary operators: +, -, *, / and parentheses.                                                
 *                                                                                                                     
 *  Limitation                                                                                                       
 *  --------------                                                                                                     
 *    -  can add additional operators and precedence orders, but they                                                  
 *       must be left associative (exponentiation is right associative)                                                
 *                                                                                                                     
 ******************************************************************************/

import java.util.Stack;
import java.util.Scanner;


public class Calculator {
    // all operators -- for now -- as a string                                                                         
    private static String opsString = "()+-/*";
    public static BST<String, Double> var = new BST<>();
    public static BST<String, AbstractSyntax> func = new BST<>();

    // result of applying binary operator op to two operands val1 and val2                                             
    public static double eval(String op, double val1, double val2) {
        if (op.equals("+")) return val1 + val2;
        if (op.equals("-")) return val1 - val2;
        if (op.equals("/")) {
            if(val2 == 0) {
               throw new ArithmeticException(" <<Division by zero");
            } else {
            return val1 / val2;
            }
        }
        if (op.equals("*")) return val1 * val2;
        throw new RuntimeException(" <<Invalid operator");
    }

    // put spaces around operators to simplify tokenizing                                                              
    public static String separateOps(String in) {
        for (int i = 0; i < opsString.length(); i++) {
            char c = opsString.charAt(i);
            in = in.replace(Character.toString(c), " " + c + " ");
        }
        return in.trim(); // remove leading and trailing spaces                                                        
    }

    public static int precedence(String op) {
        // operator precedence: "(" ")" << "+" "-" << "*" "/"                                                          
        return opsString.indexOf(op) / 2;
    }

    public static Double evaluate(String[] tokens, BST<String, Double> x) {
        // Edsger Dijkstra's shunting-yard (two-stack) algorithm                                                       
        Stack<String> ops  = new Stack<String>();
        Stack<Double> vals = new Stack<Double>();
        // evaluate the input one token at a time                                                                      
        for (String s : tokens) {
            // token is a value                                                                                        
            if (! opsString.contains(s)) {
                if (Parser.alphanum(s)) {
                  if (x.get(s) == null) {
                      if(var.get(s)==null) {
                          throw new RuntimeException(s + " is undefined");
                      }else {
                          vals.push(var.get(s));
                          continue;
                      }
                  } else {
                    vals.push(x.get(s));
                    continue;
                  }
                    
                } else {
                    vals.push(Double.parseDouble(s));
                    continue;
                }
            }
            // token is an operator                                                                                    
            while (true) {
                // the last condition ensures that the operator with                                                   
                // higher precedence is evaluated first                                                                
                if (ops.isEmpty() || s.equals("(") ||
                    (precedence(s) > precedence(ops.peek()))) {
                    ops.push(s);
                    break;
                }
                // evaluate expression                                                                                 
                String op = ops.pop();
                // ignore left parentheses                                                                         
                if (op.equals("("))
                    break;
                else {
                    // evaluate operator and two operands; 
                    // push result to value stack                             
                    double val2 = vals.pop();
                    double val1 = vals.pop();
                    vals.push(eval(op, val1, val2));
                }
            }
        }

        // evaluate operator and operands remaining on two stacks                                                      
        while (!ops.isEmpty()) {
            String op = ops.pop();
            double val2 = vals.pop();
            double val1 = vals.pop();
            vals.push(eval(op, val1, val2));
        }
        // last value on stack is value of expression                                                                  
        return vals.pop();
    }

    public static void main(String[] args) {
        
        Scanner input = new Scanner(System.in);
        // our command line prompt                                                                                     
        System.out.print("> ");
        while (input.hasNext()) {
            try {
                // read in next line as a string                                                                           
                String ln = input.nextLine();
                // tokenize -- separate operands and operators into a string array                                         
                // String[] tokens = separateOps(ln).split("\\s+");
                // // evaluate and print                                                                                      
                // System.out.println(evaluate(tokens));
                Parser p = new Parser(ln);
                AbstractSyntax liangliang = p.parse();
                if (liangliang.getType().equals("eval")) {
                    double value = evaluate(liangliang.getExp(), var);
                    System.out.println(value);
                } else if (liangliang.getType().equals("assign")) {
                    double value = evaluate(liangliang.getExp(), var);
                    var.put(liangliang.getName(), value);
                } else if (liangliang.getType().equals("def")) {
                    func.put(liangliang.getName(), liangliang);
                } else if (liangliang.getType().equals("call")) {
                    AbstractSyntax function = func.get(liangliang.getName());
                    if(function == null) {
                        throw new RuntimeException("undefined function");
                    }
                        
                    BST<String, Double> assignment = new BST<>();
                    if(liangliang.getParams().length > function.getParams().length) {
                        throw new RuntimeException("too many parameters");
                    } else if(liangliang.getParams().length < function.getParams().length) {
                        throw new RuntimeException("insufficient parameters");
                    }
                    for(int i = 0; i < liangliang.getParams().length; i++) {
                        String param = "";
                        boolean error = false;
                        try {
                            param = function.getParams()[i];

                        }catch (Exception e) {
                            error = true;
                            throw new RuntimeException("undefined function");
                            
                        }
                        double val = 0;
                        try{
                                val = var.get(liangliang.getParams()[i]);
                        }catch(Exception e1) {
                            try {
                                val = Double.parseDouble(liangliang.getParams()[i]);
                            } catch(Exception e2) {
                                error = true;
                                throw new RuntimeException("the variable has not been initialized");    
                            }
                        }
                        if(error) continue;
                        assignment.put(param, val);
                       
                    }
                    double value = evaluate(function.getExp(), assignment);
                    System.out.println(value);
                }                                                                                
                
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            System.out.print("> ");
        }
   
    }
}