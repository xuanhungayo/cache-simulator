package cachesimulator.model;

/**
 *
 * @author hungcx
 */
// Double-linked list containing string as value
public class DoubleLinkedList {

	public static class Node {

		public Node next = null;
		public Node prev = null;
		public String key;

		Node(String key) {
			this.key = key;
		}
	}

	private Node head = null;
	private Node tail = null;
	private int size = 0;

	public DoubleLinkedList() {
	}

	public int size() {
		int result = 0;
		Node node = head;
		while (node != null) {
			result++;
			node = node.next;
		}
		return result;
	}

	public Node begin() {
		return head;
	}

	public Node end() {
		return tail;
	}

	public boolean empty() {
		return head == null;
	}

	public String front() {
		return head.key;
	}

	public String back() {
		return tail.key;
	}

	public void pushFront(String key) {
		size++;
		if (head == null) {
			head = new Node(key);
			tail = head;
			return;
		}
		Node newNode = new Node(key);
		newNode.next = head;
		head.prev = newNode;
		head = newNode;
	}

	public void pushBack(String key) {
		size++;
		if (tail == null) {
			tail = new Node(key);
			head = tail;
			return;
		}
		Node newNode = new Node(key);
		newNode.prev = tail;
		tail.next = newNode;
		tail = newNode;
	}

	public void popBack() {
		if (empty()) {
			return;
		}
		size--;
		if (tail.prev == null) {
			head = null;
			tail = null;
			return;
		}
		tail = tail.prev;
		tail.next = null;
	}

	public void popFront() {
		if (empty()) {
			return;
		}
		size--;
		if (head.next == null) {
			head = null;
			tail = null;
			return;
		}
		head = head.next;
		head.prev = null;
	}

	public void remove(Node node) {
		Node prev = node.prev;
		Node next = node.next;
		if (prev == null) {
			head = next;
		} else {
			prev.next = next;
		}
		if (next == null) {
			tail = prev;
		} else {
			next.prev = prev;
		}
		size--;
	}

	public void print() {
		Node node = head;
		while (node != null) {
			System.out.print(String.format("%s ", node.key));
			node = node.next;
		}
		System.out.println("");
	}
}
