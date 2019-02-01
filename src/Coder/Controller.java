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

    private char[] letters;   //words divided into letters
    private double[] section;   //probability estimate
    private double segmentTempMin;  //minimal segment value for coding string
    private double segmentTempMax;  //maximal segment value for coding string
    private double[] segmentMin;  //table with minimal segment values
    private double[] segmentMax;  //table with maximal segment values
    private int[] length; //table with coded string length

    private double[] decimalResult;
    private String[] binaryResult;

    private final double precision=1.0E-10;
    private final int binaryIteration=50;

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
        letters = new char[input.length()];
        letters = input.toCharArray();

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

    public void calculateResult() {
        decimalResult = new double[letters.length];
        binaryResult = new String[letters.length];
        segmentMin = new double[letters.length];
        segmentMax = new double[letters.length];
        length = new int[letters.length];


        //result calculating
        segmentTempMin = 0.0;
        segmentTempMax = 1.0;
        double tempmin;
        double tempmax;
        int index = 0;

        //min and max sections calculations
        for (int i = 0; i < letters.length; i++) {
            for (int j = 0; j < refLetters.length; j++) {
                if (letters[i] == refLetters[j]) {
                    if (section[j] != 1.0 || section[j + 1] != 1.0) {
                        if (segmentTempMax - segmentTempMin < precision) {
                            index++;
                            length[index]=i;
                            i=i-1;
                            segmentTempMin =0.0;
                            segmentTempMax =1.0;
                            break;
                        }
                        tempmin= segmentTempMin;
                        tempmax= segmentTempMax;
                        segmentTempMin =tempmin+(tempmax-tempmin)*section[j];
                        segmentTempMax =tempmin+(tempmax-tempmin)*section[j+1];
                        segmentMin[index]= segmentTempMin;
                        segmentMax[index]= segmentTempMax;
                        length[index+1]=i+1;
                    }
                }
            }
        }

        //calculating binary point of section
        for (int l=0; l<decimalResult.length;l++)
        {
            if (length[l] > 0 || l==0) {
                decimalResult[l]=0.5;
                for(int j=0; j<binaryIteration;j++)
                {
                    if (decimalResult[l] < segmentMin[l])
                        decimalResult[l] = decimalResult[l] + Math.pow(0.5, j + 1);
                    else if (decimalResult[l] > segmentMax[l])
                        decimalResult[l] = decimalResult[l] - Math.pow(0.5, j + 1);
                    else
                        break;
                }
                binaryResult[l]=convert(decimalResult[l]);
                binaryResult[l]=parse(binaryResult[l]);
            }
            binaryResult[index+1]=null;
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

            for (int i=0; i<binaryResult.length; i++) {
                if(binaryResult[i]!=null)
                    outCode.println(binaryResult[i]);
            }
            outCode.close();

            File fileWord = new File("src\\resources\\WordsLength.txt");
            PrintWriter outWord = new PrintWriter(fileWord);

            for (int i=0; i<length.length; i++) {
                if(length[i]!=0 || i==0)
                    outWord.println(length[i]);
            }
            outWord.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
