
public class Circle {

    private int id;
    private static int circleCount = 0;
    private final double PI = 3.1415;
    public int radius;

    public Circle(int radius){
        circleCount++;
        this.id = circleCount;
        this.radius = radius;
    }

    public static void main(String[] args){
        Circle circle = new Circle(5);
        int actualArea = circle.area(false); // should be 78
        int integerArea = circle.area(true); // should be 81
    }

    public int bar1(int x){
        return -x*bar();
    }

    public int bar(){
        return getCircleCount();
    }

    public int foo(){
        getCircleCount();
        return bar();
    }

    public int area_direct(){
        foo();
        //bar();
        return (int)PI*radius*radius;
    }

    // A.K.A Gauss Circle Problem
    public int area_gauss(){
        /*if(getCircleCount() > 5){
            return 10;
        }*/
        int result = 0;
        for(int x=-radius; x<=radius; x++){
            for(int y=-radius; y<=radius; y++){
                if(x*x + y*y <= radius*radius)
                    result++;
            }
        }
        return result >= 10 ? area_direct() : result;
    }

    public int area(boolean integerArea){
        int a = 13;
        a = foo();
        while(a > 0){
            System.out.println(a);
            a--;
        }
        //System.out.println(a);
        /*for(int i = a+1; i < 22; i++){
            a = a - 1;
        }
        if(a == 4){
            a = a - 1;
        }*/
        int b = a;
        a = a + b;
        b = b + a;
        int c = a*b;
        int d = c/2;
        //System.out.println(d);
        if(a > b+5){
            b = bar1(3);
            a += 3*b;
        }
        else if(a > b+4){
            c = d*b;
            a = d;
        }
        else if(a > b+1){
            a = d*4;
            b = a+b;
        }
        else if(a > b+2){
            a = (b+d)/5;
            b=d/2;
        }
        else if(a > b+1){
            a = b+3;
        }
        /*else{
            b = a*4;
        }*/
        a -= b;
        return a;
    }


    public static int getCircleCount(){
        return circleCount;
    }

}

