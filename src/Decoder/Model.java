package Decoder;

import java.io.*;
import java.util.*;

public class Model {
    private Map<Character, Double> dictionary;  //map for letter and her probability
    private String[] stringToDecode;    //binary code to decode
    private int[] wordsLength;    //binary words lengths
    private String input= new String();


    public Model() throws  Exception
    {
        dictionary = new HashMap<>();

        readDictionary();
        readCodedString();
        readWordsLength();
        validateProbability();
    }

    private void readDictionary()
    {
        //Read data from dictionary file
        try {
            File file = new File("src\\resources\\Dictionary.txt");
            Scanner in = new Scanner(file);
            String str;
            Character c=null;
            Double num=null;
            while (in.hasNext()) {
                str=in.nextLine();
                for (int i=0; i<str.length(); i++)
                {
                    //skip Byte Order Mark
                    if(str.charAt(0)!='\uFEFF')
                        c=str.charAt(0);
                    else
                        c=str.charAt(1);
                    //get double after tab char
                    if(str.charAt(i)=='\u0009') {
                        num = Double.parseDouble(str.substring(i + 1, str.length()));
                        break;
                    }
                }
                dictionary.put(c, num);
            }
            in.close();

            dictionary=sortHashMapByValues((HashMap<Character, Double>) dictionary);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //sorting map by probability values
    private LinkedHashMap<Character, Double> sortHashMapByValues(HashMap<Character, Double> passedMap)
    {
        List<Character> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Double> mapValues = new ArrayList<>(passedMap.values());

        Collections.sort(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapValues);
        Collections.reverse(mapKeys);

        LinkedHashMap<Character, Double> sortedMap = new LinkedHashMap<>();

        Iterator<Double> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Double val = valueIt.next();
            Iterator<Character> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Character key = keyIt.next();
                Double comp1 = passedMap.get(key);
                Double comp2 = val;

                if (comp1.equals(comp2)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    //Validating probability
    private void validateProbability() throws Exception
    {
        List<Double> dictionaryValues = new ArrayList<>(dictionary.values());
        Iterator<Double> dictionaryIterator = dictionaryValues.iterator();
        double valid =0.0;
        double temp;

        while(dictionaryIterator.hasNext()) {
            temp = dictionaryIterator.next();
            if(temp < 0.0)
                throw new Exception("Probability can't be less than 0!");
            valid += temp;
        }

        if (valid>1.0)
            throw new Exception("Probability higher than 1.0");

    }

    private void readCodedString()
    {
        try {
            File file = new File("src\\resources\\CodedString.txt");
            Scanner in = new Scanner(file);
            input= new String();

            //put coded strings to stringToCode[]
            while(in.hasNext())
            {
                input+=in.nextLine()+" ";
            }
            stringToDecode=input.split(" ");

            //parse other characters than 0,1
            for (int i=0; i<stringToDecode.length; i++)
                stringToDecode[i] = stringToDecode[i].replaceAll("[^0-1.]", "");
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void readWordsLength()
    {
        try {
            File file = new File("src\\resources\\WordsLength.txt");
            Scanner in = new Scanner(file);
            wordsLength = new int[stringToDecode.length+1];
            int counter=0;

            //put coded sequence length to wordsLength[]
            while(in.hasNextInt())
            {
                wordsLength[counter]=in.nextInt();
                counter++;
            }
            in.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public Character[] getLetters()
    {
        List<Character> charList = new ArrayList<>(dictionary.keySet());
        Character[] chars = new Character[charList.size()];
        chars=charList.toArray(chars);
        return chars;
    }

    public Double[] getProbability()
    {
        List<Double> doubleList = new ArrayList<>(dictionary.values());
        Double[] prob = new Double[doubleList.size()];
        prob=doubleList.toArray(prob);
        return prob;
    }


    public String[] getStringToDecode()
    {
        return stringToDecode;
    }

    public int[] getWordsLength() {
        return wordsLength;
    }
}
