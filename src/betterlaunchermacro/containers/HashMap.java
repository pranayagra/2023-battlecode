package betterlaunchermacro.containers;

public class HashMap<K, V> {
    public LinkedList<HashMapNodeVal<K, V>>[] table;
    int capacity;
    public int size = 0;
    int tabIndex = 0;
    Node<HashMapNodeVal<K, V>> curr = null;
    public HashMap(int capacity) {
        table = new LinkedList[capacity];
        this.capacity = capacity;
        for (int i = capacity; --i>= 0; ) {
            table[i] = new LinkedList<>();
        }
    }
    public boolean put(K key, V obj) {
        int index = (Math.abs(key.hashCode())) % this.capacity;

        // doesn't contain, add it
        HashMapNodeVal<K, V> node = new HashMapNodeVal<>(key, obj);
        if (!table[index].contains(node)) {
            table[index].add(node);
            size++;
            return true;
        }
        return false;
    }
    // ASSUMING ALREADY CONTAINED
    public void setAlreadyContainedValue(K key, V obj) {
        int index = (Math.abs(key.hashCode())) % this.capacity;
        // doesn't contain, add it
        HashMapNodeVal<K, V> node = new HashMapNodeVal<K, V>(key, null);

        // unroll function calls here for bytecode and also easier

        // below is copied from LinkedList.contains
        Node<HashMapNodeVal<K, V>> llnode = table[index].head;

        while (llnode != null) {
            if (llnode.val.equals(node)) {
                // found it
                llnode.val.val = obj;

                return;
            }
            llnode = llnode.next;
        }
    }
    public V get(K key) {
        int index = (Math.abs(key.hashCode())) % this.capacity;

        // doesn't contain, add it
        HashMapNodeVal<K, V> node = new HashMapNodeVal<K, V>(key, null);

        if (table[index].size == 0)
            return null;

        // unroll function calls here for bytecode and also easier

        // below is copied from LinkedList.contains
        Node<HashMapNodeVal<K, V>> llnode = table[index].head;

        while (llnode != null) {
            if (llnode.val.equals(node)) {
                return llnode.val.val;
            }
            llnode = llnode.next;
        }
        return null;
    }
    public boolean contains(K key) {
        int index = (Math.abs(key.hashCode())) % this.capacity;
        HashMapNodeVal<K, V> node = new HashMapNodeVal<K, V>(key, null);
        return table[index].contains(node);
    }
    public boolean remove(K key) {
        int index = (Math.abs(key.hashCode())) % this.capacity;
        HashMapNodeVal<K, V> node = new HashMapNodeVal<K, V>(key, null);
        // contains it, remove it
        if (table[index].contains(node)) {
            this.size--;
            return table[index].remove(node);
        }
        return false;
    }
    public void resetIterator() {
        tabIndex = table.length;
        curr = null;
    }
    // returns null if no elements exist or iterator exhausted
    public HashMapNodeVal<K, V> next() {
        if (size != 0) {
            if (curr == null || curr.next == null) {
                for (; --tabIndex >= 0; ) {
                    if (table[tabIndex].size != 0) {
                        curr = table[tabIndex].head;
                        return curr.val;
                    }
                }
                // no element left!
                return null;
            }
            else {
                // go to the next one
                curr = curr.next;
                return curr.val;
            }
        }
        else {
            return null;
        }

    }

    public void increment(K key, int valToAdd) {
        int index = (Math.abs(key.hashCode())) % this.capacity;
        // doesn't contain, add it
        HashMapNodeVal<K, V> node = new HashMapNodeVal<K, V>(key, (V)(Object)valToAdd);

        // unroll function calls here for bytecode and also easier

        // below is copied from LinkedList.contains
        Node<HashMapNodeVal<K, V>> llnode = table[index].head;

        while (llnode != null) {
            if (llnode.val.equals(node)) {
                // found it
                llnode.val.val = (V) (Object) (((Integer) llnode.val.val) + valToAdd);

                return;
            }
            llnode = llnode.next;
        }
        table[index].add(node);
        size++;
    }

    public V getOrDefault(K key, V defaultVal) {
        int index = (Math.abs(key.hashCode())) % this.capacity;

        // doesn't contain, add it
        HashMapNodeVal<K, V> node = new HashMapNodeVal<K, V>(key, null);

        if (table[index].size == 0)
            return defaultVal;

        // unroll function calls here for bytecode and also easier

        // below is copied from LinkedList.contains
        Node<HashMapNodeVal<K, V>> llnode = table[index].head;

        while (llnode != null) {
            if (llnode.val.equals(node)) {
                return llnode.val.val;
            }
            llnode = llnode.next;
        }
        return defaultVal;
    }

    public void clear() {
        for (int i = capacity; --i>= 0; ) {
            table[i].clear();
        }
        size = 0;
    }
}
