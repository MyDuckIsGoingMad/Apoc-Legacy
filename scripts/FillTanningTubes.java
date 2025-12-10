/*
 * This file is code made for modifying the Haven and Hearth client.
 * Copyright (c) 2012-2015 Xcom (Sahand Hesar) <sahandhesar@gmail.com>
 *  
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this code.  If not, see <http://www.gnu.org/licenses/>.
*/

import addons.*;
import haven.*;

import java.util.ArrayList;

public class FillTanningTubes extends Thread{
	public String scriptName = "Tanning Tubes Filler";
	
	HavenUtil m_util;
	int m_option;
	String m_modify;
	
	public void ApocScript(HavenUtil util, int option, String modify){
		m_util = util;
		m_option = option;
		m_modify = modify;
	}
	
	// Find well
	Gob findWell(){
		Gob well = m_util.findClosestObject("gfx/terobjs/well");
		if(well == null){
			m_util.sendErrorMessage("No well found nearby.");
			m_util.stop = true;
		}
		return well;
	}
	
	// Fill all buckets from well
	void fillBucketsFromWell(Gob well){
		if(well == null || m_util.stop) return;
		
		m_util.walkTo(well.getr().add(-7, 0));
		
		// Wait for player to reach well
		int walkTimeout = 0;
		while(m_util.checkPlayerWalking() && !m_util.stop && walkTimeout < 100){
			m_util.wait(100);
			walkTimeout++;
		}
		
		ArrayList<Item> items = m_util.getItemsFromBag();
		Inventory bag = m_util.getInventory("Inventory");
		
		for(Item bucket : items){
			if(m_util.stop) break;
			
			// Look for empty buckets
			if(bucket.GetResName().contains("buckete")){
				// Pick up bucket
				bucket.wdgmsg("take", new Object[]{Coord.z});
				m_util.wait(200);
				
				// Use bucket on well
				m_util.itemActionWorldObject(well, 0);
				
				// Wait for bucket to fill
				int timeout = 0;
				while(!m_util.stop && timeout < 50){
					if(m_util.getMouseItem() != null && 
					   m_util.getMouseItem().GetResName().contains("bucket-water")){
						break;
					}
					m_util.wait(100);
					timeout++;
				}
				
				// Drop bucket back
				bag.drop(new Coord(0, 0), bucket.c);
				m_util.wait(200);
			}
		}
		
		// Clear hand if holding anything
		while(m_util.mouseHoldingAnItem() && !m_util.stop){
			m_util.wait(100);
		}
	}
	
	// Find nearest tanning tube
	Gob findTanningTube(){
		Gob tube = m_util.findClosestObject("gfx/terobjs/ttub");
		if(tube == null){
			m_util.sendErrorMessage("No tanning tube found nearby.");
			m_util.stop = true;
		}
		return tube;
	}
	
	// Fill tanning tube from buckets
	boolean fillTanningTube(Gob tube){
		if(tube == null || m_util.stop) return false;
		
		m_util.walkTo(tube.getr());
		
		// Wait for player to reach tube
		int walkTimeout = 0;
		while(m_util.checkPlayerWalking() && !m_util.stop && walkTimeout < 100){
			m_util.wait(100);
			walkTimeout++;
		}
		
		ArrayList<Item> items = m_util.getItemsFromBag();
		Inventory bag = m_util.getInventory("Inventory");
		
		for(Item bucket : items){
			if(m_util.stop) break;
			
			// Look for full water buckets
			if(bucket.GetResName().contains("bucket-water")){
				// Pick up bucket
				bucket.wdgmsg("take", new Object[]{Coord.z});
				m_util.wait(200);
				
				// Use bucket on tanning tube
				m_util.itemActionWorldObject(tube, 0);
				
				// Wait for action to complete
				int timeout = 0;
				while(!m_util.stop && timeout < 50){
					if(m_util.getMouseItem() != null && 
					   m_util.getMouseItem().GetResName().contains("buckete")){
						break;
					}
					m_util.wait(100);
					timeout++;
				}
				
				// Drop bucket back
				bag.drop(new Coord(0, 0), bucket.c);
				m_util.wait(200);
			}
		}
		
		// Clear hand if holding anything
		while(m_util.mouseHoldingAnItem() && !m_util.stop){
			m_util.wait(100);
		}
		
		// Check if tube is full (we'll assume it's full after filling all buckets)
		// You may need to add actual checking logic here based on game mechanics
		return true;
	}
	
	// Check if we have empty buckets
	boolean hasEmptyBuckets(){
		ArrayList<Item> items = m_util.getItemsFromBag();
		for(Item item : items){
			if(item.GetResName().contains("buckete")){
				return true;
			}
		}
		return false;
	}
	
	public void run(){
		m_util.setPlayerSpeed(2);
		m_util.openInventory();
		m_util.wait(500);
		
		// Main loop
		while(!m_util.stop){
			// 1. Find well
			Gob well = findWell();
			if(m_util.stop) break;
			
			// 2. Fill all buckets from well
			fillBucketsFromWell(well);
			if(m_util.stop) break;
			
			// 3. Find nearest tanning tube
			Gob tube = findTanningTube();
			if(m_util.stop) break;
			
			// 4. Fill tanning tube from buckets
			boolean tubeFull = fillTanningTube(tube);
			
			// 5. If tube is full, we're done
			if(tubeFull && !hasEmptyBuckets()){
				m_util.sendErrorMessage("Tanning tube is full!");
				break;
			}
		}
		
		m_util.running(false);
	}
}