package mega.menu;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;



public class MegaMenu {
	private static final String FILE_PATH = "menu/choicess.xml";
	
	private static final String MENU_TAG = "menu";
	private static final String MENU_TAG_ATTRIBUTE = "name";	
	private static final String CHOICE_TAG = "choice";
	private static final String CHOICE_TAG_ATTRIBUTE = "value";	

	private static final String MENU_BUILDER_TITLE = "Welcome to the menu builder.\n\n"
			+ "If you are reading this, it means that the file at '" + FILE_PATH + "' is not present.\n"
			+ "Through this wizard, you can generate a multi-layered menu to be used in later executions of the program.\n"
			+ "Let's start.\n\n";
	
	private SingleMenu builderMainMenu = null;
	
	private SingleMenu megaMainMenu;	
	
	public MegaMenu() {
		
	}
	
	public void setMenu() {
		System.out.println(MENU_BUILDER_TITLE);

		InputStream in = getClass().getResourceAsStream("choices.xml"); 
		XMLReader re = new XMLReader(in);
		re.init();
		this.megaMainMenu = re.readMenus();
		
		
		boolean going = true;
		while (going) {
			ArrayList<Integer> chosen = megaMainMenu.choose();
			switch (chosen.get(0)) {
			case 1:
				if (builderMainMenu == null) {
					System.out.println("Looks like there are no menus yet.");
					this.builderMainMenu = newMenu();
				}
				else {
					SingleMenu chosenMenuToNew = chooseMenu(false);
					if (chosenMenuToNew != null)
						newMenu(chosenMenuToNew);
				}
				break;
			case 2:
				SingleMenu chosenMenuToAdd = chooseMenu(true);
				if (chosenMenuToAdd != null)
					addChoices(chosenMenuToAdd);
				break;
			}
		}
	}
	
	private void addChoices(SingleMenu menu) {
		do {
			
			String newOption = KeyboardInput.getString("Insert the text for the new choice (use #DONE# to finish)\n > ", "Error. Try again >");
			if ("#DONE#".equals(newOption))
				return;
			menu.addChoice(newOption, new SingleMenu());
			
		} while (true);
	}

	private void newMenu(SingleMenu menu) {
		String newName = KeyboardInput.getString("Type the title (= question) of the main menu\n > ", "Error. Try again > ");
		
		menu.setName(newName);
	}
	
	private SingleMenu newMenu() {
		String newName = KeyboardInput.getString("Type the title (= question) of the main menu\n > ", "Error. Try again > ");
		
		return new SingleMenu(newName);
	}
	
	private void addSubMenu(SingleMenu builderMainMenu, int choice) {
		
	}

	private SingleMenu chooseMenu(boolean avoidEmpty) {
		SingleMenu currentMenu = builderMainMenu;
		
		boolean cycling = true;
		
		do {
			System.out.println("Current menu:\n" + currentMenu.toString(true));
			
			
			SingleMenu options = new SingleMenu("Which menu would you like to choose?");
			
			options.startFromZero();
			options.addChoice("Exit");
			options.addChoice("This");
			
			SingleMenu subChoices = new SingleMenu("Which sub-menu?");
			
			subChoices.startFromZero();
			subChoices.addChoice("Exit");
			ArrayList<String> currChoices = currentMenu.getChoices();
			//ArrayList<SingleMenu> currSubMenus = currentMenu.getSubMenus();
			for (int i = 0; i < currChoices.size(); i++) {
//				subChoices.addChoice(currChoices.get(i), currSubMenus.get(i));
				subChoices.addChoice(currChoices.get(i), null);
			}
	
			options.addChoice("A sub-menu", subChoices);
			
			ArrayList<Integer> chosen = options.choose();
			
			switch (chosen.get(0)) {
			case 0:
				cycling = false;
				break;
			case 1:
				if (avoidEmpty && currentMenu.isEmpty())
					System.out.println("This menu hasn't been generated yet!");
				else if (!avoidEmpty && !currentMenu.isEmpty())
					System.out.println("This menu has already been generated!");
				else
					return currentMenu;
				break;
			case 2:
				int menuChoice = chosen.get(chosen.size() - 1);
				if (menuChoice != 0) {
					SingleMenu subMenuChoice = currentMenu.getSubMenu(menuChoice - 1); 
					if ((!avoidEmpty && subMenuChoice.isEmpty()) || (avoidEmpty && subMenuChoice.hasNoChoices())) {
						return subMenuChoice;
					}
					else
						currentMenu = subMenuChoice;
				}
				else
					cycling = false;
				break;
			}
		} while (cycling);
		return null;
	}

