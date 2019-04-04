package com.jfeather.App;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.jfeather.App.Config.ColorType;

public class List {

	public static final String ACTIVE_INDICATOR = "!Active!";
	public static final String COMPLETED_INDICATOR = "!Completed!";
	public static final String GROUP_INDICATOR = "?";
	
	
	public static void write(String[] currentTasks, String[] completedTasks) {
		try {
			FileWriter fw = new FileWriter(Config.getFilePath(), false);
			BufferedWriter bw = new BufferedWriter(fw);
			
			// Simple disclaimer about editing
			bw.write("# The following file is generated by jTodo whenever it writes to the file, and user additions will most likely");
			bw.newLine();
			bw.write("# be overwritten on next write. To edit settings, use the config generator (todo --mkconfig) (WIP) or specific commands");
			bw.newLine();
			bw.write("# that can be found by using \"todo -h\".");
			bw.newLine();
			bw.newLine();
			
			bw.write("# Todo tasks:");
			bw.newLine();
			bw.write(ACTIVE_INDICATOR);
			bw.newLine();
			//System.out.println(arr.length);
			for (String s: currentTasks) {
				bw.write(s.toString());
				bw.newLine();
			}
			bw.write(ACTIVE_INDICATOR);
			
			/*
			 * Now we write out properties and config settings
			 */
			bw.newLine();
			bw.newLine();
			bw.write("# The config settings for jTodo");
			bw.newLine();
			bw.write(":enable_color=" + Config.isColorEnabled() + " # Possible values: true or 1 will enable, while false or 0 will disable");
			bw.newLine();
			
			bw.write("# Enabling groups will display the current tasks in group form (which is specified by todo -a <task> <-g <group>>");
			bw.newLine();
			bw.write(":enable_groups=" + Config.areGroupsEnabled() + " # Possible values: true or 1 will enable, while false or 0 will disable");
			bw.newLine();

			// This way we default to 8 if there is not real color type
			if (Config.getColorType() == ColorType.TYPE_256)
				bw.write(":color_type=256");
			else if (Config.getColorType() == ColorType.TYPE_16)
				bw.write(":color_type=16");
			else
				bw.write(":color_type=8");

			// Now we write the completed tasks
			bw.newLine();
			bw.newLine();
			bw.write(COMPLETED_INDICATOR);
			bw.newLine();
			
			if (completedTasks.length <= 10) {
				for (String s: completedTasks) {
					bw.write(s);
					bw.newLine();
				}
			} else {
				for (int i = 0; i < 10; i++) {
					bw.write(completedTasks[i]);
					bw.newLine();
				}
			}
			bw.write(COMPLETED_INDICATOR);
			
			bw.flush();
			bw.close();
		} catch (IOException ex) {
			if (Config.isColorEnabled())
				System.out.println(Color.errorColor() + "Error writing to file! \nFile \"" + Color.reset() + Color.ANSI_WHITE + Config.getFilePath() + Color.reset() + Color.errorColor() + "\" not found!");
			else
				System.out.println("Error writing to file! \nFile \"" + Config.getFilePath() + "\" not found!");
			
		} catch (Exception ex) {
			// Any other errors we should probably check out
			ex.printStackTrace();

		}
	}
	
