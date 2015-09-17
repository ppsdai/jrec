package ru.recog.imgproc;

import java.util.*;

import ru.recog.Utils;

/**
 *  
         Sequencer takes a List of Strings that contains output of recognized plates
         and does a search for the most probable, with estimation of a probability
 *
 * @version      
         1.00 16 September 2015  * @author          
         Alexey Denisov  */

public class Sequencer {

	//contains a map of possible sequences
	private Map< String, int[] > mPossibleSequences = new HashMap<String, int[]>();
	private int firstMax = 0;
	private int secondMax = 0;	
	
	// final output method, gives a 6 symbols string, that should contain a number 
	public String doSequence( List<String> plateSymbols) {
		
		String outputStr = "";
		
		// detect numbers
		for( String str: plateSymbols ){
			 stringContainsNumber(str);
		}
		
		// if there is no matches
		if (mPossibleSequences.size() == 0)
		{
			System.out.println(" No matches ");
			return outputStr;
		}
		// if there is 1 match and it was matched more than 3 times
		if (mPossibleSequences.size() == 1){
			for (String str: mPossibleSequences.keySet() )
			{
				int[] arr = mPossibleSequences.get(str);
				if (arr[0]>=2)
				{
					System.out.println(" One matches >2  = " + arr[0]);
					return str;
				}
				else
					return outputStr; // as in no matches
			}
		}
				
		// find first and second max, and key
		String firstMaxKey = findFristAndSecondMax();
		// if one match >3, and other 1 or less, then output
		// we use Russian idea: " One time doesn't count" - Odin raz ne pidaraz.
		if ((firstMax>=3) && (secondMax<=1))
		{
			System.out.println(" >2 Matches, but first >=3, other 1 or less ");
			return firstMaxKey;
		}
		
		//if second best has two or more matches, find best guess on digits
		// and find if some symbols can be guessed too
		int[] misMatches = {0,0,0,0,0,0};
		for (String str: mPossibleSequences.keySet() ){
			if ( str != firstMaxKey)
			{
				int[] arr = mPossibleSequences.get(str);
				for(int i=0; i<6; i++)
					if ( str.charAt(i)!=firstMaxKey.charAt(i))
						misMatches[i]+=arr[i];
			}
		}
		

		StringBuilder sb = new StringBuilder();
		for(int i=0; i<6; i++)
			if (misMatches[i]<=1)
			{
				int[] arr = mPossibleSequences.get(firstMaxKey);
				if ((arr[i]-misMatches[i])>=2)
				sb.append(firstMaxKey.charAt(i) );
				else
					sb.append("*");
			}	
			else
				sb.append("*");
		
		outputStr = sb.toString();
		
		return outputStr;
	}
	
	
	// detects strings that can be numbers and adds them to a Map
	private void stringContainsNumber(String str){
		
		if (str.length() >= 6){
		  // find a string that can be a plate number
		  for (int i = 0; i <= (str.length() - 6); i++){
			if ( Utils.isNumberWith0( str.substring(i,i+6) ))
				addNumberToMap(str.substring(i,i+6));
		  }
		}
				
	}
	
	// adds detected plates to a map, if there is already one then adds +2 to all elements
	// returns number added ( 0 if there is already one)
	private void addNumberToMap( String str){
		
		if (mPossibleSequences.containsKey(str)){
			int[] arr = mPossibleSequences.get(str); 
			for(int i=0; i<6; i++)
				arr[i]++;	
			mPossibleSequences.put(str,arr);
			
		}
		else{
			int[] arr = {1, 1, 1, 1, 1, 1}; 
			mPossibleSequences.put(str, arr);
		
		}
	}
	

	private String findFristAndSecondMax(){
		String outputStr="";
		
		for (String str: mPossibleSequences.keySet() ){
	      int[] arr = mPossibleSequences.get(str);
	      if (arr[0]>secondMax)
	      {
	    	if (arr[0] > firstMax)
	    	{
	    	  secondMax = firstMax ;
	    	  firstMax = arr[0];
	    	  outputStr = str;
	    	}
	    	else 
	    	  secondMax = arr[0];
	      }
	    }
		return outputStr;
	}

}