	public boolean init() {
		File f = new File(FILE_PATH);
		if (!f.exists()) {
			setMenu();
			return false;
		}
		
		XMLReader re = new XMLReader(FILE_PATH);
		re.init();
		this.megaMainMenu = re.readMenus();
		
		return (megaMainMenu != null);
	}
	
	public ArrayList<Integer> choose() {
		return megaMainMenu.choose();
	}

	private class SingleMenu {
		private String name;
		private ArrayList<String> choices;
		private ArrayList<SingleMenu> subMenus;
		private ArrayList<Integer> finalChoices;
		private boolean startFromZero = false;
		
		public SingleMenu() {
			this(null);
		}

		public void setName(String name) {
			this.name = name;
		}

		public SingleMenu(String name) {
			this(name, new ArrayList<String>(), new ArrayList<SingleMenu>());
		}
		
		public SingleMenu(String name, ArrayList<String> choices, ArrayList<SingleMenu> subMenus) {
			this.name = name;
			this.choices = new ArrayList<>(choices);
			this.subMenus = new ArrayList<>(subMenus);
			this.finalChoices = new ArrayList<>();
		}

		public boolean isEmpty() {
			return (name == null && hasNoChoices());
		}
		
		public boolean hasNoChoices() {
			return (choices.isEmpty() && subMenus.isEmpty());
		}
		
		public void startFromZero() {
			this.startFromZero = true;
		}
		
		public SingleMenu getSubMenu(int choice) {
			return subMenus.get(choice);
		}
		
		public ArrayList<SingleMenu> getSubMenus() {
			// TODO Auto-generated method stub
			return new ArrayList<>(subMenus);
		}

		public ArrayList<String> getChoices() {
			return new ArrayList<>(choices);
		}

		public void addChoice(String choice) {
			addChoice(choice, null);
		}
		public void addChoice(String choice, SingleMenu subMenu) {
			this.choices.add(choice);
			this.subMenus.add(subMenu);
		}
		
		public void setSubMenu(int choice, SingleMenu subMenu) {
			this.subMenus.set(choice, subMenu);
		}

		
		public ArrayList<Integer> choose() {
			ArrayList<Integer> out = new ArrayList<>();
			System.out.println(name);
			for (int i = 0; i < choices.size(); i++) {
				int index = (startFromZero ? i : i + 1);
				System.out.printf("%d) %s%n", index, choices.get(i));
			}
			
			int choice = KeyboardInput.getIntWithBounds("\nMake your choice > ", "Please insert a valid value > ", (startFromZero ? 0 : 1), choices.size() + (startFromZero ? -1 : 0));
			out.add(choice);
			
			SingleMenu nextMenu = subMenus.get(startFromZero ? choice : choice - 1);
			if (nextMenu != null && !nextMenu.isEmpty())
				out.addAll(nextMenu.choose());
			
			return out;
		}
		
		public String toString() {
			return toString(false);
		}
		
		public String toString(boolean indented) {
			StringBuffer out = new StringBuffer();
			if (indented)
				out.append("  ");
			out.append("-------------------------------\n");
			if (indented)
				out.append("  ");
			out.append(name);
			int cnt = 1;
			for (String c : choices) {
				out.append(System.lineSeparator());
				if (indented)
					out.append("    ");
				out.append(cnt + ") ");
				out.append(c);
				cnt++;
			}
			out.append(System.lineSeparator());
			if (indented)
				out.append("  ");
			out.append("-------------------------------\n");
			return out.toString();
		}
	}
	
