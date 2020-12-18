
/**
 * This is the Priority Queue code.
 * This implemeantion uses min heap 
 * 
 * @author Lakshya Seth 
 */

import java.util.*;

public class PQueue<T extends Comparable<T>> {

    // The number of element current inside the heap
    private int heapSize = 0;

    // The internalcapicity of heap
    private int heapCapicity = 0;

    // A dynamic list to track the element inside the heap
    List<T> heap = null;

    // This map keep track of the possible indices a particular
    // node value is found in heap. Having this maping lets
    // us have O(log(n)) removals and O(1) element contains check
    // at the cost of some additional space and minor overhead
    private Map<T, TreeSet<Integer>> map = new HashMap<>();

    // Construct and initially empty priority queue
    public PQueue() {
        this(1);
    }

    // Construct a priority queue with an initial capicity
    public PQueue(int sz) {
        heap = new ArrayList<>(sz);
    }

    // Construct a priority queue using heapify in O(n) time, a great explanation
    // can found:
    // https://www.cs.umd.edu/~meesh/351/mount/lectures/lect14-heapsort-analysis-part.pdf
    public PQueue(T[] elems) {
        heapSize = heapCapicity = elems.length;
        heap = new ArrayList<>(heapCapicity);

        // Place all element n heap
        for (int i = 0; i < heapSize; i++) {
            mapAdd(elems[i], i);
            heap.add(elems[i]);
        }

        // Heapify process, O(n)
        for (int i = Math.max(0, (heapSize / 2) - 1); i >= 0; i--) {
            sink(i);
        }
    }

    public PQueue(Collection<T> elems) {
        this(elems.size());
        for (T elem : elems) {
            add(elem);
        }
    }

    // Return true/false depending on if priority queue is empty
    public boolean isEmpty() {
        return heapSize == 0;
    }

    // Clear everything inside the heap, O(n)
    public void clear() {
        for (int i = 0; i < heapCapicity; i++) {
            heap.set(i, null);
        }
        heapSize = 0;
        map.clear();
    }

    // Return the size of the heap
    public int size() {
        return heapSize;
    }

