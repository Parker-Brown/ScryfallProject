package brown.parker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/*
 * Stores a list of tags and saved cards
 */
public class Manifest {
	//lists of cards and tags to be written to file at program termination
	private ArrayList<String> tags;
	private ArrayList<Card> cards;	//dirty list, cards to be saved at end of program
	
	private final String TAG_LIST = "tags.csv";
	private File manifest;
	private JsonParser parser;
	
	public Manifest() {
		tags = new ArrayList<String>();
		cards = new ArrayList<Card>();
		parser = new JsonParser();
	}
	
	//*******************************Runtime Modifications*********************************
	//adds a tag to a card 
	public void addTag(Card card, String tag) {
		if(card.getTags().contains(tag)) {
			System.out.println("The selected card already has that tag.\n");
		}
		else {
			card.addTag(tag);
			//if not in cards, add
			if(!cards.contains(card)) {
				cards.add(card);
			}
			//if it is in the list already, we need to replace with the updated version
			else {
				cards.remove(card);		//since equals method checks card name only it will remove old card data
				cards.add(card);
			}
			
			if(!tags.contains(tag)) {
				tags.add(tag);
			}
		}
	}
	
	//removes tags, likely could be more effiecent...
	public void removeTag(Card card, String tag) {
		card.removeTag(tag);
		boolean last = true;
		for(int i = 0 ; i < cards.size(); i++) {
			Card currentCard = cards.get(i);
			ArrayList<String> currentTags = currentCard.getTags();
			
			for(int j = 0 ; j < currentTags.size(); j++) {
				if(currentTags.get(j).equals(tag)) {
					last = false;
				}
			}
		}
		//if no tags exist after removal, the file will be deleted 
		if(last) {
			tags.remove(tag);
			File f = new File(tag + ".txt");
			if(f.exists()) {
				f.delete();
			}
		}
	}
	
	
	//*******************************File Input*******************************************

	
	/*
	 * 	reads previously stored tags from file.  It would be much easier to use json and my parser yet I want a format
	 * 	that is easily readable at a glance (a file with just a list of cards, which is easier to export to other 
	 * 	Magic software if I want later
	 */
	public void init() {
		//manifest holds all tags in csv
		manifest = new File(TAG_LIST);
		try {
			if(manifest.exists()) {
				//only read if the file existed prior to running program
				
				BufferedReader br = new BufferedReader(new FileReader(manifest));
				String input = br.readLine();
				String[] list = input.split(",");
				for(int i = 0 ; i < list.length ; i++) {
					tags.add(list[i]);
				}
				br.close();
			}
			//I don't create a manifest unless there's data on close
			
		} catch (IOException e1) {
			System.out.println("This can't possibly fail /s");
			System.exit(1);
		}
		
		//****************************Read Cards*******************************
		/*
		 * read each tag's file 
		 */
		for(int i = 0 ; i < tags.size() ; i++) {
			File currentFile = new File(tags.get(i) + ".txt");
			try {
				BufferedReader br = new BufferedReader(new FileReader(currentFile));
				String currentString = br.readLine();
				while(currentString != null) {
					createCard(currentString, tags.get(i));
					currentString = br.readLine();
				}
				br.close();
			} catch (IOException e1) {
				System.out.println("couldn't find a corresponding data file");
				System.exit(1);
			}
		}
		
	}//init	
	
	//****************************File Output***************************************
	
	/*
	 * Current implementation is to write all data even if cards are not modified.  This is wasteful but I don't 
	 * want to bother parsing each and every tag file and removing instances of the card.  A bulk update is much 
	 * easier to program and should work for my uses (I will flush the data occasionally to keep the overhead low)
	 */
	public void save() {
		
		if(cards.size() > 0){
			ArrayList<FileWriter> writers = new ArrayList<FileWriter>();
			FileWriter taglist = null;
			try {
				taglist = new FileWriter(TAG_LIST);
				
				for(int i = 0 ; i < tags.size(); i++) {
					writers.add(new FileWriter(tags.get(i) + ".txt"));	
					taglist.write(tags.get(i) + ",");
				}
				
				//for each card
				for(int i = 0 ; i < cards.size(); i++) {
					Card currentCard = cards.get(i);
					ArrayList<String> currentTags = currentCard.getTags();
					
					for(int j = 0 ; j < currentTags.size(); j++) {
						String currentTag = currentTags.get(j);
						
						for(int k = 0 ; k < tags.size(); k++) {
							if(currentTag.equals(tags.get(k))){
								writers.get(k).write(currentCard.getName() + "\n");
								break;	//don't bother with rest of tags, we found the match
							}
						}
					}
				}
				
				for(int i = 0 ; i < writers.size() ; i++) {
					writers.get(i).close();
				}
				taglist.close();
				
			} catch (IOException e) {
				System.out.println("ERROR: could not save");
				System.exit(1);
			}
		}
	}
	
	//******************************Helpers********************************************
	
	private void createCard(String name, String tag) {
		//if the card already exists just update the tag
		for(int i = 0 ; i < cards.size();i++) {
			if(cards.get(i).getName().equals(name)) {
				cards.get(i).addTag(tag);
				return;
			}
		}
		
		//otherwise, create a new card
		ScryfallConnection connection = new ScryfallConnection();
		String result = connection.search(name, true);
		parser.setInput(result, false);
		Card card = parser.getNextCard();
		card.addTag(tag);
		cards.add(card);
	}
	
	//*******************************Getters**************************************
	public ArrayList<String> getTags(){
		return tags;
	}
	
	public ArrayList<Card> getCards(){
		return cards;
	}
	
	public ArrayList<Card> getCardsWithTag(String tag){
		ArrayList<Card> result = new ArrayList<Card>();
		for(int i = 0 ; i < cards.size(); i++) {
			for(int j = 0 ; j < cards.get(i).getTags().size() ; j++) {
				if(cards.get(i).getTags().get(j).equals(tag)) {
					result.add(cards.get(i));
				}
			}
		}
		return result;
	}
}
