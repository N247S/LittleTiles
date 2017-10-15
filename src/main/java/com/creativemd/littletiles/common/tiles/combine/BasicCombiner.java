package com.creativemd.littletiles.common.tiles.combine;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

public class BasicCombiner {
	
	//private static ThreadLocal<BasicCombiner> combiner = new ThreadLocal<>();
	
	/**
	 * only one combination process can run at the same time on each thread
	 */
	public static void combineBoxes(List<LittleTileBox> boxes)
	{
		/*BasicCombiner local = combiner.get();
		if(local == null)
		{
			local = new BasicCombiner();
			combiner.set(local);
		}
		
		local.combineBox(boxes);*/
		new BasicCombiner().combineBox(boxes);
	}
	
	/**
	 * only one combination process can run at the same time on each thread
	 */
	public static void combineTiles(List<LittleTile> tiles)
	{
		/*BasicCombiner local = combiner.get();
		if(local == null)
		{
			local = new BasicCombiner();
			combiner.set(local);
		}
		
		local.combineTile(tiles);*/
		new BasicCombiner().combineTile(tiles);
	}
	
	/**
	 * only one combination process can run at the same time on each thread
	 */
	public static void combineTiles(List<LittleTile> tiles, LittleStructure structure)
	{
		/*BasicCombiner local = combiner.get();
		if(local == null)
		{
			local = new BasicCombiner();
			combiner.set(local);
		}
		
		local.combineTile(tiles, structure);*/
		new BasicCombiner().combineTile(tiles, structure);
	}
	
	protected List<LittleTileBox> boxes;
	protected List<LittleTile> tiles;
	protected int i;
	protected int j;
	protected boolean modified;
	protected LittleStructure structure;
	protected LittleTile currentTile;
	
	public BasicCombiner() {
		
	}
	
	public List<LittleTileBox> getBoxes()
	{
		return boxes;
	}
	
	public List<LittleTile> getTiles()
	{
		return tiles;
	}
	
	public void combineTile(List<LittleTile> tiles)
	{
		this.boxes = new ArrayList<>();
		for (LittleTile tile : tiles) {
			boxes.add(tile.box);
		}
		this.tiles = tiles;
		modified = true;
		while(modified)
		{
			modified = false;
			i = 0;
			while(i < tiles.size()){
				j = 0;
				while(j < tiles.size()) {
					if(i != j && !tiles.get(i).isStructureBlock && !tiles.get(j).isStructureBlock && tiles.get(i).canBeSplitted() && tiles.get(j).canBeSplitted() && tiles.get(i).canBeCombined(tiles.get(j)) && tiles.get(j).canBeCombined(tiles.get(i)))
					{
						this.currentTile = tiles.get(i);
						LittleTileBox box = tiles.get(i).box.combineBoxes(tiles.get(j).box, this);
						if(box != null)
						{
							tiles.get(i).box = box;
							tiles.get(i).combineTiles(tiles.get(j));
							tiles.remove(j);
							boxes.set(i, box);
							boxes.remove(j);
							if(i > j)
								i--;
							modified = true;
							continue;
						}
					}
					j++;
				}
				i++;
			}
		}
		this.tiles = null;
		this.boxes = null;
		this.currentTile = null;
	}
	
	public void combineTile(List<LittleTile> tiles, LittleStructure structure)
	{
		this.structure = structure;
		
		if(!structure.hasLoaded())
			return ;
		
		this.boxes = new ArrayList<>();
		for (LittleTile tile : tiles) {
			boxes.add(tile.box);
		}
		this.tiles = tiles;
		
		boolean isMainTile = false;
		modified = true;
		while(modified)
		{
			modified = false;
			
			i = 0;
			while(i < tiles.size()){
				if(tiles.get(i).structure != structure)
				{
					i++;
					continue;
				}
				
				j = 0;
				
				while(j < tiles.size()) {
					if(tiles.get(j).structure != structure)
					{
						j++;
						continue;
					}
					
					if(i != j && tiles.get(i).canBeSplitted() && tiles.get(j).canBeSplitted() && tiles.get(i).canBeCombined(tiles.get(j)) && tiles.get(j).canBeCombined(tiles.get(i)))
					{
						this.currentTile = tiles.get(i);
						
						if(tiles.get(i).isMainBlock || tiles.get(j).isMainBlock)
							isMainTile = true;
						LittleTileBox box = tiles.get(i).box.combineBoxes(tiles.get(j).box, this);
						if(box != null)
						{
							tiles.get(i).box = box;
							tiles.get(i).combineTiles(tiles.get(j));
							tiles.remove(j);
							boxes.set(i, box);
							boxes.remove(j);
							if(i > j)
								i--;
							modified = true;
							continue;
						}
					}
					j++;
				}
				i++;
			}
		}
		
		if(isMainTile)
			structure.selectMainTile();
		
		this.tiles = null;
		this.boxes = null;
		this.structure = null;
		this.currentTile = null;
	}
	
	public void combineBox(List<LittleTileBox> boxes)
	{
		this.boxes = boxes;
		modified = true;
		while(modified)
		{
			modified = false;
			i = 0;
			while(i < boxes.size()){
				j = 0;
				while(j < boxes.size()) {
					if(i != j)
					{
						LittleTileBox box = boxes.get(i).combineBoxes(boxes.get(j), this);
						if(box != null)
						{
							boxes.set(i, box);
							boxes.remove(j);
							modified = true;
							if(i > j)
								i--;
							continue;
						}
					}
					j++;
				}
				i++;
			}
		}
		this.boxes = null;
	}
	
	public boolean cutOut(LittleTileBox searching, LittleTile toCombine)
	{
		for (LittleTile tile : tiles) {
			boolean canBeCombined = tile.canBeSplitted() && tile.canBeSplitted() && tile.canBeCombined(toCombine) && toCombine.canBeCombined(tile);
			if(this.structure != null && (toCombine.structure != structure || tile.structure != structure))
				canBeCombined = false;
			else if(this.structure == null && tile.isStructureBlock)
				canBeCombined = false;
			
			if(canBeCombined && searching.getClass() == tile.box.getClass() && tile.box.containsBox(searching))
			{
				List<LittleTileBox> cutOut = tile.box.cutOut(searching);
				if(cutOut != null)
				{
					boxes.addAll(cutOut);
					for (LittleTileBox cutBox : cutOut) {
						LittleTile cutTile = toCombine.copy();
						cutTile.box = cutBox;
						tiles.add(cutTile);
					}
				}
				removeBox(tile.box);
				return true;
			}
		}
		return false;
	}
	
	public boolean cutOut(LittleTileBox searching)
	{
		if(currentTile != null)
			return cutOut(searching, currentTile);
		
		for (LittleTileBox box : boxes) {
			if(searching.getClass() == box.getClass() && box.containsBox(searching))
			{
				List<LittleTileBox> cutOut = box.cutOut(searching);
				if(cutOut != null)
					boxes.addAll(cutOut);
				removeBox(box);
				return true;
			}
		}
		return false;
	}
	
	public void removeBox(LittleTileBox box)
	{
		int index = boxes.indexOf(box);
		if(index != -1)
		{
			if(i > index)
				i--;
			if(j > index)
				j--;
			modified = true;
			boxes.remove(index);
			if(tiles != null)
				tiles.remove(index);
		}
	}
}