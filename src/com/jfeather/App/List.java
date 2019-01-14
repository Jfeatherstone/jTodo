package com.jfeather.App;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class List {

	public static void write(String[] arr) {
		try {
			FileWriter fw = new FileWriter(Config.getFilePath(), false);
			BufferedWriter bw = new BufferedWriter(fw);
			
			// Simple disclaimer about editing
			bw.write("# The following file is generated by JTodo whenever it writes to the file, and user additions will most likely");
			bw.newLine();
			bw.write("# be overwritten on next write. To edit settings, use the config generator (todo --mkconfig) (WIP) or specific commands");
			bw.newLine();
			bw.write("# that can be found by using \"todo -h\".");
			bw.newLine();
			bw.newLine();
			
			bw.write("# Todo tasks:");
			bw.newLine();
			//System.out.println(arr.length);
			for (String s: arr) {
				bw.write(s.toString());
				bw.newLine();
			}
			
			
			/*
			 * Now we write out properties and config settings
			 */
			bw.newLine();
			bw.write("# The config settings for JTodo");
			bw.newLine();
			bw.write(":enable_color=" + Config.isColorEnabled() + " # Possible values: true or 1 will enable, while false or 0 will disable");
			
			bw.flush();
			bw.close();
		} catch (Exception ex) {
			if (Config.isColorEnabled())
				System.out.println(Color.errorColor() + "Error writing to file! \nFile \"" + Color.reset() + Color.ANSI_WHITE + Config.getFilePath() + Color.reset() + Color.errorColor() + "\" not found!");
			else
				System.out.println("Error writing to file! \nFile \"" + Config.getFilePath() + "\" not found!");
			//ex.printStackTrace();
		}
	}
	
	public static Task[] read(boolean print) {
		
		try {
			FileReader fr = new FileReader(Config.getFilePath());
			BufferedReader br = new BufferedReader(fr);
			String line;
			ArrayList<String> lines = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				// This will ignore comments (#) and config properties (:)
				if (line.length() > 0) {
					if (!line.trim().substring(0, 1).equals("#") && !line.trim().substring(0, 1).equals(":")) {
						lines.add(line);
					}
				}
			}
			
			Task[] arr = new Task[lines.size()];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = new Task(lines.get(i));
			}
			
			br.close();
			return arr;
		} catch (IOException ex) {
			if (print) {
				if (Config.isColorEnabled())
					System.out.println(Color.errorColor() + "Error reading file! \nFile \"" + Color.reset() + Color.ANSI_WHITE + Config.getFilePath() + Color.reset() + Color.errorColor() + "\" not found!");
				else
					System.out.println("Error reading file! \nFile \"" + Config.getFilePath() + "\" not found!");
			}
		} catch (Exception ex) {
			//ex.printStackTrace();
		}
		return null;
	}
	
	public static void print(Calendar date) {
		/*
		 * This will be relatively simple, but most of the complexity will be due to the different options to print (color, non-color, color-bydate, etc.)
		 */
		
		Task[] tasks = List.read(true);
		if (tasks != null) {
			if (tasks.length == 0) {
				
				if (Config.isColorEnabled())
					System.out.println(Color.ANSI_BLUE + "Nothing to do... :)" + Color.reset());
				else
					System.out.println("Nothing to do... :)");

			} else {
				int i = 0;
				
				if (Config.isColorEnabled())
					System.out.println(Color.ANSI_256_TEST + "******* TODO LIST *******" + Color.reset());
				else
					System.out.println("******* TODO LIST *******");

				for (Task t: tasks) {
					
					t.printTask(i, date.get(Calendar.DAY_OF_YEAR));
										
					i++;
				}
				System.out.println();
			}
		}
	}


}
