package Decoder;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.BigInteger;
import javax.swing.*;
import javax.swing.border.TitledBorder;


/**
 *
 * @author Marcin
 */
public class KoderArytmetyczny2 extends JFrame implements ActionListener{
    // implementacja obiektów interfejsu
    private JFrame ramka;
    private JLayeredPane PanelGlowny;
    private JTextArea pole;
    private JScrollPane przewijanie;
    private JPanel PanelAplikacji;
    private JLabel labelek;
    private JButton zatwierdz;
    // implementacja zmiennych kodera
    private String s1=new String(); // łańcuch wejściowy
    private char[] litery4; // łańcuch wejściowy podzielony na litery
    private final char[] litery1 = {' ','a', 'i', 'o', 'e', 'z', 'n', 'r', 
                                    'w', 's', 't', 'c', 'y', 'k', 'd', 
                                    'p', 'm', 'u', 'j', 'l', 'ł', 'b', 
                                    'g', 'ę', 'h', 'ą', 'ó', 'ż', 'ś', 
                                    'ć', 'f', 'ń', 'q', 'ź', 'v', 'x'}; //tabela liter wd. prawdopodobieństw
    private char[] litery5; // litery po zdekodowaniu
    private double temp;
    private int temp3=0;
    private final double[] prawd={0.151235, 0.075617, 0.069677, 0.065773, 0.065009, 0.047866, 0.046847, 
                                  0.039803, 0.039464, 0.036663, 0.033777, 0.033608, 0.03191, 0.029789, 
                                  0.027582, 0.026564, 0.023763, 0.021217, 0.01935, 0.017822, 0.015446, 
                                  0.012476, 0.012051, 0.00942, 0.009166, 0.008402, 0.007214, 0.007044, 
                                  0.005601, 0.003395, 0.002546, 0.001697, 0.001188, 0.000509, 0.000339, 0.00017}; //tabela prawdopodobieństw
    private double[] przedz; // tabela przedziałów prawdopodobieństw na podstawie tabeli prawd
    private double przedzmin; 
    private double przedzmax;
    private double[] przedzmax3;
    private double[] przedzmin3;
    private int[] dlug2; // tabela przechowująca długości zakodowanych ciągów
    private double temp1=0.0;
    private double temp2=0.0;
    private double[] wynik; // wynik w postaci dziesiętnej
    private String[] wynik2; // wynik w postaci binarnej
    private double[] wynik3; // wynik dziesiętny po zdekodowaniu postaci binarnej
    private char[][] tempchar;
    private double wyrazy=0;
    private double dlugosc;
    private double srednia;
    private double efektywnosc;
    private double entropia;
    private final double precyzja=1.0E-10;
    
