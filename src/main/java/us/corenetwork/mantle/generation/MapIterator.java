package us.corenetwork.mantle.generation;

public interface MapIterator {
	public boolean advance();
	
	public StructureData getCurStructure();
	public int getCurX();
	public int getCurZ();
}
