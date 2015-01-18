package us.corenetwork.mantle.generation;

import java.util.List;
import us.corenetwork.mantle.MantlePlugin;


public class SchematicNodeParser {
	public static String pickSchematic(List<?> node)
	{
		int childCount = 0;
		for (Object o : node)
			if (!((String) o).startsWith("weights ")) childCount++;

		int[] weights = new int[childCount];
		for (int i = 0; i < childCount; i++)
			weights[i] = 1;

		for (Object o : node)
		{
			if (o instanceof String)
			{
				String text = (String) o;
				if (text.startsWith("weights "))
				{
					String[] textSplit = text.split(" ");
					for (int i = 0; i < childCount; i++)
					{
						weights[i] = Integer.parseInt(textSplit[i + 1]);
					}
				}

			}
		}

		int weightsSum = 0;
		for (int i = 0; i < childCount; i++)
			weightsSum += weights[i];

		int selection = 0;
		int pickedNumber = MantlePlugin.random.nextInt(weightsSum);
		int sum = 0;
		for (int i = 0; i < childCount; i++)
		{
			sum += weights[i];
			if (pickedNumber < sum)
			{
				selection = i;
				break;
			}
		}

		int counter = -1;
		for (int i = 0; i < node.size(); i++)
		{
			Object o = node.get(i);
			if (!((String) o).startsWith("weights ")) 
				counter++;
			else
				continue;

			if (counter == selection)
			{
				return (String) o;
			}
		}
		
		return null;
	}

}
