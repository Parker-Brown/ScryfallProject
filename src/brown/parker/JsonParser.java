package brown.parker;

public class JsonParser {
	private String input;
	
	private boolean hasMore;	//if query results too large, more pages are available
	private int cardsOnPage;     //used to make sure all cards are parsed
	private String nextPage;	//query string for next page
	private int pageNumber;
	private int totalCards;		//used to show percent loaded
	
	//if search result > 175 cards scryfall divides into pages
	private final int PAGE_SIZE = 175;
	
	public JsonParser() {
		pageNumber = 0;
	}
	public boolean hasNextCard() {
		if(cardsOnPage > 0) {
			return true;
		}
		else return false;
	}
	
	public Card getNextCard() {
		cardsOnPage--;
		String name = getName("name",true, true);
		String imagePath = getName("png",true,true);
		float price;
		try {
			price = Float.parseFloat(getName("usd",true,false));
		}
		catch(NumberFormatException noPrice) {
			price = -1;
		}
		return new Card(name, price, imagePath);
	}
	
	//single cards are much simpler and don't need this extra data
	public void setInput(String input, boolean list) {
		this.input = input;
		if(list) {
			totalCards = Integer.parseInt(getName("total_cards", true,false));
			cardsOnPage = totalCards - (pageNumber * PAGE_SIZE);
			
			if(cardsOnPage > PAGE_SIZE) {
				cardsOnPage = PAGE_SIZE;
			}

			hasMore = Boolean.parseBoolean(getName("has_more", true,false));
			if(hasMore) {
				nextPage = getName("next_page",true,true);
			}
		}
	}
	
	//returns the value based on the key, if consume true, edit input string
	private String getName(String name, boolean consume, boolean isString) {
		int startIndex = input.indexOf(name);
		int endIndex = -1;
		if(isString) {
			endIndex = input.indexOf('"', startIndex); //closing name "
			endIndex = input.indexOf('"', endIndex+1);   //opening value "
			endIndex = input.indexOf('"', endIndex+1);   //closing value "
		}
		else {
			endIndex = input.indexOf(',', startIndex);
		}
		String substr = input.substring(startIndex,endIndex);
		substr = substr.substring(substr.indexOf(':') + 1);		
		
		//trim ""
		if(substr.charAt(0) == '"') {
			substr = substr.substring(1);
		}
		if(substr.charAt(substr.length()-1) == '"') {
			substr = substr.substring(0,substr.length()-1);
		}
		
		if(consume) {
			input = input.substring(endIndex + 1);
		}
		
		return substr;
	}
	
	public boolean getHasMore() {
		return hasMore;
	}
	
	public String getNextPage() {
		return nextPage;
	}
	
	//page number reset in setInput
	public void incrementPageNumber() {
		pageNumber++;
	}
	
	public void resetPageNumber() {
		pageNumber = 0;
	}
	
	public int getTotalCards() {
		return totalCards;
	}
	
}
