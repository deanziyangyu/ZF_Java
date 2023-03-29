public class FizzBuzz {

    public void printFizzBuzz(int k){
        if (k%15==0)
            System.out.println("FizzBuzz");
        else if (k%5==0)
            System.out.println("Buzz");
        else if (k%3==0)
            System.out.println("Fizz");
        else
            System.out.println(k);
        
        k = 10;
        while(k > 0){
            if(k % 2 == 0){
                System.out.println("even number!");
                System.out.println(k);
            }
            else if(k < 0){
                k = -k;
                System.out.println("magnitude of k is " + k);
                k = 0;
            }
            else{
                System.out.println("none of the above");
            }
            k--;
        }
    }

    public void fizzBuzz(int n){
        for (int i=1; i<=n; i++)
            printFizzBuzz(i);
    }
}

