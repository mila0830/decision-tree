import java.io.Serializable;
import java.util.ArrayList;
import java.text.*;
import java.lang.Math;

public class DecisionTree implements Serializable {

	DTNode rootDTNode;
	int minSizeDatalist; //minimum number of datapoints that should be present in the dataset so as to initiate a split
	
	// Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
	public static final long serialVersionUID = 343L;
	
	public DecisionTree(ArrayList<Datum> datalist , int min) {
		minSizeDatalist = min;
		rootDTNode = (new DTNode()).fillDTNode(datalist);
	}

	class DTNode implements Serializable{
		//Mention the serialVersionUID explicitly in order to avoid getting errors while deserializing.
		public static final long serialVersionUID = 438L;
		boolean leaf;
		int label = -1;      // only defined if node is a leaf
		int attribute; // only defined if node is not a leaf
		double threshold;  // only defined if node is not a leaf

		DTNode left, right; //the left and right child of a particular node. (null if leaf)

		DTNode() {
			leaf = true;
			threshold = Double.MAX_VALUE;
		}

		
		// this method takes in a datalist (ArrayList of type datum). It returns the calling DTNode object 
		// as the root of a decision tree trained using the datapoints present in the datalist variable and minSizeDatalist.
		// Also, KEEP IN MIND that the left and right child of the node correspond to "less than" and "greater than or equal to" threshold
		DTNode fillDTNode(ArrayList<Datum> datalist) {

			//define variables for finding the best split
			double bestAverageEntropy= Double.POSITIVE_INFINITY;
			int bestAttribute= -1;
			double bestThreshold=-1; 
			
			//set a variable to the DTNode that will be returned
			DTNode finalNode= new DTNode();
			
			//check if the labeled data set has at least k data items
			if (datalist.size() >= minSizeDatalist) {
				
				//check if all the data items have the same label
				if (calcEntropy(datalist)== 0) {
					//creates a leaf node and returns it
					finalNode.leaf=true;
					finalNode.label= datalist.get(0).y;
					return finalNode;
				}
				else {
					//find the best "attribute" test question
					
					//set a variable for the length of attributes of each data point
					int numOfAttributes= datalist.get(0).x.length;
					//iterate for each attribute in x
					for (int i=0; i< numOfAttributes; i++) {
						//iterate through each data point in the data list
						for (Datum data: datalist) {
							//compute the split
							
							//set two variables equal to two array lists of Datum
							ArrayList<Datum> dSet1= new ArrayList<Datum>();
							ArrayList<Datum> dSet2= new ArrayList<Datum>();
							
							//iterate through each data point in the data list
							for (Datum dataPoint : datalist) {
								//check if a certain attribute is greater than all the other attributes at a certain index
								if(dataPoint.x[i] < data.x[i]) {
									//add to one array list
									dSet1.add(dataPoint);
								}
								else {
									//if smaller add to the other array list
									dSet2.add(dataPoint);
								}
							}
							//set two variables to the size of both array lists and the size of the data list
							double dSet1Size= dSet1.size();
							double dSet2Size=  dSet2.size();
							double datalistSize=  datalist.size();
							//calculate the current average entropy based on the split
							double currentAverageEntropy= (((dSet1Size/datalistSize) * calcEntropy(dSet1)) + ((dSet2Size/datalistSize) * calcEntropy(dSet2)));
							//check if the best average entropy is greater than the current average entropy
							if (bestAverageEntropy > currentAverageEntropy) {
								//assign the best average entropy to the current average entropy
								bestAverageEntropy = currentAverageEntropy;
								//make the best attribute equal to the index of the best attribute in x
								bestAttribute=i;
								//assign the threshold equal to the value at the index
								bestThreshold=data.x[i];
							}
						}
						
					}
					//account for if the minimum average entropy is equal to the entropy of the data set
					if (bestAverageEntropy== calcEntropy(datalist)) {
						//node is a leaf with a label of the majority of the labels in the data set
						finalNode.leaf=true;
						finalNode.label=findMajority(datalist);
						return finalNode;
						
					}
					//store the attribute test in that node (the best attribute and threshold)
					finalNode.attribute= bestAttribute;
					finalNode.threshold= bestThreshold;
					finalNode.leaf=false;
					
					//split the set of data items into two subsets according to the test question
					
					//make two subsets to hold data items
					ArrayList <Datum> d1= new ArrayList<Datum>();
					ArrayList <Datum> d2= new ArrayList<Datum>();
					//set a variable equal to the size of the data list
					int datalistSize= datalist.size();
					//iterate through the whole data list
					for (int i=0; i< datalistSize; i++) {
						//check if at a certain datum the attribute at the new node is less than the new node threshold (test question)
						if (datalist.get(i).x[(finalNode.attribute)] < finalNode.threshold) {
							//if less add to the first array list of datum
							d1.add(datalist.get(i));
						}
						else {
							//if less add to the second array list of datum
							d2.add(datalist.get(i));
						}
					}
					//recursively call fillDTNode for both the left and right child
					finalNode.left= fillDTNode(d1);
					finalNode.right=fillDTNode(d2);
					//return the final node
					return finalNode;
				}
			}
			else {
				//create a leaf node with label equal to the majority of labels and return
				finalNode.leaf=true;
				finalNode.label=findMajority(datalist);
				return finalNode;
			}
			
			
		}



