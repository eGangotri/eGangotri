import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by user on 11/12/15.
 */
public class LockBreaker {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int m=0, n=0;
        ArrayList<String> grid = new ArrayList<String>();
        String line;
        while( (line = scanner.nextLine()) != null){
            System.out.println("---" + line);

       /*     if(m==0){
                m = scanner.nextInt();
            }
            else if(n==0){
                n = scanner.nextInt();
            }*/
           /* if(true){
                String read = scanner.next();
                System.out.println( m + " - " + n + "->" + read + grid.size());
                grid.add(read);
            }*/
        }
        for(String x: grid){
            System.out.println("---" + x);
        }
    }
}
