//synechron java buddy list
//reverse a String
public class Main
{
    public static void reverseString(String str){
        char[] arr = str.toCharArray();
        // String reversed = "";
        // for(int i = arr.length -1; i>=0 ; i--){
        //     reversed = reversed + charr;
        // }
        // System.out.println(reversed);
        
        int start = 0;
        int end = arr.length - 1;
        
        while(start < end){
            char temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;
            start++;
            end--;
        }
        String result = new String(arr);
        System.out.println(result);
    }
    
	public static void main(String[] args) {
	    String str =  "hello";
		reverseString(str);
	}
}
//synechron 
//Given a list of Integers, find the maximum value element present in 
//it using Stream functions
import java.util.*;

public class Main
{
	public static void main(String[] args) {
	    List<Integer> list = Arrays.asList(5,2,8,1);
	    int max = list.stream().max((a,b) -> a.compareTo(b)).get();
	    //list.stream().max(Integer::compare).get();
	    //get is used because max returns optional
	    
	    //or
	    //list.stream().max(Integer::compareTo).orElse(0); 
	    
	    System.out.println("max: "+max);
	    
	    int min = list.stream().min((a, b) -> a.compareTo(b)).get();
	    System.out.println("max: "+min);
	}
}
