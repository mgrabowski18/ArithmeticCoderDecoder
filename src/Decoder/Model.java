package Decoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

public class Model {
    private Map<Character, Double> dictionary;  //map for letter and her probability
    private String[] stringToDecode;    //input string to code
    private int counter = 0;

    public Model()
    {
        readCodedString();
    }

    private void readCodedString()
    {
        try {
            File file = new File("src\\resources\\CodedString.txt");
            Scanner in = new Scanner(file);
            while(in.hasNext())
                counter++;
            stringToDecode = new String[counter];
            counter=0;
            while(in.hasNext())
            {
                stringToDecode[counter]=in.nextLine();
                System.out.println(stringToDecode[counter]);
                counter++;
            }
            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
