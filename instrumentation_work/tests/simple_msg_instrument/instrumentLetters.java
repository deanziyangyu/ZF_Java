import java.io.*;

public class instrumentLetters {
  public static String result;

  public static void endProgram(String s) {
    System.out.println(result);
    System.exit(0);
  }

  public static void A0() {
    result += "A0 ";
    endProgram("");
  }
  
  public static void A1() {
    result += "A1 ";
    B1(1);
  }

  public static void A2() {
    result += "A2 ";
    B2(2);
  }

  public static void A3() {
    result += "A3 ";
    B3(3);
  }

  public static void B0(int a) {
    result += "B0 ";
    C0(0, "0");
  }

  public static void B1(int a) {
    result += "B1 ";
    C1(1, "1");
  }

  public static void B2(int a) {
    result += "B2 ";
    C2(2, "2");
  }

  public static void B3(int a) {
    result += "B3 ";
    C3(3, "3");
  }

  public static void C0(int a, String b) {
    result += "C0 ";
    endProgram("");
  }

  public static void C1(int a, String b) {
    result += "C1 ";
    endProgram("");
  }

  public static void C2(int a, String b) {
    result += "C2 ";
    endProgram("");
  }

  public static void C3(int a, String b) {
    result += "C3 ";
    endProgram("");
  }

  public static void main(String[] args) {
    result = "";
    A0();
  }

}
