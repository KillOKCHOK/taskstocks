package com.trade.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class parsedOrder {
	private String type;
	private int val;
	private int itemOrder;
	private int fromItem;

	@Override
	public String toString() {
		return "parsedOrder [type=" + type + ", val=" + val + ", itemOrder=" + itemOrder + ", fromItem=" + fromItem
				+ "]";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getVal() {
		return val;
	}

	public void setVal(int val) {
		this.val = val;
	}

	public int getItemOrder() {
		return itemOrder;
	}

	public void setItemOrder(int itemOrder) {
		this.itemOrder = itemOrder;
	}

	public int getFromItem() {
		return fromItem;
	}

	public void setFromItem(int fromItem) {
		this.fromItem = fromItem;
	}

}

class parsedUpdate {
	private String type;
	private int index;
	private int itemPrice;
	private int itemSize;

	@Override
	public String toString() {
		return "parsedUpdate [type=" + type + ", index=" + index + ", itemPrice=" + itemPrice + ", itemSize=" + itemSize
				+ "]";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getItemPrice() {
		return itemPrice;
	}

	public void setItemPrice(int itemPrice) {
		this.itemPrice = itemPrice;
	}

	public int getItemSize() {
		return itemSize;
	}

	public void setiIemSize(int itemSize) {
		this.itemSize = itemSize;
	}

}

public class Main {

	public static ArrayList<String> inputArr = new ArrayList<String>();
	
	public static ArrayList<parsedOrder> parsedOrdersArr = new ArrayList<parsedOrder>();
	public static ArrayList<parsedUpdate> parsedUpdates = new ArrayList<parsedUpdate>();
	public static ArrayList<String> processedOrdersArr = new ArrayList<String>();
	public static String result="";
	
	private static void readFileInputStreamReader(String fileName) {

		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fileName))))) {

			String line;
			while ((line = br.readLine()) != null) {
				inputArr.add(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static int lastOrderItem = 0;

	private static void parseOrders(List<String> l) {
		for (int i = 0; i < l.size(); i++) {
			String s = l.get(i);
			if (s.startsWith("o")) {
				parsedOrder po = new parsedOrder();
				po.setFromItem(lastOrderItem);
				po.setItemOrder(i);
				lastOrderItem = i;
				if (s.startsWith("o,buy")) {
					po.setType("buy");
				} else if (s.startsWith("o,sell")) {
					po.setType("sell");
				}
				int n = 0;
				try {
					n = Integer.parseInt(s.replaceFirst("^.*\\D", ""));
				} catch (Exception e) {
					System.out.println(e);
				}
				po.setVal(n);
				parsedOrdersArr.add(po);
			}

		}
	}

	private static void parseUpdates(List<String> l) {
		for (int i = 0; i < l.size(); i++) {
			String s = l.get(i);
			if (s.startsWith("u")) {
				parsedUpdate pu = new parsedUpdate();
				pu.setIndex(i);
				pu.setType(s.substring(s.length() - 3, s.length()));
				String priceAndSize = s.substring(2, s.length() - 4);
				int commaIndex = priceAndSize.indexOf(",");
				String price = priceAndSize.substring(0, commaIndex);
				String size = priceAndSize.substring(commaIndex+1, priceAndSize.length());
				pu.setItemPrice(Integer.parseInt(price));
				pu.setiIemSize(Integer.parseInt(size));
				parsedUpdates.add(pu);
			}
		}
	}
	
	
	private static void processOrders(List<parsedUpdate> updatesArr, ArrayList<parsedOrder> ordersArr) {
		int maxBidPrice = 0;
		int maxBidPriceIndex = 0;
		int minAskPrice = 0;
		int minAskPriceIndex = 0;
		for (int i = 0; i < ordersArr.size(); i++) {
			parsedOrder order = ordersArr.get(i);
			if(order.getType().equals("sell")) {
				// loop through updates
				for (int j = 0; j < updatesArr.size(); j++) {
					// check if update was before order
					if(updatesArr.get(j).getIndex()>ordersArr.get(i).getFromItem()&&updatesArr.get(j).getIndex()<ordersArr.get(i).getItemOrder()) {
						// check update type and best price
						if(updatesArr.get(j).getItemPrice()>maxBidPrice&&updatesArr.get(j).getType().equals("bid")&&updatesArr.get(j).getItemSize()>order.getVal()) {
							maxBidPrice = updatesArr.get(j).getItemPrice();
							maxBidPriceIndex = j;							
						}
					}
				}
				// o,sell,<size> - removes <size> shares out of bids, most expensive ones
				updatesArr.get(maxBidPriceIndex).setiIemSize(updatesArr.get(maxBidPriceIndex).getItemSize()-ordersArr.get(i).getVal());
			}
			//for each order find smallest ask price
			else if(order.getType().equals("buy")) {
				//find first update price for asks to compare with 
				for (int j = 0; j < updatesArr.size(); j++) {
					if(updatesArr.get(j).getIndex() <= ordersArr.get(i).getItemOrder() && updatesArr.get(j).getType().equals("ask")) {
						minAskPrice = updatesArr.get(j).getItemPrice();
						minAskPriceIndex = updatesArr.get(j).getIndex();
						break;
					}
				}
				for (int j = 0; j < updatesArr.size(); j++) {
					// find update with lowest ask
					if(updatesArr.get(j).getItemPrice()<minAskPrice&&updatesArr.get(j).getItemSize()>order.getVal()&&updatesArr.get(j).getIndex()<updatesArr.get(j).getIndex()&&updatesArr.get(j).getType().equals("ask")) {
						minAskPrice = updatesArr.get(j).getItemPrice();
						minAskPriceIndex = updatesArr.get(j).getIndex();
					}
				}
				updatesArr.get(minAskPriceIndex).setiIemSize(updatesArr.get(minAskPriceIndex).getItemSize()-ordersArr.get(i).getVal());
			}
		}
			
	}
	
	// Form queries array l
	private static void processQueries(List<String> l) {
		int maxBidPrice = 0;
		int maxBidPriceIndex = 0;
		int minAskPrice = 0;
		int minAskPriceIndex = 0;
		for (int i = 0; i < l.size(); i++) {
			//most expensive bids (price,size)
			if(l.get(i).startsWith("q,best_bid")) {
				for (int j = 0; j < parsedUpdates.size(); j++) {
					if(parsedUpdates.get(j).getItemPrice()>maxBidPrice&&parsedUpdates.get(j).getType().equals("bid")&&parsedUpdates.get(j).getIndex()<i) {
						maxBidPrice = parsedUpdates.get(j).getItemPrice();
						maxBidPriceIndex = parsedUpdates.get(j).getIndex();
					}
				}
				int x = (l.get(maxBidPriceIndex).length()-4);
				result = result + l.get(maxBidPriceIndex).substring(2, x )+"\n";
			}//cheapest asks
			else if(l.get(i).startsWith("q,best_ask")) {
				for (int j = 0; j < parsedUpdates.size(); j++) {
					if(parsedUpdates.get(j).getIndex()<i&&parsedUpdates.get(j).getType().equals("ask")) {
						minAskPrice = parsedUpdates.get(j).getItemPrice();
						minAskPriceIndex = parsedUpdates.get(j).getIndex();
						break;
					}
				}
				for (int j = 0; j < parsedUpdates.size(); j++) {
					if(parsedUpdates.get(j).getItemPrice()<minAskPrice&&parsedUpdates.get(j).getType().equals("ask")&&parsedUpdates.get(j).getIndex()<i) {
						minAskPrice = parsedUpdates.get(j).getItemPrice();
						minAskPriceIndex = parsedUpdates.get(j).getIndex();
					}
				}
				int x = (l.get(minAskPriceIndex).length()-4);
				result = result + l.get(minAskPriceIndex).substring(2, x )+"\n";
			}
			// left size of shares with given price
			else if(l.get(i).startsWith("q,size,")) {
			//	result = result+l.get(i).substring(7, l.get(i).length())+"\n";
				int queryPrice  = Integer.parseInt(l.get(i).substring(7, l.get(i).length()));
				for (int j = parsedUpdates.size()-1;j>0; j--) {
					if(parsedUpdates.get(j).getItemPrice()==queryPrice) {
						result = result+parsedUpdates.get(j).getItemSize()+"\n";
						break;
					}
				}
			}
		}
	}
	
	public static void createFile(String path) {
	    try {
	        File file = new File(path);
	        if (!file.exists()) {
	            file.createNewFile();
	        } 
	        if (file.exists()){
	            FileOutputStream writer = new FileOutputStream(path);
	            writer.write((result).getBytes());
	            writer.close();
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void main(String[] args) {

		// readFileInputStreamReader("./src/input.txt");
		readFileInputStreamReader("./input.txt");

		// Parse Orders
		parseOrders(inputArr);

		// parse Updates
		parseUpdates(inputArr);

		// Process Orders (removes amount of stocks from best bid or ask based on order
		processOrders(parsedUpdates,parsedOrdersArr);

		// Process queries output
		processQueries(inputArr);

		// Create output file with result
		createFile("./output.txt");
		
		// Console.log the result
		System.out.println(result);       	
		
	}
}
