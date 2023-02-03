package gotofriend.containers;

public class HashMap<K, V> {
  public LinkedList<HashMapNodeVal<K, V>>[] table;
  int capacity;
  public int size = 0;
  int tabIndex = 0;
  Node<HashMapNodeVal<K, V>> curr = null;
  public HashMap(int capacity) {
    LinkedList<HashMapNodeVal<K, V>>[] table = new LinkedList[capacity];
    this.capacity = capacity;
//    for (int i = capacity; --i>= 0; ) {
//      table[i] = new LinkedList<>();
//    }
    switch (capacity) {
      default:
        throw new RuntimeException("HashMap capacity not supported: " + capacity);
      case 50:
        table[49] = new LinkedList<>();
      case 49:
        table[48] = new LinkedList<>();
      case 48:
        table[47] = new LinkedList<>();
      case 47:
        table[46] = new LinkedList<>();
      case 46:
        table[45] = new LinkedList<>();
      case 45:
        table[44] = new LinkedList<>();
      case 44:
        table[43] = new LinkedList<>();
      case 43:
        table[42] = new LinkedList<>();
      case 42:
        table[41] = new LinkedList<>();
      case 41:
        table[40] = new LinkedList<>();
      case 40:
        table[39] = new LinkedList<>();
      case 39:
        table[38] = new LinkedList<>();
      case 38:
        table[37] = new LinkedList<>();
      case 37:
        table[36] = new LinkedList<>();
      case 36:
        table[35] = new LinkedList<>();
      case 35:
        table[34] = new LinkedList<>();
      case 34:
        table[33] = new LinkedList<>();
      case 33:
        table[32] = new LinkedList<>();
      case 32:
        table[31] = new LinkedList<>();
      case 31:
        table[30] = new LinkedList<>();
      case 30:
        table[29] = new LinkedList<>();
      case 29:
        table[28] = new LinkedList<>();
      case 28:
        table[27] = new LinkedList<>();
      case 27:
        table[26] = new LinkedList<>();
      case 26:
        table[25] = new LinkedList<>();
      case 25:
        table[24] = new LinkedList<>();
      case 24:
        table[23] = new LinkedList<>();
      case 23:
        table[22] = new LinkedList<>();
      case 22:
        table[21] = new LinkedList<>();
      case 21:
        table[20] = new LinkedList<>();
      case 20:
        table[19] = new LinkedList<>();
      case 19:
        table[18] = new LinkedList<>();
      case 18:
        table[17] = new LinkedList<>();
      case 17:
        table[16] = new LinkedList<>();
      case 16:
        table[15] = new LinkedList<>();
      case 15:
        table[14] = new LinkedList<>();
      case 14:
        table[13] = new LinkedList<>();
      case 13:
        table[12] = new LinkedList<>();
      case 12:
        table[11] = new LinkedList<>();
      case 11:
        table[10] = new LinkedList<>();
      case 10:
        table[9] = new LinkedList<>();
      case 9:
        table[8] = new LinkedList<>();
      case 8:
        table[7] = new LinkedList<>();
      case 7:
        table[6] = new LinkedList<>();
      case 6:
        table[5] = new LinkedList<>();
      case 5:
        table[4] = new LinkedList<>();
      case 4:
        table[3] = new LinkedList<>();
      case 3:
        table[2] = new LinkedList<>();
      case 2:
        table[1] = new LinkedList<>();
      case 1:
        table[0] = new LinkedList<>();
    }
    this.table = table;
  }

  /**
   * puts the key in the hashmap
   * ONLY IF not already preset
   * @param key
   * @param obj
   * @return
   */
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
