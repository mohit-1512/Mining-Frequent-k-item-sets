import java.awt.List;
import java.util.Iterator;
import java.util.LinkedList;

public class ItemSet implements Comparable<ItemSet>{
	LinkedList<String> set;
	int support;
	public ItemSet() {
		super();
	}
	public ItemSet(String str) {
		this.set = new LinkedList<String>();
		this.set.add(str);
		support=0;
	}
	public ItemSet(LinkedList<String> set) {
		this.set = new LinkedList<String>();
		this.set=set;
		support=0;
	}	
	@Override
	public String toString() {
		String retStr="";
		for (String stringInSet : set) {
			retStr+=stringInSet+" ";
		}
		retStr+="("+support+")\n";
		return retStr;
	}
	@Override
	public int compareTo(ItemSet o) {
		for(int i=0;i<this.set.size();i++) {
			if(!this.set.get(i).equals(o.set.get(i))) {
				return this.set.get(i).compareTo(o.set.get(i));
			}
		}
		return 0;
	}

}
