package classes;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class Heatmap 
{
	public void create(String filename, String path, String newDirectoryname, int AUFLOESUNG_SLIDER, ImagePlus heatmapTmp, ImageProcessor heatmapMaske, ImageProcessor heatmap_ip, int w, int h)
	{
		//Dichte berechnen:		
  		float radiusFloat = (float) (w * 0.0002 * AUFLOESUNG_SLIDER);		//w/(w/aufloesung);
  		int radius = Math.round(radiusFloat);
  		int max = 0;
  		int suchsetzRadius = w / 400;
  		for (int x = 0; x <= w; x=x+suchsetzRadius) 
  		{
  			if (x == 0)
  			{
  				IJ.showProgress(0.01);	
  			}
  			else if (x < (w/100*10))
  			{
  				IJ.showProgress(0.1);		
  			}
  			else if (x < (w/100*20))
  			{
  				IJ.showProgress(0.2);		
  			}
  			else if (x < (w/100*30))
  			{
  				IJ.showProgress(0.3);		
  			}
  			else if (x < (w/100*40))
  			{
  				IJ.showProgress(0.4);		
  			}
  			else if (x < (w/100*50))
  			{
  				IJ.showProgress(0.5);		
  			}
  			else if (x < (w/100*60))
  			{
  				IJ.showProgress(0.6);		
  			}
  			else if (x < (w/100*70))
  			{
  				IJ.showProgress(0.7);		
  			}
  			else if (x < (w/100*80))
  			{
  				IJ.showProgress(0.8);		
  			}
  			else if (x < (w/100*90))
  			{
  				IJ.showProgress(0.9);		
  			}
  			else if (x < (w/100*95))
  			{
  				IJ.showProgress(0.95);		
  			}
  			
  			for (int y = 0; y <= h; y=y+suchsetzRadius) 
  			{
  				// in Radius Umgebung nach roten Pixeln suchen
  				int i, j, sum = 0, abgesuchtepixel=0;
  				float dichte;
  				for (j=-radius; j<=radius; j++)
  				{
  					for (i=-radius; i<=radius; i++)
  					{
  						if ( ((x+j) >= 0) && ((y+i) >= 0) )		//regel dass wir nicht im negativen bereich suchen
  						{
  							abgesuchtepixel++;
  							int c = heatmapMaske.getPixel(x+j, y+i);
  							if ( c == -16777216)				//-2420452
  							{
  								sum++;							//gefundene Pixel zaehlen
  							}
  						}
  					}
  				}				
  				//alles abgesucht, sum ist gesetzt
  				if (sum == 0)
  				{
  					dichte = 0;			    
  				    for (j=-suchsetzRadius; j<=suchsetzRadius; j++)
    				{
    					for (i=-suchsetzRadius; i<=suchsetzRadius; i++)
    					{
    						if ( ((x+j) >= 0) && ((y+i) >= 0)   )
      						{
    							heatmap_ip.putPixel(x+j, y+i, 0);
      						}
    					}
    				}		    
  				}
  				else
  				{
  					dichte = 255 / (abgesuchtepixel / sum); 					
  					if (dichte > max)
  					{
  						max = (int) dichte;
  					}
  					//float dichte zu int tmp
  					int tmp = Math.round(dichte); 					
  				    for (j=-suchsetzRadius; j<=suchsetzRadius; j++)
  				    {
  				    	for (i=-suchsetzRadius; i<=suchsetzRadius; i++)
  				    	{
  				    		if ( ((x+j) >= 0) && ((y+i) >= 0)   )
  				    		{
  				    			heatmap_ip.putPixel(x+j, y+i, tmp);
  				    		}
  				    	}
  				    }
  				}
  			}
  		}
  		heatmapTmp.show();
  		
  		IJ.run(heatmapTmp, "Fire", "");
  		float faktorfloat = (255 / max);
  		String befehl = "value=%";
  		String value = String.valueOf(faktorfloat);
  		
  		IJ.run(heatmapTmp, "Multiply...", befehl.replace("%", value));
  		IJ.run(heatmapTmp, "Fire", "");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Speichern der Heatmap
  		String resultsFilename = filename.replaceFirst("[.][^.]+$", "") + "_Heatmap.jpg";		//Neuen Filenamen festlegen
  		String exportHeatmap = path + newDirectoryname + "\\" + resultsFilename;
		
	    IJ.saveAs(heatmapTmp, "Jpeg", exportHeatmap);
	    System.out.print("\n\nHeatmap exportiert...\n");
	    System.out.print(exportHeatmap);
  		
  		//heatmapTmp.changes = false;
  		heatmapTmp.close();
  		
  		System.out.print("\n\nHeatmap created...");
  		
  		return;
	}
	

}
