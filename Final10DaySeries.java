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