    public KoderArytmetyczny2()
    {
        ramka = new JFrame();
        PanelGlowny=new JLayeredPane();
        PanelAplikacji=new JPanel();
        pole = new JTextArea();
        przewijanie = new JScrollPane(pole, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        labelek=new JLabel("<html>Podaj ciąg znaków do zakodowania: </html>");
        zatwierdz=new JButton("<html>Zatwierdź</html>");
        
        // Implementacja Ramki
    	ramka.setPreferredSize(new Dimension(506, 559));
    	ramka.setLayout(new BorderLayout());
    	ramka.setResizable(false);
        
        // Implementacja Panelu Głównego
    	PanelGlowny.setBackground(new Color(0,0,153));
    	PanelGlowny.setPreferredSize(new Dimension(506, 559));
        ramka.add(PanelGlowny, BorderLayout.CENTER);
    	PanelGlowny.setBounds(0,0,500,600);
        
        // Implementacja Panelu Aplikacji
    	PanelAplikacji.setBounds(0, 30, 500, 500);
        PanelAplikacji.setBackground(new Color(0,0,153));
        TitledBorder tytul;
        tytul=(BorderFactory.createTitledBorder(
                null, 
                "Koder Arytmetyczny", 
                TitledBorder.CENTER, 
                TitledBorder.ABOVE_TOP,
                new Font("SansSerif Bold", Font.PLAIN, 15), 
                new Color(255,255,255))		
        );
        PanelAplikacji.setPreferredSize(new Dimension(500,500));
        PanelAplikacji.setLayout(null);
        PanelAplikacji.setBorder(tytul);
        PanelAplikacji.setOpaque(true);
        PanelGlowny.add(PanelAplikacji, new Integer(1), 0);
        
        // Implementacja label'ka
        labelek.setFont(new Font("Sanserif", Font.PLAIN, 15));
        labelek.setForeground(new Color(255,255,255));
        labelek.setHorizontalAlignment(SwingConstants.CENTER);
        this.labelek.setVisible(true);
        labelek.setBounds(30,30, 300, 60);
        PanelAplikacji.add(this.labelek);
        
        // Implementacja pola tekstowego z przewijaniem
        przewijanie.setBounds(60, 70, 380, 200);
        PanelAplikacji.add(przewijanie);
        
        // Implementacja guzika Zatwierdź
        zatwierdz.setBounds(220, 280, 100, 30);
        this.zatwierdz.addActionListener(this);
        PanelAplikacji.add(zatwierdz);
        
        ramka.setTitle("Koder Arytmetyczny");
        ramka.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ramka.pack();
        ramka.setLocationRelativeTo(null);
        ramka.setVisible(true);
    }
// konwersja ułamka dziesiętnego na ułamek binarny    
    private static String convert(double number) {
        int n = 50;  // precyzja
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
// parsowanie 0 w ułamku binarnym
    private static String parse(String we){
        String wy=new String(we);
        char[] pars= new char[wy.length()];
        pars=we.toCharArray();
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
        wy=String.valueOf(pars);
        return wy;
    }
// konwersja ułamka binarnego na ułamek dziesiętny    
    private static Double convert2(String number) {
        char[] symbole=new char[number.length()-2];
        char[] symbole2=new char[number.length()];
        int temp;
        double potega;
        double wynik=0;
        symbole2[0]=number.charAt(0);
        if (symbole2[0]=='1'){
            wynik=1.0;
        }
        else{
            for(int i=2; i<symbole.length; i++)
            {   
                symbole[i]=number.charAt(i);
                temp=Character.getNumericValue(symbole[i]);
                potega=Math.pow(2,(-i+1));
                wynik=wynik+temp*potega;
            }
        }
        return wynik;
    }
            
    public static void main(String[] args) {
       KoderArytmetyczny2 aplikacja = new KoderArytmetyczny2();
    }
    @Override
    public void actionPerformed(ActionEvent ae) {
        s1=pole.getText();  // pobieranie danych wejściowych z pola tekstowego i zapisywanie ich w łańcuchu s1
        // parsowanie danych na małe litery z polskimi znakami i spacjami z pominięciem innych symboli
        s1 = s1.toLowerCase();
        s1 = s1.replaceAll("[\\t\\n\\r]", " ");
        String s3 = s1.replaceAll("[^a-ząęśćńźółż ]", "");
        //zabezpiecznie nadmiaru spacji
        s3=s3.trim().replaceAll(" +", " ");
        // zapisanie do wektora slowa łańcucha s3 z separacją " "
        
        litery4=new char[s3.length()];
        litery4=s3.toCharArray();
 
        // zapisanie tabeli odpowiadającej przedziałom prawdopodobieństw liter polskiego alfabetu
        przedz=new double[prawd.length+1];
        for(int i=0; i<przedz.length; i++)
        {
                if(i==0)
                    przedz[i]=0;
                else if(i==przedz.length-1)
                    przedz[i]=1;
                else
                    przedz[i]=przedz[i-1]+prawd[i-1];
        }
        przedzmin3 = new double[Math.floorDiv(litery4.length,8)+10];
        przedzmax3 = new double[Math.floorDiv(litery4.length,8)+10];
        dlug2 = new int[Math.floorDiv(litery4.length,8)+10];
        wynik=new double[przedzmin3.length];
        wynik2=new String[wynik.length];
        wynik3=new double[wynik.length];
        
        przedzmin=0;
        przedzmax=1;
        int a=0; 
        dlug2[0]=0;
        for (int i=0; i<litery4.length; i++)
        {
            for(int k=0; k<litery1.length; k++)
            {
                if(litery4[i]==litery1[k]) // wybranie pierwszego elementu tablicy i wyszukiwanie odpowiadającej mu litery
                    {
                        if(przedz[k]!=1.0 || przedz[k+1]!=1.0)
                        {
                        //    obliczanie przedziału dla znalezionego znaku
                            if(przedzmax-przedzmin<precyzja)
                            {
                                a=a+1;
                                dlug2[a]=i;
                                i=i-1;
                                przedzmin=0;
                                przedzmax=1;
                                break;
                            }
                        temp1=przedzmin;
                        temp2=przedzmax;
                        przedzmin=temp1+(temp2-temp1)*przedz[k];
                        przedzmax=temp1+(temp2-temp1)*przedz[k+1];
                        przedzmin3[a]=przedzmin;
                        przedzmax3[a]=przedzmax;
                        dlug2[a+1]=i+1;
                        }
                    }
                }
            }
        for (int l=0; l<wynik.length;l++)
        {
            if (dlug2[l]>0 || l==0)
            {
                wynik[l]=0.5;
                for (int j=0; j<50;j++)
                {
                    if(wynik[l]>=przedzmin3[l]){
                        if (wynik[l]<=przedzmax3[l])
                            wynik[l]=wynik[l];
                        else
                            wynik[l]=wynik[l]-Math.pow(0.5, j+1);
                        }
                    else
                        wynik[l]=wynik[l]+Math.pow(0.5, j+1);
                }
            wynik2[l]=convert(wynik[l]);
            wynik3[l]=convert2(wynik2[l]);
            wynik2[l]=parse(wynik2[l]);
            }
                wynik2[a+1]=null;
        }
        
        //dekodowanie
        litery5=new char[litery4.length+1];

        for (int n=1; n<dlug2.length; n++)
        {
            // deklaracja tablicy przedziałów prawdopodobieństw każdego z symboli
            for (int m=0; m<przedz.length; m++)
            {
                if(m==0)
                    przedz[m]=0;
                else if(m==przedz.length-1)
                    przedz[m]=1;
                else
                    przedz[m]=przedz[m-1]+prawd[m-1];
            }
            // wyszukiwanie symboli i obliczanie nowych przedziałów
            for (int j=0; j<dlug2[n]-dlug2[n-1];j++)
            {
                for(int k=0; k<=litery1.length; k++)
                {
                    if (wynik3[n-1]<=przedz[k]&&dlug2[n]>0)
                    {
                        if(litery5[j]=='\u0000')
                        {
                            litery5[j]=litery1[k-1];
                            przedzmin=przedz[k-1];
                            przedzmax=przedz[k];
                            temp1=przedzmin;
                            temp2=przedzmax;
                            temp=przedzmax-przedzmin;
                            przedz[0]=temp1;
                            przedz[przedz.length-1]=temp2;
                            for (int l=1; l<przedz.length-1;l++)
                            {
                                przedz[l]=przedz[l-1]+prawd[l-1]*temp;
                            }
                            break;
                        }  
                        else
                        {
                            litery5[j+dlug2[n-1]]=litery1[k-1];
                            przedzmin=przedz[k-1];
                            przedzmax=przedz[k];
                            temp1=przedzmin;
                            temp2=przedzmax;
                            temp=przedzmax-przedzmin;
                            przedz[0]=temp1;
                            przedz[przedz.length-1]=temp2;
                            for (int l=1; l<przedz.length-1;l++)
                            {
                                przedz[l]=przedz[l-1]+prawd[l-1]*temp;
                            }
                            break; 
                        }
                    }
                }
                if(dlug2[n]==1)
                    break;
            }
        }
        
        tempchar = new char[wynik2.length][];
        wyrazy=0;
        dlugosc=0;
        
        // parsowanie '0.' i obliczanie długości kodu binarnego
        for (int i=0;i<tempchar.length;i++)
        {
            if (wynik2[i]!=null){
                tempchar[i]=wynik2[i].toCharArray();
                for (int j=0; j<tempchar[i].length; j++)
                {   if (tempchar[i][0]=='1' && tempchar[i][1]=='.' && tempchar[i][2]=='0')
                        break;
                    else if(j<tempchar[i].length-2)
                        tempchar[i][j]=tempchar[i][j+2];
                }
                for (int j=0; j<tempchar[i].length; j++)
                {
                     if(tempchar[i][j]!='\u0000')
                        dlugosc=dlugosc+1;
                }
            }
        }
        // przypisanie zmiennej wyrazy dlugosci ciagu wejsciowego
        for (int i=0; i<dlug2.length; i++)
        {
            if(dlug2[i]>0)
                wyrazy=dlug2[i];
        }
        // obliczanie entropii
        for (int i=0;i<prawd.length;i++)
        {
            entropia=entropia+(-prawd[i]*((Math.log(prawd[i]))/(Math.log(2))));
        }
        // srednia dl slowa
        srednia=dlugosc/wyrazy;
        // efektywnosc
        efektywnosc=(entropia/srednia)*100;
        for (int i=0;i<wynik2.length;i++)
        {
                if(tempchar[i]==null)
                    continue;
                wynik2[i]=String.valueOf(tempchar[i]);
        }
        //prezetacja wyniku
        for (int i=0; i<dlug2.length; i++){
            if (wynik2[i]!=null){
                //System.out.print("Wynik dla "+(i+1)+" ciągu: ");
                System.out.print(wynik2[i]+"\t");
                //System.out.print(" "+wynik[i]);
                //System.out.print(" Ciąg po zdekodowaniu: ");
                if (dlug2[2]==0 && dlug2[1]==1){
                    System.out.print(litery5[0]);
                }
                else if(dlug2[2]==0 && dlug2[1]>1){
                    for(int k=dlug2[i]; k<dlug2[i+1] ;k++)
                        System.out.print(litery5[k]);
                }
                else if(dlug2[i+2]==0 && i>0){
                    for(int k=dlug2[i]; k<=dlug2[i+1];k++)
                        System.out.print(litery5[k]);
                }
                else
                    for(int k=dlug2[i]; k<dlug2[i+1] ;k++)
                        System.out.print(litery5[k]);
                System.out.print("\n");
                }
        }        
        
        System.out.println("Średnia długość zakodowanego znaku: "+srednia);
        System.out.println("Entropia wynosi: "+entropia);
        System.out.println("Efektywność kodu wynosi: "+efektywnosc+"%");
    }
}