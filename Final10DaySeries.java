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


//Singleton ensures that only one instance of a class is created and provides a global access point to that instance.
//1. Logging
//2. Configuration classes
//3. Database connections

//Create singleton
//Eager
public class EagerSingleton{
	
	private static final EagerSingleton instance = new EagerSingleton();
	
	private EagerSingleton(){
	}
	
	public static EagerSingleton getInstance(){
		return instance;
	}
}

//Lazy
public class LazySingleton{
	private static LazySingleton instance;
	
	private LazySingleton(){		
	}
	
	public static LazySingleton getInstance(){
		if(instance==null){
			 instance = new LazySingleton();
		}
		return instance;
	}
}

//Thread safe
public class ThreadSafeSingleton{
	private static volatile ThreadSafeSingleton instance;
	
	private ThreadSafeSingleton(){}
	
	public static synchronized ThreadSafeSingleton getInstance() {
        if (instance == null) {
            instance = new ThreadSafeSingleton();
        }
        return instance;
    }
}

//Double checked
public class DoubleCheckedLocking{
	private static volatile DoubleCheckedLocking instance;
	
	private DoubleCheckedLocking(){}
	
	public static DoubleCheckedLocking getInstance() {
        if (instance == null) {
            synchronized (DoubleCheckedLocking.class) {
                if (instance == null) {
                    instance = new DoubleCheckedLocking();
                }
            }
        }
        return instance;
    }
}

