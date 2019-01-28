package Coder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

public class Controller {

    private String input;   //input string to code frome file
    private double[] probability;   //input probability frome file
    private char[] refLetters;  //input dictionary letters from file

    private String[] words; //input string divided into words
    private char[][] letters;   //words divided into letters
    private double[] section;   //probability estimate
    private double segmentMinTemp;  //minimal segment value for coding string
    private double segmentMaxTemp;  //maximal segment value for coding string
    private double[][] segmentMin;  //table with minimal segment values
    private double[][] segmentMax;  //table with maximal segment values
    private int[][] length; //table with coded string length

    private double[][] decimalResult;
    private String[][] binaryResult;

    private final double precision=1.0E-12;
    private final int maximalWordLenth=50;
    private final int binaryIteration=100;

    public Controller()
    {
        input = new String();
        try {
            Model m = new Model();
            input = m.getStringToCode();

            //put values from input map to tables
            Double[] tempProb = m.getProbability();
            Character[] tempLett = m.getLetters();
            probability = new double[tempProb.length];
            refLetters = new char[tempLett.length];
            for (int i=0; i<tempProb.length; i++)
            {
                refLetters[i]=tempLett[i].charValue();
                probability[i]=tempProb[i].doubleValue();
            }
            initializeInput();
            calculateResult();
            saveToFiles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeInput() {
        //parse input string
        input = input.toLowerCase();
        input = input.replaceAll("[\\t\\n\\r]", " ");
        input = input.replaceAll("[^a-ząęśćńźółż ]", "");
        input = input.trim().replaceAll(" +", " ");

        //split input string to words
        words = input.split(" ");

        //split words to letters
        letters = new char[words.length][maximalWordLenth];
        for (int i = 0; i < words.length; i++) {
            for (int j = 0; j < words[i].length(); j++) {
                letters[i] = words[i].toCharArray();
            }
        }

        //conversion of probability into an estimate
        section = new double[probability.length+1];
        for(int i=0; i<section.length; i++)
        {
            if(i==0)
                section[i]=0;
            else if(i==section.length-1)
                section[i]=1;
            else
                section[i]=section[i-1]+probability[i-1];
        }
    }

    public void calculateResult() throws Exception {
        decimalResult = new double[letters.length][10];
        binaryResult = new String[letters.length][10];
        segmentMin = new double[letters.length][10];
        segmentMax = new double[letters.length][10];
        length = new int[letters.length][10 + 1];


        //result calculating
        for (int i = 0; i < letters.length; i++) {
            segmentMinTemp = 0.0;
            segmentMaxTemp = 1.0;
            double tempmin;
            double tempmax;
            int index = 0;
            for (int j = 0; j < letters[i].length; j++) {
                for (int k = 0; k < section.length - 1; k++) {
                    if (letters[i][j] == refLetters[k]) {
                        if (section[k] != 1.0 || section[k + 1] != 1.0) {
                            if (segmentMaxTemp - segmentMinTemp < precision) {
                                segmentMin[i][index] = segmentMinTemp;
                                segmentMax[i][index] = segmentMaxTemp;
                                length[i][index] = 0;
                                index++;
                                length[i][index] = j;
                                segmentMinTemp = 0.0;
                                segmentMaxTemp = 1.0;
                            }
                            tempmin=segmentMinTemp;
                            tempmax=segmentMaxTemp;
                            segmentMinTemp=tempmin+(tempmax-tempmin)*section[k];
                            segmentMaxTemp=tempmin+(tempmax-tempmin)*section[k+1];

                            if (index <= letters[i].length - 1)
                                length[i][index + 1] = j + 1;
                            else
                                throw new Exception("Word length must be less than 50 characters");
                        }
                    }
                }
            }
            segmentMin[i][index] = segmentMinTemp;
            segmentMax[i][index] = segmentMaxTemp;

            for (int l = 0; l < decimalResult[i].length; l++) {
                if (length[i][l + 1] > 0) {
                    decimalResult[i][l] = 0.5;
                    for (int j = 0; j < binaryIteration; j++) {
                        if (decimalResult[i][l] < segmentMin[i][l])
                            decimalResult[i][l] = decimalResult[i][l] + Math.pow(0.5, j + 1);
                        else if (decimalResult[i][l] > segmentMax[i][l])
                            decimalResult[i][l] = decimalResult[i][l] - Math.pow(0.5, j + 1);
                        else
                            break;
                    }
                    binaryResult[i][l]=convert(decimalResult[i][l]);
                    binaryResult[i][l]=parse(binaryResult[i][l]);
                }
            }
        }
    }
    // conversion decimal fraction to binary
    private String convert(double number) {
        int n = 50;  // precision

        BigDecimal bd = new BigDecimal(number);
        BigDecimal mult = new BigDecimal(2).pow(n);
        bd = bd.multiply(mult);
        BigInteger bi = bd.toBigInteger();
        StringBuilder str = new StringBuilder(bi.toString(2));
        while (str.length() < n+1) {
            str.insert(0, "0");
        }
        str.insert(str.length()-n, ".");
        return str.toString();
    }

    // parse 0 in binary fraction
    private String parse(String in){
        String out;
        char[] pars=in.toCharArray();
        for (int i=pars.length-1;i>0;i--){
            if(pars[i]=='0')
                if(pars[i-1]=='1'){
                    pars[i]='\u0000';
                    break;
                }
                else if (i==2)
                    break;
                else
                    pars[i]='\u0000';
        }
        if(pars[0]=='0' && pars[1]=='.')
        {
            for(int i=2; i< pars.length; i++)
                pars[i-2]=pars[i];
        }
        out=String.valueOf(pars);
        return out;
    }

    //save results to files
    private void saveToFiles(){
        try {
            File fileCode = new File("src\\resources\\CodedString.txt");
            PrintWriter outCode = new PrintWriter(fileCode);

            File fileWord = new File("src\\resources\\WordsLength.txt");
            PrintWriter outWord = new PrintWriter(fileWord);

            for (int i=0; i<binaryResult.length; i++) {
                for (int j = 0; j < binaryResult[i].length; j++) {
                    if(binaryResult[i][j]!=null)
                        outCode.print(binaryResult[i][j]+" ");
                }
                outCode.println();
            }
            outCode.close();

            for (int i=0; i<length.length; i++) {
                for (int j = 0; j < length[i].length; j++) {
                    if(length[i][j]!=0)
                        outWord.print(length[i][j]+" ");
                }
                outWord.println();
            }
            outWord.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
