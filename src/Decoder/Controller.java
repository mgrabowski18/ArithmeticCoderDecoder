package Decoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Controller {
    private String[] binaryValues;
    private double[] probability;
    private char[] refLetters;

    private double[] decimalValues;
    private char[] decodedLetters;
    private int[] sequenceLength;
    private double[] section;



    public Controller()
    {
        try {
            Model m = new Model();

            Double[] tempProb = m.getProbability();
            Character[] tempLett = m.getLetters();
            probability = new double[tempProb.length];
            refLetters = new char[tempLett.length];
            for (int i=0; i<tempProb.length; i++)
            {
                refLetters[i]=tempLett[i].charValue();
                probability[i]=tempProb[i].doubleValue();
            }
            binaryValues=m.getStringToDecode();
            sequenceLength=m.getWordsLength();
            decimalValues = new double[binaryValues.length];
            for (int i=0; i<decimalValues.length; i++)
                decimalValues[i] = convertBinToDecimal(binaryValues[i]);

            calculateResult();
            saveToFile();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Double convertBinToDecimal(String number) {
        char[] symbol=new char[number.length()];
        int temp;
        double pow;
        double result=0;

        if (number.charAt(0)=='1' && number.charAt(1)=='.'){
            result=1.0;
        }
        else{
            for(int i=0; i<symbol.length; i++)
            {
                symbol[i]=number.charAt(i);
                temp=Character.getNumericValue(symbol[i]);
                pow=Math.pow(2,(-i-1));
                result=result+temp*pow;
            }
        }
        return result;
    }


    private void calculateResult() {
        section = new double[probability.length + 1];
        int size = sequenceLength[sequenceLength.length - 1];
        decodedLetters = new char[size];
        double segmentTempMin = 0.0;
        double segmentTempMax = 1.0;
        int index=0;


        for (int n = 1; n < sequenceLength.length; n++) {
            // implementation of probability estimate
            for (int m = 0; m < section.length; m++) {
                if (m == 0)
                    section[m] = 0;
                else if (m == section.length - 1)
                    section[m] = 1;
                else
                    section[m] = section[m - 1] + probability[m - 1];
            }

            for (int j = 0; j < sequenceLength[n] - sequenceLength[n - 1]; j++) {
                for (int k = 0; k <= refLetters.length; k++) {
                    if (decimalValues[n - 1] <= section[k] && sequenceLength[n] > 0) {
                        decodedLetters[index] = refLetters[k - 1];
                        index++;
                        segmentTempMin = section[k - 1];
                        segmentTempMax = section[k];
                        section[0] = segmentTempMin;
                        section[section.length - 1] = segmentTempMax;
                        for (int l = 1; l < section.length - 1; l++)
                            section[l] = section[l - 1] + probability[l - 1] * (segmentTempMax - segmentTempMin);
                        break;

                    }
                }

                if (sequenceLength[n] == 1)
                    break;
            }
        }
    }


    private void saveToFile()
    {

        try {
            File fileDecode = new File("src\\resources\\DecodedString.txt");
            PrintWriter outDecode = new PrintWriter(fileDecode);

            for (int i=0; i<decodedLetters.length; i++)
            {
                if(decodedLetters[i]!='\u0000')
                outDecode.print(decodedLetters[i]);
            }

            outDecode.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
