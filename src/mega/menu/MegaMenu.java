package mega.menu;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;



public class MegaMenu {
	private static final String FILE_PATH = "menu/choices.xml";
	
	private static final String MENU_TAG = "menu";
	public static final Object MENU_TAG_ATTRIBUTE = "name";	
	private static final String CHOICE_TAG = "choice";
	public static final Object CHOICE_TAG_ATTRIBUTE = "value";	

	private SingleMenu megaMainMenu;
	ArrayList<Integer> chosen = new ArrayList<>();
	
	
	public MegaMenu() {
		
	}
	
	public boolean init() {
		File f = new File(FILE_PATH);
		if (!f.exists()) {
			System.out.println(new File("").getAbsolutePath());
			System.out.println(f.getAbsolutePath());
			return false;
		}
		
		XMLReader re = new XMLReader(FILE_PATH);
		re.init();
		this.megaMainMenu = re.readMenus();
		
		return (megaMainMenu != null);
	}
	
	public void choose() {
		megaMainMenu.choose();
	}
	
	public ArrayList<Integer> choices() {
		return new ArrayList<Integer>(chosen);
	}

	private class SingleMenu {
		String name;
		ArrayList<String> choices;
		ArrayList<SingleMenu> subMenus;
		
		public SingleMenu(String name, ArrayList<String> choices, ArrayList<SingleMenu> subMenus) {
			this.name = name;
			this.choices = new ArrayList<>(choices);
			this.subMenus = new ArrayList<>(subMenus);
		}
		
		public void choose() {
			System.out.println(name);
			for (int i = 1; i <= choices.size(); i++) {
				System.out.printf("%d) %s%n", i, choices.get(i - 1));
			}
			
			
			int choice = 0;
			boolean valid = true;
			do {
				Scanner s = new Scanner(System.in);
				try {
					if (!valid)
						System.out.print("Please insert a valid value > ");
					else
						System.out.print("\nMake your choice > ");
					choice = s.nextInt();
					valid = true;
				} catch (Exception e) {
					valid = false;
				}
				if (choice < 1 || choice > choices.size())
					valid = false;
			} while (!valid);
			
			chosen.add(choice);
			
			SingleMenu nextMenu = subMenus.get(choice - 1);
			if (nextMenu != null)
				nextMenu.choose();
			
		}
	}
	
	private class XMLReader {
		private XMLStreamReader reader;
		private XMLInputFactory factory;
		private String path;
		
		public XMLReader(String path) {
			this.path = path;
		}


		public boolean init() {
			try {
				factory = XMLInputFactory.newInstance();
				reader = factory.createXMLStreamReader(path, new FileInputStream(path));
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

}
