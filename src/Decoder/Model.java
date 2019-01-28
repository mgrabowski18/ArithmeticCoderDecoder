package Decoder;

import java.util.Map;

public class Model {
    private Map<Character, Double> dictionary;  //map for letter and her probability
    private String[][] stringToDecode;    //input string to code

    public Model()
    {
        stringToDecode = new String() [][];
    }
}