	/**
	 * This method will return both the active and recently completed tasks in a two dimensional task array
	 * The active tasks will always be the first element and the completed will be the second (assuming it didn't catch and error)
	 * @return A 2D array of active and completed tasks in that order
	 */
	public static Task[][] read(boolean print) {
		
		try {
			FileReader fr = new FileReader(Config.getFilePath());
			BufferedReader br = new BufferedReader(fr);
			String line;
			ArrayList<String> lines = new ArrayList<>();
			ArrayList<String> completedLines = new ArrayList<>();
			
			// We need to keep track of whether we are reading the active or completed tasks
			boolean readingCompleted = false;
			boolean readingActive = false;
			
			// Iterate through every line
			while ((line = br.readLine()) != null) {
				
				// Since there will be an indicator before and after the todos, this should toggle properly
				if (line.equals(COMPLETED_INDICATOR)) {
					readingCompleted = !readingCompleted;
					continue;
				}
				
				if (line.equals(ACTIVE_INDICATOR)) {
					readingActive = !readingActive;
					continue;
				}

				if (line.length() > 0) {
					// This will ignore comments (#) and config properties (:)
					if (!line.trim().substring(0, 1).equals("#") && !line.trim().substring(0, 1).equals(":")) {
						
						// Make sure we properly sort whether a task is completed or not
						if (readingCompleted)
							completedLines.add(line);
						if (readingActive)
							lines.add(line);
					}
				}
			}
			
			Task[] arr = new Task[lines.size()];
			for (int i = 0; i < arr.length; i++) {
				arr[i] = new Task(lines.get(i));
			}
			
			Task[] completedArr = new Task[completedLines.size()];
			for (int i = 0; i < completedArr.length; i++) {
				completedArr[i] = new Task(completedLines.get(i));
			}
			
			br.close();
			
			return new Task[][] {arr, completedArr};
			
		} catch (IOException ex) {
			if (print) {
				if (Config.isColorEnabled())
					System.out.println(Color.errorColor() + "Error reading file! \nFile \"" + Color.reset() + Color.ANSI_WHITE + Config.getFilePath() + Color.reset() + Color.errorColor() + "\" not found!");
				else
					System.out.println("Error reading file! \nFile \"" + Config.getFilePath() + "\" not found!");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
		
	public static void print(Calendar date) {
		/*
		 * This will be relatively simple, but most of the complexity will be due to the different options to print (color, non-color, color-bydate, etc.)
		 */
		
		Task[] tasks = List.read(true)[0];
		if (tasks != null) {
			if (tasks.length == 0) {
				
				if (Config.isColorEnabled())
					System.out.println(Color.ANSI_BLUE + "Nothing to do... :)" + Color.reset());
				else
					System.out.println("Nothing to do... :)");

			} else {
				
				if (!Config.areGroupsEnabled()) {
					int i = 0;
					int indentSpaces = ("" + tasks.length).length();
					
					if (Config.isColorEnabled())
						System.out.println(Color.titleColor() + "******* TODO LIST *******" + Color.reset());
					else
						System.out.println("******* TODO LIST *******");
	
					for (Task t: tasks) {
						
						t.printTask(i++, date.get(Calendar.DAY_OF_YEAR), indentSpaces);
											
					}
					System.out.println();
				
				} else {
					// Groups
					
					HashMap<String, ArrayList<Task>> taskMap = sortByGroups(tasks);
					
					if (!Config.isColorEnabled()) {
						//System.out.println(taskMap.size());
						// Now iterate through every group and display tasks
						int j = 0;
						for (Map.Entry<String, ArrayList<Task>> entry: taskMap.entrySet()) {
							// Print out the group name
							System.out.println(j++ + ". ** " + entry.getKey().toUpperCase() + " **");
							
							int i = 0;
							int indentSpaces = ("" + entry.getValue().size()).length();
							
							//System.out.println(entry.getValue().size());
							
							for (Task t: entry.getValue()) {
								System.out.print("    ");
								t.printTask(i++, date.get(Calendar.DAY_OF_YEAR), indentSpaces);
							}
						}
					} else {
						// Color
						//System.out.println(taskMap.size());
						// Now iterate through every group and display tasks
						int j = 0;
						for (Map.Entry<String, ArrayList<Task>> entry: taskMap.entrySet()) {
										
							// Fetch our colors for each entry
							int[] dueDates = new int[entry.getValue().size()];
							for (int i = 0; i < dueDates.length; i++) {
								dueDates[i] = entry.getValue().get(i).getYearDayDue();
							}
							int[] colors = Color.relativeRankDueDates(dueDates);
							// Print out the group name
							System.out.println(Color.titleColor() + j++ + ". ** " + entry.getKey().toUpperCase() + " **" + Color.reset());
							
							int i = 0;
							int indentSpaces = ("" + entry.getValue().size()).length();
							
							//System.out.println(entry.getValue().size());
							
							for (Task t: entry.getValue()) {
								System.out.print(Color.colorFromInt(Color.ANSI_URGENCY_GROUPED[(j-1)%5][colors[i]]) + "    ");
								t.printTask(i++, date.get(Calendar.DAY_OF_YEAR), indentSpaces);
								System.out.print(Color.reset());
							}
						}
					}
				}
			}
		}
	}
	
	public static HashMap<String, ArrayList<Task>> sortByGroups(Task[] tasks) {
		// First we want to find how many groups we have and sort each task into its category
		HashMap<String, ArrayList<Task>> taskMap = new HashMap<>();
		
		for (int i = 0; i < tasks.length; i++) {
			// Grab the map, in case there are other tasks that were already put in the group
			ArrayList<Task> tasksInThisGroup = taskMap.get(tasks[i].getGroup().toLowerCase());
			
			//System.out.println(tasks[i]);
			
			// If there weren't any, we create the list
			if (tasksInThisGroup == null)
				tasksInThisGroup = new ArrayList<>();
			
			// Add the task to the list and put it back in the map
			tasksInThisGroup.add(tasks[i]);
			taskMap.put(tasks[i].getGroup().toLowerCase(), tasksInThisGroup);
			
			//System.out.println(tasksInThisGroup);
		}
		
		return taskMap;

	}
	
	public static void printCompleted(Calendar date) {
		
		// Read in the completed tasks (note the 1 index, as opposed to 0 in most other places)
		Task[] tasks = List.read(true)[1];
		
		// If we have no tasks
		if (tasks.length == 0) {
			System.out.println("No completed tasks...");
			System.out.println("Better get to work!");
		} else {
			// Otherwise just print out the tasks
			// No support for color in this part yet, working on overhauling the color system as of now (update 1.9)
			System.out.println("******* Completed Tasks *******");

			for (int j = tasks.length - 1; j > 0; j--) {
				tasks[j].printCompletedTask((tasks.length - j + 1), date.get(Calendar.DAY_OF_YEAR));
			}
		}
	}
	
	public static String[] taskToStringArr(Task[] arr) {
		String[] strArr = new String[arr.length];
		int i = 0;
		for (Task t: arr) {
			strArr[i++] = t.toString();
		}
		
		return strArr;
	}


}
