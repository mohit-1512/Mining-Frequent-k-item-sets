import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class PatternApriori {

	static String tranFilePath;
	static int min_sup;
	static int k_plus;

	public static void main(String[] args) {

		if(args.length<4) {
			System.out.println("Invalid number of commandline arguments");
			System.out.println("Expected");
			System.out.println("java -jar apriori.jar min_sup k input_transaction_file_path output_file_path");
			System.exit(0);
		}
		tranFilePath=args[2];
		min_sup=Integer.parseInt(args[0]);
		k_plus=Integer.parseInt(args[1]);
		String outputFilePath=args[3];
		long startTime = System.nanoTime();
		Map<String,BitSet> indivItemBitsetTranDB= new HashMap<>();
		int currTransactionCounter=0;
		try (BufferedReader br = new BufferedReader(new FileReader(tranFilePath))) {
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				String[] currentTransactions=sCurrentLine.split(" ");
				if(currentTransactions.length>=k_plus){
					for (String currItemInTrans : currentTransactions) {
						if(indivItemBitsetTranDB.containsKey(currItemInTrans)) {
							BitSet currItemBits=indivItemBitsetTranDB.get(currItemInTrans);
							currItemBits.set(currTransactionCounter);
						}else {
							BitSet currItemBits=new BitSet();
							currItemBits.set(currTransactionCounter);
							indivItemBitsetTranDB.put(currItemInTrans, currItemBits);
						}
					}
				}
				currTransactionCounter++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TreeSet<ItemSet> freqItemSet;
		freqItemSet=getFirstFreqSet(indivItemBitsetTranDB);


		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(new File(outputFilePath));
			if(k_plus==1) {
				for (ItemSet itemSet : freqItemSet) {
					fileWriter.write(""+itemSet);
				}
			}
			int j=1;
			while(!freqItemSet.isEmpty()) {
				j++;
				freqItemSet=candGenPruneGetSupport(freqItemSet,indivItemBitsetTranDB,currTransactionCounter);
				if(j>=k_plus) {
					for (ItemSet itemSet : freqItemSet) {
						fileWriter.write(""+itemSet);
					}
				}
			}
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long endTime = System.nanoTime();
		long elapsedTimeInMillis = TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
		System.out.println("Total elapsed time: " +(elapsedTimeInMillis / 1000)  / 60  + " min "+ (elapsedTimeInMillis / 1000) % 60 +" sec");
	}
	private static TreeSet<ItemSet> candGenPruneGetSupport(TreeSet<ItemSet> freqItemSet, Map<String, BitSet> indivItemBitsetTranDB, int totTrans) {
		TreeSet<ItemSet> freqItemSetCurrIter=new TreeSet<>();
		Iterator<ItemSet> it1=freqItemSet.iterator();
		while (it1.hasNext()) {
			ItemSet itemSet1 = (ItemSet)it1.next();
			Iterator<ItemSet> tailIterator=freqItemSet.tailSet(itemSet1,false).iterator();
			while (tailIterator.hasNext()) {
				ItemSet itemSet2 = (ItemSet) tailIterator.next();
				if(validCandidate(itemSet1,itemSet2)) {
					LinkedList<String> outputSet=new LinkedList<>();
					outputSet.addAll(itemSet1.set);
					outputSet.add(itemSet2.set.get(itemSet2.set.size()-1));
					int currSup=getSupportCount(outputSet,indivItemBitsetTranDB,totTrans);
					if(currSup>=min_sup && checkForPruning(outputSet,freqItemSet)) {
						ItemSet outputItemSet=new ItemSet(outputSet);
						outputItemSet.support=currSup;
						freqItemSetCurrIter.add(outputItemSet);
					}
				}
			}
		}
		return freqItemSetCurrIter;
	}
	private static boolean checkForPruning(LinkedList<String> outputSet, TreeSet<ItemSet> freqItemSet) {

		for (int i = 0; i < outputSet.size(); i++) {
			String remItmStr=outputSet.remove(i);
			ItemSet outputItemSet=new ItemSet(outputSet);
			if(!freqItemSet.contains(outputItemSet)) {
				return false;
			}
			outputSet.add(i,remItmStr);
		}
		return true;
	}
	private static int getSupportCount(LinkedList<String> outputSet, Map<String, BitSet> indivItemBitsetTranDB, int totTrans) {

		BitSet currBits=new BitSet(totTrans);

		currBits.set(0, currBits.size(),true);
		for (String itemStr : outputSet) {
			currBits.and(indivItemBitsetTranDB.get(itemStr));
		}
		return currBits.cardinality();
	}
	private static boolean validCandidate(ItemSet itemSet1, ItemSet itemSet2) {
		List<String> set1=itemSet1.set;
		List<String> set2=itemSet2.set;

		for(int i=0;i<set1.size()-1;i++) {
			if(!set1.get(i).equals(set2.get(i))) {
				return false;
			}
		}
		if(set1.get(set1.size()-1).compareTo(set2.get(set2.size()-1)) >= 0) {
			//validate Once
			return false;
		}
		return true;
	}
	private static TreeSet<ItemSet> getFirstFreqSet(Map<String, BitSet> indivItemBitsetTranDB) {
		TreeSet<ItemSet> firstFreqSet = new TreeSet<>();

		for(String key : indivItemBitsetTranDB.keySet()) {
			BitSet currBits = indivItemBitsetTranDB.get(key);
			int currSup=currBits.cardinality();
			if(currSup>=min_sup) {
				ItemSet currItemSet=new ItemSet(key);
				currItemSet.support=currSup;
				firstFreqSet.add(currItemSet);
			}
		}
		return firstFreqSet;
	}

}
