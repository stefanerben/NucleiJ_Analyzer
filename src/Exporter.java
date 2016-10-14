import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ij.IJ;
import ij.ImagePlus;

public class Exporter 
{
	public void Results(String resultzeile, String filename, String path)
	{

		// In Textdatei exportieren
		String ReportFilename = filename.replaceFirst("[.][^.]+$", "") + "_Results.txt";		//Neuen Filenamen festlegen
    	
    	String exportReport = path + "\\" + ReportFilename;
     	
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
	
	
	
	public void summary(String summaryStack, String path, String todayTimeStamp)
	{
		System.out.println();
		
		if (summaryStack != null)			//Error Handling
		{
			String logInhalt = summaryStack;
			System.out.println("\nInhalt des Logfensters:\n" +logInhalt +"\n");
			
			// In Textdatei exportieren
			String ReportFilename = "Summary-Report.txt";		//Neuen Filenamen festlegen
	    	
	    	String exportReport = path + "\\" + ReportFilename;
	     	
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
	
	
	public void heatmap(ImagePlus imp, String filename, String path, boolean EXPORT_PIC_CHECKBOX)
	{
		if (EXPORT_PIC_CHECKBOX == true)
		{
			
			filename = filename.replaceFirst("[.][^.]+$", "") + "_Marked.jpg";		//Neuen Filenamen festlegen
	    	
	    	String exportEndpic = path + "\\" + filename;

		    IJ.saveAs(imp, "Jpeg", exportEndpic);
		    System.out.print("\n\nMarkierter Schnitt exportiert: " + exportEndpic + "\n");
		    System.out.print(filename);
		}
		
	    return;
	}
	
	
}