    // Returns the value of the element with the lowest
    // priority in this priority queue. If the priority
    // queue is empty null is returnd
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return heap.get(0);
    }

    // Remove the root of the heap, O(log(n))
    public T pool() {
        return removeAt(0);
    }

    // Test if an element is in heap, O(1)
    public boolean contains(T elem) {

        // Map lookup to check containment, O(1)
        if (elem == null) {
            return false;
        }

        return map.containsKey(elem);

        // Linear scan to check containment, O(n)
        // for (int i = 0; i < heapSize; i++)
        // if (heap.get(i).equals(elem))
        // return true;
        // return false;

    }

    // Adds an element to the priority queue, the
    // element must not be null, O(log(n))
    public void add(T elem) {
        if (elem == null) {
            throw new IllegalArgumentException();
        }

        if (heapSize < heapCapicity) {
            heap.set(heapSize, elem);
        } else {
            heap.add(elem);
            heapCapicity++;
        }

        mapAdd(elem, heapSize);
        swim(heapSize);
        heapSize++;
    }

    // Test if the value of node i <= node j
    // The method assumes i & j are valid indices, O(1)
    // This is helper method
    private boolean less(int i, int j) {
        T node1 = heap.get(i);
        T node2 = heap.get(j);

        return node1.compareTo(node2) <= 0;
    }

    // Bottom up node swim, O(log(n))
    private void swim(int k) {

        // Grab the index of the next parent node WRT to k
        int parent = (k - 1) / 2;

        // Keep swimming while we not reached the
        // root and while we're less than our parent
        while (k > 0 && less(k, parent)) {

            // Exchange k with parent
            swap(parent, k);

            k = parent;

            // Grab the index of the next parent node WRT to k
            parent = (k - 1) / 2;

        }
    }

    // Top down node sink, O(log(n))
    private void sink(int k) {
        while (true) {

            int left = 2 * k + 1; // Left node
            int right = 2 * k + 2; // Right node
            int smallest = left; // Assume left is the smallest node of tow children

            // Find hich smaller left or right
            // If right is smaller set smallest to be right
            if (right < heapSize && less(right, left)) {
                smallest = right;
            }

            // Stop if we're outside the bounds of the tree
            // or stop early if we cannot sink k anymore
            if (left >= heapSize || less(k, smallest)) {
                break;
            }

            // Moeve down the tree following smallest node
            swap(smallest, k);

            k = smallest;

        }
    }

    // Swap wo nodes. Assume i & j are valid, O(1)
    private void swap(int i, int j) {

        T i_elem = heap.get(i);
        T j_elem = heap.get(j);

        heap.set(i, j_elem);
        heap.set(j, i_elem);

        mapSwap(i_elem, j_elem, i, j);
    }

    // Removes a particular element in the heap, O(log(n))
    public boolean remove(T element) {
        if (element == null) {
            return false;
        }

        // Linear removal via search, O(n)
        // for (int i = 0; i < heapSize; i++)
        // if (heap.get(i).equals(elem)) {
        // removeAt(index);
        // return true;
        // }

        // Logarithmic removal with map, O(log(n))
        Integer index = mapGet(element);

        if (index != null) {
            removeAt(index);
        }

        return index != null;
    }

    // Remove a node at particular index, O(log(n))
    public T removeAt(int i) {

        if (isEmpty()) {
            return null;
        }

        heapSize--;
        T removed_data = heap.get(i);
        swap(i, heapSize);

        // Obliterate the value
        heap.set(heapSize, null);
        mapRemove(removed_data, heapSize);

        // Remove last element
        if (i == heapSize) {
            return removed_data;
        }

        T elem = heap.get(i);

        // Try sinking element
        sink(i);

        // If sinking did not work try swimming
        if (heap.get(i).equals(elem)) {
            swim(i);
        }

        return removed_data;
    }

    // Recursiely checks if this heap is a min heap
    // This method is just for testing purposes to make
    // sure the heap invariant is still being maintained
    // Called this method with k = 0 start at the root
    public boolean isMinHeap(int k) {

        // If we are outside the bounds of the heap return true
        if (k > heapSize) {
            return true;
        }

        int left = 2 * k + 1;
        int right = 2 * k + 2;

        // Make sure that the current node k is less than
        // both of its childr en left, and right if they exist
        // return false otherwise to indicate an invalid heap
        if (left < heapSize && !less(k, left)) {
            return false;
        }

        if (right < heapSize && !less(k, right)) {
            return false;
        }

        return isMinHeap(left) && isMinHeap(right);
    }

    private void mapAdd(T value, int index) {

        TreeSet<Integer> set = map.get(value);

        // New value being inerted n map
        if (set == null) {
            set = new TreeSet<>();

            set.add(index);
            map.put(value, set);
        }

        // Value already exists im map
        else {
            set.add(index);
        }
    }

    // Removes the index at given value, O(log(n))
    private void mapRemove(T value, int index) {
        TreeSet<Integer> set = map.get(value); // 3206125652 // RAMSHREE
        set.remove(index); // TreeSet take O(log(n)) removal time
        if (set.size() == 0) {
            map.remove(value);
        }
    }

    // Extract an index position for the given value
    // NOTE: If a value exists multiple times in the heap the highest
    // index returned (this has arbitarialy een chosen)
    private Integer mapGet(T value) {
        TreeSet<Integer> set = map.get(value);

        if (set != null) {
            return set.last();
        }
        return null;
    }

    // Exchange the index of two node internally with in map
    private void mapSwap(T val1, T val2, int val1_index, int val2_index) {

        Set<Integer> set1 = map.get(val1);
        Set<Integer> set2 = map.get(val2);

        set1.remove(val1_index);
        set2.remove(val2_index);

        set1.add(val2_index);
        set2.add(val1_index);
    }

    @Override
    public String toString() {
        return heap.toString();
    }
}
