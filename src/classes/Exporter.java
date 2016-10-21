package classes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import ij.IJ;
import ij.ImagePlus;

/**
 * Exporter class for the NucleiJ Analyzer
 *
 * @author Stefan Erben
 * @version 1.0
 *
 */

public class Exporter
{
	public String newDirectoryname = null;

	//setValue
	public void setnewDirectoryname(String uebergebenerString)
	{
		newDirectoryname = uebergebenerString;

	}

	//getValue
	public String getnewDirectoryname()
	{
		return newDirectoryname;

	}


	public void csvSummary (String resultzeile, String path)
	{
		String exportReport = path + newDirectoryname + "\\" + "Results.csv";

		File reportfile = new File(exportReport);
		try {
			reportfile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(reportfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedWriter writeSummary = new BufferedWriter(fw);

		try {
			writeSummary.write(resultzeile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//writeSummary.flush();

		try {
			writeSummary.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			writeSummary.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}




	/**
	 * The results method creates and exports a textfile (.txt), which contains the analyzed results for the
	 * current Scan.
	 *
	 * @param resultzeile	String, which contains the return value of the method getValue() from the object
	 *                         resultStack. The String represents the hole Results of the analyzed Scan.
	 * @see StringAdder
	 *
	 * @param filename		String, which contains the return value of the getValue Method from the Object file
	 * @param path 			String, which String, which contains the return value of the getValue Method from
	 *                         the Object path
	 * @see StringTransfer
	 */
	public void results(String resultzeile, String filename, String path)
	{

		// In Textdatei exportieren
		String ReportFilename = filename.replaceFirst("[.][^.]+$", "") + "_Results.txt";		//Neuen Filenamen festlegen
    	
    	String exportReport = path + newDirectoryname + "\\" + ReportFilename;
     	
     	File reportfile = new File(exportReport);
        try {
			reportfile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        FileWriter fw = null;
		try {
			fw = new FileWriter(reportfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        BufferedWriter writeSummary = new BufferedWriter(fw);
        try {
        	writeSummary.write("Results fuer den Scan: " + filename.replaceFirst("[.][^.]+$", "") + "\n\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			writeSummary.write(resultzeile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //writeSummary.flush();

        try {
			writeSummary.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			writeSummary.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * The summary method creates and exports a textfile (.txt), which contains and exports the summary of the
	 * analyzed results for the current Scan.
	 *
	 * @param summaryStack	String, which contains the return value of the method getString() from the object
	 *                         summaryStack.
	 * @see StringAdder
	 *
	 * @param path			String, which contains the return value of the method getValue() from the object path
	 * @see StringTransfer
	 *
	 * @param todayTimeStamp String, which contains the return value of the method getCurrentTimeStamp() from the
	 *                       object today.
	 * @see Timestamp
	 */
	public void summary(String summaryStack, String path, String todayTimeStamp)
	{
		System.out.println();
		
		if (summaryStack != null)			//Error Handling
		{
			String logInhalt = summaryStack;
			System.out.println("\n" +logInhalt +"\n");
			
			// In Textdatei exportieren
			String ReportFilename = "Summary-Report.txt";		//Neuen Filenamen festlegen
	    	
	    	String exportReport = path + newDirectoryname + "\\" + ReportFilename;
	     	
	     	File file = new File(exportReport);
	        try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        FileWriter fw = null;
			try {
				fw = new FileWriter(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        BufferedWriter writeSummary = new BufferedWriter(fw);
	        try {		
	        	writeSummary.write("Programm von Stefan Erben und Andreas Mattes. (c) 2016\nSummary erstellt am: " + todayTimeStamp + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				writeSummary.write(logInhalt);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        //writeSummary.flush();

	        try {
				writeSummary.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        try {
				writeSummary.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        //bw.close();
			
		}
		else
		{
			System.out.println("ERROR. Kein IJ Log vorhanden!");
		}
		
		
	}

	/**
	 * The heatmap method creates and exports a textfile (.txt), which exports the heatmap
	 * for the current Scan.
	 *
	 * @param imp					Object from the ImagePlus class, which contains the created 8bit Heatmap for the
	 *                              	currently analyzed Scan.
	 * @see ImagePlus
	 *
	 * @param filename 				String, which contains the return value of the method getValue() from the object
	 *                         			file.
	 * @see StringTransfer
	 *
	 * @param path 					String, which contains the return value of the method getValue() from the object
	 *                         			path.
	 * @see StringTransfer
	 *
	 * @param EXPORT_PIC_CHECKBOX	boolean value, if true, the marked Scan will be exported, otherwise not
	 */
	public void marked(ImagePlus imp, String filename, String path, String getnewDirectoryname, boolean EXPORT_PIC_CHECKBOX)
	{
		if (EXPORT_PIC_CHECKBOX == true)
		{
			filename = filename.replaceFirst("[.][^.]+$", "") + "_Marked.tif";	//Neuen Filenamen festlegen
	    	
	    	String exportEndpic = path + newDirectoryname + "\\" + filename;

		    IJ.saveAs(imp, "Tif", exportEndpic);
		    System.out.print("\n\nMarkierter Schnitt exportiert: " + exportEndpic + "\n");
		    System.out.print(filename);
		}
	    return;
	}
	
	
}
