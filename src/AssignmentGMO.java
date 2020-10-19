import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssignmentGMO {

    //Root of the Trie Structure
    TrieNode root;

    /** Constituting the nodes of the Trie Structure **/
    class TrieNode{
        int index;
        TrieNode[] map; // mapping to other TrieNodes linked to it
        int freq;
        boolean isEnd;  // marking the end of word
        TrieNode(){
            index=-1;   // initial index before inserting in the heap
            map=new TrieNode[26];
            isEnd=false;
            freq=0;
        }
    }   // End of TrieNode class

    class MaxHeap{
        int size;    // capacity of the heap(int this case it's 20)
        int currIndex; // denoting the current available index in heap to insert next Node
        Node[] heap;   // implementing heap of nodes through array(all the  processing will take place in this)


        MaxHeap(int size){
            this.size=size;
            currIndex=0;      // initial available index will always be 0(empty heap)
            heap=new Node[size];
        }

        /** Individual Nodes of the Heap,
         * implementing Comparable to perform sorting on the basis of Frequency of the words **/
        class Node implements Comparable<Node> {
            int val;    // frequency of the word in this node(comparing factor)
            TrieNode pointer;  // pointing to the trieNode of the word's pos in the trie
            String word;
            Node(int freq, TrieNode temp, String word){
                this.val=freq;
                this.pointer=temp;
                this.word=word;
            }

            @Override
            public int compareTo(Node o) {
                return o.val - this.val;
            }
        }

        /** To Shift up the node in the heap according to node.val **/
        private void siftUp(int i) {
            int parent = i%2==0? (i/2)-1 : i/2 ;   // calculating parent node
            if(parent>=0 && heap[parent].val<heap[i].val){   // checking if swap is needed
                heap[parent].pointer.index=i;
                heap[i].pointer.index=parent;

                //Swapping the nodes (Shifting up)
                Node temp = heap[parent];
                heap[parent]=heap[i];
                heap[i]=temp;

                siftUp(parent);     // recursively repeating the function till the right pos is reached
                return;
            }
            heap[i].pointer.index=i;   // updating index in the trie of this particular node
        }

        /** To Insert node in a HEAP **/
        public void insert(TrieNode temp, int index, String word) {
            if (index != -1) {     // checking if node already exists
                heap[index].val = temp.freq;
                heap[index].pointer = temp;
                siftUp(index);
            } else {
                //Check for size (free space) of the Heap, and directly Inserting the node.
                if(currIndex<size){
                    heap[currIndex]=new Node(temp.freq, temp,word );
                    heap[currIndex].pointer.index = currIndex;
                    siftUp(currIndex); // to get to its right priority place in heap
                    currIndex++;
                }
                else {
                    int min=temp.freq;
                    int minIndex=-1;

                    // to calculate min value node(less than temp.freq)
                    for (int k = (size / 2); k < size; k++) {
                        if(heap[k].val<min){
                            min=heap[k].val;
                            minIndex=k;
                        }
                    }

                    // if min value exists than we need to replace it with current node
                    if (minIndex!=-1) {
                        heap[minIndex].pointer.index=-1;
                        heap[minIndex] = new Node(temp.freq, temp, word);
                        siftUp(minIndex);
                    }
                }
            }
        }
    } // MaxHeap class ends

    /** To Print the final output: Words along with their Frequency**/
    void printS(String[] doc, int k){

        if(doc.length == 0){
            System.out.println("File is Empty !!");
            return;
        }

        MaxHeap mHeap = new MaxHeap(k);

        // building and executing trie and MaxHeap together
        buildTrieHeap(doc, mHeap);

        // sorting heap according to its node.val using comparator
        Arrays.sort(mHeap.heap);

        for(int i=0;i<k;i++){
            if(mHeap.heap[i]==null)
                break;
            System.out.println(mHeap.heap[i].word + " "+ mHeap.heap[i].val);
        }
    }
    void buildTrieHeap(String[] doc, MaxHeap mHeap){
        int n=doc.length;
        root=new TrieNode();   // defining the root of trie
        for(int i=0;i<n;i++){   // inserting each word in trie
            String a = doc[i];
            insert(a,mHeap);
        }
    }

    /** To Insert word in TRIE **/
    void insert(String a, MaxHeap mHeap){
        TrieNode temp=root;
        int len  = a.length();
        try {
            for (int i = 0; i < len; i++) {
                char c = a.charAt(i);

                // Create new node is NOT already Exists
                if (temp.map[c - 'a'] == null) {
                    temp.map[c - 'a'] = new TrieNode();
                }

                temp = temp.map[c - 'a'];   // going to that particular node
            }
        }catch(ArrayIndexOutOfBoundsException e){
            System.out.println("Character other than 'a-z' or 'A-Z' found in the file !!"+ e.getLocalizedMessage());
        }
        catch(Exception e){
            System.out.println(e.getLocalizedMessage());
        }
        temp.isEnd=true;    // marking the end of word
        temp.freq++;
        mHeap.insert(temp, temp.index,a);   // inserting in heap
    }

    /** Method to read Binary File **/
    static List<String> readBinFile(byte fileBytes[]) throws Exception{
        System.out.println("Inside Binary");
        List<String> ls = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        StringBuilder sbChar = new StringBuilder();
        int ch;
        int counter = 0;

        try{
            for(byte b: fileBytes){
                if(b == 48 || b==49){   //check for 0 & 1 in file
                    sbChar.append((char)b);
                    counter ++;
                }

                if(counter == 8){
                    counter = 0;
                    ch = Integer.parseInt(sbChar.toString(),2);
                    sbChar.delete(0,sbChar.length());

                    if((ch>=65 && ch<=90) || (ch>=97 && ch<=122)){
                        sb.append((char) ch);
                    }
                    else {
                        if(sb.length()>0){
                            ls.add(sb.toString().toLowerCase());
                            sb.delete(0,sb.length());

                        }
                    }
                }

            }
        }catch(Exception e){
            throw new Exception("Error while Reading Binary File. Please check if File is Corrupted.");
        }

        finally{
            sb = null;
            sbChar = null;
        }

        return ls;
    }

    public static void main(String args[]) throws IOException{

        boolean isBinary = true;
        int k = 20;
        if(k<=0){
            System.out.print("Value for no. of words should be atleast 1 or more");
            return;
        }
        FileInputStream fis = null;

        List<String> ls = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        AssignmentGMO ob = new AssignmentGMO();
        int ch;
        try{
            File file = new File("src/Resource/"+args[0]);
            byte [] fileBytes = Files.readAllBytes(file.toPath());
            if(fileBytes.length==0){
                throw new Exception("File Cannot be processed. File is Empty !!");
            }

            for(byte b : fileBytes) {
                if(b != 48 && b != 49){
                    isBinary = false;
                    break;
                }
            }

            if(!isBinary){
                for(byte b : fileBytes) {
                    ch = (int)(char) b;
                    if((ch>=65 && ch<=90) || (ch>=97 && ch<=122)){
                        sb.append((char) b);
                    }
                    else {
                        if(sb.length()>0){
                            ls.add(sb.toString().toLowerCase());
                            sb.delete(0,sb.length());
                        }
                    }
                }
            }
            else ls = readBinFile(fileBytes);

            int listSize = ls.size();
            if(listSize==0){
                throw new Exception("File is Empty or Corrupted !!");
            }
            String[] doc= new String[listSize];
            for(int i=0;i<listSize;i++){
                doc[i]=ls.get(i);
            }

            ob.printS(doc,k);

        }catch(IOException ioe){
            System.out.println("Error while reading: "+ ioe.getLocalizedMessage());
        }
        catch(Exception e){
            System.out.println("Could Not Process File: "+ e.getLocalizedMessage());
        }


    }


}