	private class XMLReader {
		private XMLStreamReader reader;
		private XMLInputFactory factory;
		private InputStream is;
		private String path;
		
		public XMLReader(String path) {
			this.is = null;
			this.path = path;
		}

		public XMLReader(InputStream is) {
			this.is = is;
			this.path = null;
		}
		
		public boolean init() {
			try {
				factory = XMLInputFactory.newInstance();
				if (is == null)
					reader = factory.createXMLStreamReader(path, new FileInputStream(path));
				else 
					reader = factory.createXMLStreamReader(this.is);
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		
		public SingleMenu readMenus() {
			return readSingleMenu(true);
		}

		public SingleMenu readSingleMenu(boolean isFirst) {
			boolean subMenuAdded = false;
			boolean menuUnfinished = true;
			String currentName = (isFirst ? null : reader.getAttributeValue(0));
			ArrayList<String> currentChoices = new ArrayList<>();
			ArrayList<SingleMenu> currentSubMenus = new ArrayList<>();
			try {
				while (menuUnfinished) {
					switch (reader.next()) {
					case XMLStreamConstants.START_ELEMENT:
						if (MENU_TAG.equals(reader.getLocalName())) {
							if (reader.getAttributeCount() == 1 && MENU_TAG_ATTRIBUTE.equals(reader.getAttributeLocalName(0))) {
								if (isFirst) {
									currentName = reader.getAttributeValue(0);
									isFirst = false;
								}
								else {
									currentSubMenus.add(readSingleMenu(false));
									subMenuAdded = true;
								}
							}
							else
								return null;
						}
						else if (CHOICE_TAG.equals(reader.getLocalName())) {
							if (reader.getAttributeCount() == 1 && CHOICE_TAG_ATTRIBUTE.equals(reader.getAttributeLocalName(0)))
								currentChoices.add(reader.getAttributeValue(0));
							else
								return null;
						}
						break;
					case XMLStreamConstants.END_ELEMENT:
						if (MENU_TAG.equals(reader.getLocalName())) {
							menuUnfinished = false;
						}
						else if (CHOICE_TAG.equals(reader.getLocalName()) && !subMenuAdded) {
							currentSubMenus.add(null);
						}
						else if (subMenuAdded)
							subMenuAdded = false;
						break;
					}
				}
			} catch (Exception e) {
				return null;
			}
			SingleMenu menu = new SingleMenu(currentName, currentChoices, currentSubMenus);
			return menu;
		}
	}
	
	private static class KeyboardInput {
		private static Scanner s = new Scanner(System.in).useDelimiter(System.lineSeparator());
		
		public static String getString(String message, String wrongMessage) {
			boolean valid = true;
			String out = null;
			do {
				try {
					if (valid)
						System.out.print(message);
					else
						System.out.print(wrongMessage);
					out = s.next();
					valid = true;
				} catch (Exception e) {
					valid = false;
					s.next();
				}
			} while (!valid);
			
			return out;
		}
		
		public static boolean getConfirmation(String message, String wrongMessage, char yes, char no) {
			boolean valid = true;
			String outStr = null;
			char out = 0;
			do {
				try {
					if (valid)
						System.out.print(message);
					else
						System.out.print(wrongMessage);
					outStr = s.next();
					valid = true;
				} catch (Exception e) {
					valid = false;
					s.next();
				}
				out = Character.toLowerCase(outStr.charAt(0));
				if (outStr.length() != 1 || (out != yes && out != no))
					valid = false;
			} while (!valid);
			
			return out == yes;
		}
		
		// lower and upper inclusive
		public static int getIntWithBounds(String message, String wrongMessage, int lower, int upper) {
			boolean valid = true;
			int out = 0;
			do {
				try {
					if (valid)
						System.out.print(message);
					else
						System.out.print(wrongMessage);
					out = s.nextInt();
					valid = true;
				} catch (Exception e) {
					valid = false;
					s.next();
				}
				if (out < lower || out > upper)
					valid = false;
			} while (!valid);
			
			return out;
		}
	}

}
