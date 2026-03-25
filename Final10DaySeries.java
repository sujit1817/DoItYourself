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

//Remove Duplicates from Sorted Array : Use 2 Pointer 
import java.util.*;
public class Main {

    public static void removeDuplicates(int[] nums) {
        // Edge case
        if (nums == null || nums.length == 0) {
            System.out.println("empty array");
        }

       //
       int i = 0;
       for(int j = 1; j < nums.length; j++){
           if(nums[i]!=nums[j]){
               i++;
               nums[i]=nums[j];
           }
           
           
       }
       for(int k = 0; k<=i;k++){
            System.out.print(" "+nums[k]);
       }
      // System.out.println("Unique count: " +Arrays.toString(nums));
    }

    public static void main(String[] args) {
        int[] nums = {1, 1, 2, 2, 3};
        removeDuplicates(nums);

        
    }
}


//deque implementation
import java.util.*;

public class Main
{
	public static void main(String[] args) {
		Deque<Integer> deque = new LinkedList<>();
		deque.addFirst(1);//[1]
		deque.addFirst(2);//[2,1]
		deque.addLast(3);//[2,1,3]
		deque.addLast(4);//[2,1,3,4]
		System.out.println(deque+" ");
		
		deque.removeFirst();// first element will be removed
		System.out.println(deque+" ");
		deque.removeLast();//Last element will be removed
		
		System.out.println(deque+" ");
		
		System.out.println("first el = "+deque.getFirst());
		System.out.println("last el = "+deque.getLast());
		
	}
}

//Binary format of 4 + count number of zeroes
import java.util.*;

public class Main
{
	public static void main(String[] args) {
	  int num = 4;
	  String binary = Integer.toBinaryString(num);
	  System.out.println("Binary: "+binary);
	  
	  int zeroCount = 0;
	  int oneCount = 0;
	  
	  for(char c : binary.toCharArray()){
	      if(c == '0'){
	          zeroCount++;
	      }else {
	      oneCount++;
	      }
	  }
	  
	  System.out.println("zeroCount : "+zeroCount);
	  System.out.println("oneCount : "+oneCount);
	}
}

//Palindrome of Given String
import java.util.*;

public class Main
{
	public static void main(String[] args) {
		String str = "madam";
		String reversed = new StringBuilder(str).reverse().toString();

		if(str.equals(reversed)) {
			System.out.println("Palindrome");
		} else {
			System.out.println("not Palindrome");
		}
	}
}

//static and instance counter
//static class level
//instance - per object level
//instance in heap
//static in method area
import java.util.*;

public class Main
{
	int instanceCounter = 0; //per Object
	static int staticCount = 0; //shared across all Objects

	Main() {
	//	instanceCounter++;
		staticCount++;
	}
	
	public static int getCount(){
	    return staticCount;
	}
	
	public int getInstanceCount(){
	    return instanceCounter;
	}
	
	public static void main(String[] args) {
		new Main();
		new Main();
    Main m = new Main();

    System.out.println(getCount()); 
    System.out.println(m.getInstanceCount()); 
	}
}



//singly : last node
class Node{
	int data;
	Node next = null;
}


//Circular : last node points back to head
Node head = new Node(1);
Node second = new Node(2);
head.next = second;
second.next = head;//circular


nth highest sal

select salary 
from (
select salary,
		  DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
	FROM employees
	) ranked
where rnk = N; --replace N with desired rank (2, 3, etc.)

//Check if array is sorted (no inbuilt functions)
import java.util.*;

public class Main
{
	public static boolean isSorted(int[] arr) {
		if(arr == null || arr.length <= 1) {
			return true;
		}
		for(int i = 0; i<arr.length-1; i++) {
			if(arr[i]>arr[i+1]) {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		int[] a = {1, 3, 5, 7};    // isSorted  true
		int[] b = {1, 5, 3, 7};    // isSorted  false
		System.out.println(isSorted(a));
		System.out.println(isSorted(b));
	}
}


write a java program that takes list of integers as input and returns the squares of all the odd numbers in the list using stream api
import java.util.Arrays;
import java.util.List;

public class OddSquares {
    public static void main(String[] args) {
        
        List<Integer> nums = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        nums.stream()
            .filter(n -> n % 2 != 0)   // filter odd numbers
            .map(n -> n * n)           // square them
            .forEach(System.out::println); // print result
    }
}
nums.stream().filter(n -> n%2!=0).map(n -> n*n).forEach(System.out::println);



