import java.util.*;
import java.io.*;

/**
 * data interface for 2D and DNA
 * 
 * @author Pan
 */
public interface Data extends Serializable {
	/**
	 * distance between a data point and a centroid
	 * 
	 * @param d
	 * @return
	 * @throws IllegalArgumentException
	 */
	public int distance(Data d) throws IllegalArgumentException;

	/**
	 * calculate the average of distances
	 * 
	 * @param list
	 * @return
	 * @throws IllegalArgumentException
	 */
	public Data average(List<Data> list)
			throws IllegalArgumentException;
}
