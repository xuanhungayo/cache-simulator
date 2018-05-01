
import cachesimulator.model.DoubleLinkedList;


/**
 *
 * @author hungcx
 */
public class LinkedListTest {
	public static void main(String[] args) {
		DoubleLinkedList list = new DoubleLinkedList();
		list.pushBack("1");
		list.pushBack("2");
		DoubleLinkedList.Node node =list.end();
		list.pushBack("3");
		list.pushBack("4");
		list.print();
		list.remove(node);
		list.pushFront(node.key);
		list.print();
		list.pushFront("5");
		list.print();
	}
}