		// This is a helper method. Given a datalist, this method returns the label that has the most
		// occurrences. In case of a tie it returns the label with the smallest value (numerically) involved in the tie.
		int findMajority(ArrayList<Datum> datalist) {
			
			int [] votes = new int[2];

			//loop through the data and count the occurrences of datapoints of each label
			for (Datum data : datalist)
			{
				votes[data.y]+=1;
			}
			
			if (votes[0] >= votes[1])
				return 0;
			else
				return 1;
		}




		// This method takes in a datapoint (excluding the label) in the form of an array of type double (Datum.x) and
		// returns its corresponding label, as determined by the decision tree
		int classifyAtNode(double[] xQuery) {
			
			//check if the node is a leaf
			if (this.leaf == true) {
				//return the label of the node
				return this.label;
			}
			else {
				//test the data item using the node's attribute (test)
				if (xQuery[this.attribute]<this.threshold) {
					//if the data point is less than threshold return the recursive call on the left child
					return this.left.classifyAtNode(xQuery);
				}
				else {
					//if data point is greater than the threshold return the recursive call on right child
					return this.right.classifyAtNode(xQuery);
				}	
			}
		}


		//given another DTNode object, this method checks if the tree rooted at the calling DTNode is equal to the tree rooted
		//at DTNode object passed as the parameter
		public boolean equals(Object dt2)
		{

			//check if object inputed is even an instance of DTNode
			if (dt2 instanceof DTNode== false) {
				return false;
			}
			//set up two boolean variables
			boolean leftTree= false;
			boolean rightTree= false;
			
			//check if both the objects are leaves
			if (this.leaf==true && ((DTNode) dt2).leaf == true) {
				//check if their labels are the same therefore the leaves are the same
				if (this.label == ((DTNode) dt2).label) {
					return true;
				}
				else {
					return false;
				}
			}
			//check if both the attribute and thresholds are the same for the two objects
			else if (this.attribute == ((DTNode) dt2).attribute && this.threshold == ((DTNode) dt2).threshold){
					//check if both the left for the two nodes are not equal to null 
					if (this.left != null && ((DTNode) dt2).left != null) {
						//set a variable equal to the recursive call on the left node
						leftTree = this.left.equals(((DTNode) dt2).left);
					}
					//check if both the right for the two nodes are not equal to null
					if (this.right !=null && ((DTNode) dt2).right !=null) {
						//set a variable equal to the recursive call on the right node
						rightTree= this.right.equals(((DTNode) dt2).right);
					}
					//return whether the two boolean values are the same
					return rightTree && leftTree;
			}
			//if not the same leaves or nodes return false
			else {
				return false;
			}
		}
	}



	//Given a dataset, this returns the entropy of the dataset
	double calcEntropy(ArrayList<Datum> datalist) {
		double entropy = 0;
		double px = 0;
		float [] counter= new float[2];
		if (datalist.size()==0)
			return 0;
		double num0 = 0.00000001,num1 = 0.000000001;

		//calculates the number of points belonging to each of the labels
		for (Datum d : datalist)
		{
			counter[d.y]+=1;
		}
		//calculates the entropy using the formula specified in the document
		for (int i = 0 ; i< counter.length ; i++)
		{
			if (counter[i]>0)
			{
				px = counter[i]/datalist.size();
				entropy -= (px*Math.log(px)/Math.log(2));
			}
		}

		return entropy;
	}


	// given a datapoint (without the label) calls the DTNode.classifyAtNode() on the rootnode of the calling DecisionTree object
	int classify(double[] xQuery ) {
		return this.rootDTNode.classifyAtNode( xQuery );
	}

	// Checks the performance of a DecisionTree on a dataset
	// This method is provided in case you would like to compare your
	// results with the reference values provided in the PDF in the Data
	// section of the PDF
	String checkPerformance( ArrayList<Datum> datalist) {
		DecimalFormat df = new DecimalFormat("0.000");
		float total = datalist.size();
		float count = 0;

		for (int s = 0 ; s < datalist.size() ; s++) {
			double[] x = datalist.get(s).x;
			int result = datalist.get(s).y;
			if (classify(x) != result) {
				count = count + 1;
			}
		}

		return df.format((count/total));
	}


	//Given two DecisionTree objects, this method checks if both the trees are equal by
	//calling onto the DTNode.equals() method
	public static boolean equals(DecisionTree dt1,  DecisionTree dt2)
	{
		boolean flag = true;
		flag = dt1.rootDTNode.equals(dt2.rootDTNode);
		return flag;
	}

}
