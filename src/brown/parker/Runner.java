package brown.parker;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
/*
 * Control Flow / Menus
 */
public class Runner {
	private static ArrayList<Card> foundCards;	
	private static JsonParser parser;
	private static Scanner userInput;
	private static String lastGETResult;
	private static Card selectedCard;
	private static Manifest manifest;
	
	public static void main(String[] args) throws InterruptedException {
		
		userInput = new Scanner(System.in);
		parser = new JsonParser();
		foundCards = new ArrayList<Card>();
		manifest = new Manifest();
		
		//read in data from file
		manifest.init();
		/*
		 * Main Loop: 
		 * 	1. search (DONE)
		 * 	2. view tags
		 * 	3. quit (DONE)
		 */
		while(true) {
			printMainMenu();
					
			switch(getInteger(1,3)) {
				//***********************Search********************************
				case 1: 
					System.out.println();
					if(search()) {
						System.out.println();
						initializeFoundCards();
						
						while(parser.getHasMore()) {
							//don't spam server with requests or you get banned
							Thread.sleep(100);
							System.out.println("Percent completed: " + (float) foundCards.size() / parser.getTotalCards()); 
							getNextPage();
							initializeFoundCards();
						}
						parser.resetPageNumber();
						System.out.println("\n");
						
						//***********************************Select after Search******************************
						printCardSelectMenu();
						int input = getInteger(0,foundCards.size());
						if(input != 0) {
							selectCard(input);
							printCardOptions();
							modifyCard();
						}
						
						//I could cache results if same search used multiple times, 
						//but I believe storage is a greater concern with the way I'll be using it
						foundCards.clear();
					}//if searched
					break;
				case 2:
					System.out.println();
					viewTags();				//line 236
					break;
				case 3: 
					System.out.println("\nSaving Data to file...");
					manifest.save();
					System.out.println("Data saved successfully!");

					System.exit(0);
				default: 
					System.out.print("\nYou Broke It");
					System.exit(1);
			}
		}//loop
		
	}//main
	
	
	//****************************Searching Helper Functions************************************
	
	/*
	 * returns true if user wanted to search, else false
	 */
	private static boolean search() {
		boolean searched = false;
		ScryfallConnection connection = new ScryfallConnection();
		System.out.print("Enter a string to search for or press q to quit: ");
		String query = userInput.nextLine();
		
		if(!query.equalsIgnoreCase("q")){
			lastGETResult = connection.search(query, false);
			if(lastGETResult == null) {
				search();
			}
			else {
				parser.setInput(lastGETResult, true);
			}
			searched = true;
		}
		return searched;
	}
	
	private static void getNextPage() {
		ScryfallConnection connection = new ScryfallConnection();
		parser.incrementPageNumber();
		lastGETResult = connection.nextPage(parser.getNextPage());
		parser.setInput(lastGETResult, true);	}
	
	private static void initializeFoundCards() {
		while(parser.hasNextCard()) {
			Card card = parser.getNextCard();
			foundCards.add(card);
			//System.out.println(card);
			//System.out.println("number: " + foundCards.size());
		}
	}
	
	
	//*************************************Menu Print Statements / User Input*********************************************
	
	
	private static void printMainMenu() {
		System.out.println("1. Search for a card");
		System.out.println("2. View Tags");
		System.out.println("3. Quit");
		System.out.print("Select an option: ");
	}
	
	private static void printCardSelectMenu() {
		for(int i = 0 ; i < foundCards.size(); i++) {
			System.out.println(i + 1 + ". " + foundCards.get(i).getName());
		}
		System.out.print("\nSelect a card by inputting the number or 0 to go back: ");
		
	}
	
	private static int getInteger(int min, int max) {
		int input = -1;
		try {
			input = userInput.nextInt();
			userInput.nextLine();
		}
		catch(InputMismatchException idiot) {
			System.out.print("Valid input is integers " + min + "-" + max + ". Try again: ");
			userInput.nextLine();
			input = getInteger(min,max);
		}
		if(input < min || input > max) {
			System.out.print("Valid input is integers " + min + "-" + max + ". Try again: ");
			input = getInteger(min,max);
		}
		return input;
	}
	
	
	
	private static void printCardOptions() {
		System.out.println("1. Check Price");
		System.out.println("2. Add Tag");
		System.out.println("3. Remove Tag");
		System.out.println("4. Return to Main Menu");
		System.out.print("Select an option: ");
	}
	
	private static void printTags() {
		System.out.println();
		for(int i = 0 ; i < selectedCard.getTags().size() ; i++) {
			System.out.println(i+1+ ". " + selectedCard.getTags().get(i));
		}
		System.out.print("\nSelect a tag to remove or 0 to go back: ");
	}
	
	//***********************************Action on Selected cards******************************************
	
	//Image of card always shown on select, Card objects have their own graphics methods
	private static void selectCard(int index) {
		if(index == 0) {
			System.out.println();
			return;
		}
		selectedCard = foundCards.get(index -1);
		selectedCard.displayImage();
		System.out.println("\n\"" + selectedCard.getName() + "\" selected.\n");
	}
	
	
	/*
	 * What to do with selected card from menu choice 1, can be: 
	 * 	1. price 
	 *  2. add tag 
	 *  3. main menu 
	 */
	private static void modifyCard() {
		switch(getInteger(1,4)) {
			case 1: 
				if(selectedCard.getPrice() == -1) {
					System.out.println("\nPrice data unavailable\n");
				}
				else {
					System.out.println("\nPrice: " + selectedCard.getPrice() + "\n");
				}
				
				printCardOptions();
				modifyCard();
				break;
			case 2:
				//add tag
				System.out.print("Enter the tag: ");
				String tag = userInput.nextLine();
				System.out.println();
				
				manifest.addTag(selectedCard, tag);
				printCardOptions();
				modifyCard();
				break;
			case 3: 
				removeTag();
				printCardOptions();
				modifyCard();
				break;
			case 4:
				selectedCard.closeImage();
				System.out.println();
				break;
			default: 
				System.out.println("you broke it");
				System.exit(1);
		}
	}
	
	private static void viewTags() {
		ArrayList<String> tags = manifest.getTags();
		if(tags.size() == 0) {
			System.out.println("You do not have any tags saved.");
		}
		else {
			for(int i = 0 ; i < tags.size();i++) {
				System.out.println(i+1 + ". " + tags.get(i));
			}
			System.out.print("Choose a tag or press 0 to go back: ");
			int input = getInteger(0,tags.size());
			if(input == 0) {
				System.out.println();
				return;
			}
			else {
				//*****************************************View Tags Extends Select****************************
				foundCards = manifest.getCardsWithTag(tags.get(input-1));
				printCardSelectMenu();
				input = getInteger(0,foundCards.size());
				if(input != 0) {
					selectCard(input);
					printCardOptions();
					modifyCard();
				}
				else {
					System.out.println();
				}
				foundCards.clear(); //again could cache, but not important
			}
		}
	}
	
	private static void removeTag() {
		if(selectedCard.getTags().size() == 0) {
			System.out.print("The selected card has no tags.");
		}
		else {
			printTags();
			int input = getInteger(0,selectedCard.getTags().size());
			System.out.println();
			if(input == 0) {
				return;
			}
			else {
				manifest.removeTag(selectedCard, selectedCard.getTags().get(input-1));
			}
		}
	}
}

