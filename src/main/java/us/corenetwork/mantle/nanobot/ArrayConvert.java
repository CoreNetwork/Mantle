package us.corenetwork.mantle.nanobot;

public class ArrayConvert {
	
	public static int[] convert(Integer[] input)
	{
		int[] array = new int[input.length];
		for (int i = 0; i < array.length; i++)
			array[i] = input[i].intValue();
		
		return array;
	}
	
	public static byte[] convert(Byte[] input)
	{
		byte[] array = new byte[input.length];
		for (int i = 0; i < array.length; i++)
			array[i] = input[i].byteValue();
		
		return array;
	}
	
	public static Integer[] convert(int[] input)
	{
		Integer[] array = new Integer[input.length];
		for (int i = 0; i < array.length; i++)
			array[i] = Integer.valueOf(input[i]);
		
		return array;
	}
	
	public static Byte[] convert(byte[] input)
	{
		Byte[] array = new Byte[input.length];
		for (int i = 0; i < array.length; i++)
			array[i] = Byte.valueOf(input[i]);
		
		return array;
	}
	
}
