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
        //int actualArea = circle.area(false); // should be 78
        //int integerArea = circle.area(true); // should be 81
        int[][] M = new int[100][100];
        for(int i = 0; i < 100; i++){
            for(int j = 0; j < 100; j++){
                M[i][j] = (i*j+(i+1)/(j+1)+(i+1)/(j-1))%(2*i-j);
            }
        }
        circle.setup(M);
    }

    public int setup(int[][] M){
        if(M[M.length-1][M[0].length-1] < 0.5){
            return compute_value(M);
        }
        else{
            return hash(M);
        }
    }

    public int compute_value(int[][] M){
        int gauss_area;
        int actual_area;
        for(int i = 0; i < M.length; i++){
            for(int j = 0; j < M[0].length; j++){
                gauss_area = ((M[i][j]+23)*100)%20;
                actual_area = (M[i][j]*23)%20;
                M[i][j] = gauss_area > actual_area ? gauss_area : actual_area;
            }
        }
        return hash(M);
    }

    public int hash(int[][] M){
        for(int i = 0; i < M.length; i++){
            for(int j = 0; j < M[0].length; j++){
                M[i][j] = (int)Math.pow(M[i][j], 307) % 137;
            }
        }
        if(Math.random() < 0.5){
            return send_untransformed(M);
        }
        else{
           return apply_transform_1(M);
        }
    }

    public int apply_transform_1(int[][] M){
        if(M[0][0] < 0){
            return check_transform_1_valid(M);
        }
        else{
            return send_transform_result(M);
        }
    }

    public int check_transform_1_valid(int[][] M){
        if(M[0][1] < 0){
            return recompute_transform_1(M);
        }
        else{
            return send_transform_result(M);
        }
    }

    public int recompute_transform_1(int[][] M){
        return send_transform_result(M);
    }

    public int send_transform_result(int[][] M){
        return 0;
    }

    public int send_untransformed(int[][] M){
        return 0;
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
        else{
            b = a*4;
        }
        a -= b;
        return a;
    }


    public static int getCircleCount(){
        return circleCount;
    }

}


