package control_app.ctrl;

public class ArrayListGlobalSharedVar {
	
	int size;
	private int length = 0;
	public GlobalSharedVar ListGlobalSharedVar[] = {};
	
	public ArrayListGlobalSharedVar(int size)
	{
		ListGlobalSharedVar = new GlobalSharedVar[size];
	}
	 public void add(GlobalSharedVar e) {
         if (this.size == ListGlobalSharedVar.length) {
                //ensureCapacity(); // increase current capacity of list, make it
                                                  // double.
         }
         ListGlobalSharedVar[length++] = e;
	 }
	 
	/* public E get(int index) {
         // if index is negative or greater than size of size, we throw
         // Exception.
         if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Size "
                             + index);
         }
         return (E) elementData[index]; // return value on index.
	 }*/

}
