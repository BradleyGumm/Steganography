package net.myserverset;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import javax.imageio.ImageIO;
/* To do this we must:
 * Encode the character into ascii
 * Separate each digit. So for example: 
 * If we wanted to put the 'A' character into the pixel located at 0,0
 * 'A' is 065 in ascii. We will break that up into a 0 , 6, and 5
 * The 0 will be added to the Red value
 * 6 will be added to the Green and 5 will be added to the blue.
 * 
 * In order to add the ascii to the pixel, we will drop the last digit of the given color to a 0 
 * and add the corelating ascii digit to the color.
 * 
 * 
 * So if a color of pixel (0,0) is a grey and we want to hide 'A' in it we do the following: 
 * basic grey is RGB(128,128,128)
 * drop the last digit of each color RGB(120, 120, 120)
 * then add each digit of the ascii value of 'A' (065) to the color like this
 * RGB(120,126,125)
 */
public class Steganography {

	public static void main(String args[])
	{
		createSteganography("D:/images.png", "D:/output.png", "Hide this text!");
		System.out.println(readSteganography("D:/output.png"));
	}
	
	//Use createStegenography(path_to_original, path_to_output, text_to_hide_in_image)
	public static void createSteganography(String ogpath, String outputPath, String contents)
	{
		String toHide = contents;
		String toHideInIntegers = "";
		for(int a = 0; a < toHide.length(); a++)
		{
			String tmp = (int)toHide.charAt(a) + "";

			if(tmp.length() == 2)
			{
				tmp = "0"+tmp;
			}
			else if(tmp.length() == 1)
			{
				tmp = "00"+tmp;
			}
			toHideInIntegers += tmp;
		}
		toHideInIntegers += "127"; //Chose the ascii character 127 because it is just the DEL key and wouldn't be included in actual text.
		String ogImageLoc = ogpath;
		String newImageLoc = outputPath;
		File src = new File(ogImageLoc);
		int place = 0;
		try {
			BufferedImage img = ImageIO.read(src);
			BufferedImage output = new BufferedImage(img.getWidth(), img.getHeight() , BufferedImage.TYPE_INT_RGB);
				int r = 0;
				int g = 0;
				int b = 0;
				boolean test = false;
				for(int i = 0; i < img.getHeight(); i++)
				//for(int i = 0; i < 1; i++)
				{
					for(int j =0; j < img.getWidth(); j++)
					//for(int j =0; j < 1; j++)
					{
						Color c = new Color(img.getRGB(j,i));
						if(place < toHide.length())
						{
							String thisPixel = (int)toHide.charAt(place)+"";
							
							//Each pixel is a character
							//Make each pixel three digits to standardize the process of working it into the picture.
							if(thisPixel.length() == 2)
							{
								thisPixel = "0"+thisPixel;
							}
							else if(thisPixel.length() == 1)
							{
								thisPixel = "00"+thisPixel;
							}
							
							//Make sure that adding the specified ascii value to the color wont make it go above 255 (the upper limit of RGB)
							if(c.getRed()+Integer.parseInt(thisPixel.charAt(0)+"") <= 255 && c.getGreen()+Integer.parseInt(thisPixel.charAt(1)+"") <= 255 && c.getBlue()+Integer.parseInt(thisPixel.charAt(2)+"") <= 255)
							{
								r = c.getRed() / 10; //In the example at the beggining, r = 12 because int drops the decimal.
								String sr = r + "0"; //create a string to put a 0 at the end of the r value making it 120.
								r = Integer.parseInt(sr); //Convert the string to int.
								r = r + Integer.parseInt(thisPixel.charAt(0)+""); //add the first digit to the r value making it 120.

								g = c.getGreen() / 10;
								String sg = g + "0";
								g = Integer.parseInt(sg);
								g = g + Integer.parseInt(thisPixel.charAt(1)+"");

								b = c.getBlue() / 10;
								String sb = b + "0";
								b = Integer.parseInt(sb);
								b = b + Integer.parseInt(thisPixel.charAt(2)+"");
								place++; //counts the chars that have been put into the image from the 'contents' parameter.
							}
							else
							{
								r = c.getRed() / 10; //this runs when any value of RGB is greater than 255.
								String sr = r + "3"; //This will add three to the end of the red value to signify 
													 //to the 'readSteganography' that this pixel doesn't contain a character.
								r = Integer.parseInt(sr);
								//System.out.println("template R: " + c.getRed() + " G: " + c.getGreen() + " B: " +  c.getBlue());
								//System.out.println(thisPixel.charAt(0)+ " "+ thisPixel.charAt(1)+ " " + thisPixel.charAt(2));
								g = c.getGreen();
								b = c.getBlue();
							}
							Color newC = new Color(r,g,b);

							output.setRGB(j,i,newC.getRGB()); // Adds the new RGB setting to the pixel on the new image.
						}
						else if(place == toHide.length() && !test) //If this is then end of the string
																   //, add 127 to the next pixel to signify the end of text
						{
							test = true;
							c = new Color(img.getRGB(j,i));
							r = c.getRed() / 10;
							String sr = r + "0";
							r = Integer.parseInt(sr);
							r = r + Integer.parseInt(1+"");

							g = c.getRed() / 10;
							String sg = g + "0";
							g = Integer.parseInt(sg);
							g = g + Integer.parseInt(2+"");

							b = c.getRed() / 10;
							String sb = b + "0";
							b = Integer.parseInt(sb);
							b = b + Integer.parseInt(7+"");

							Color newC = new Color(r,g,b);

							output.setRGB(j,i,newC.getRGB());
						}
						else
						{
							r = c.getRed() / 10; //this runs for every pixel after the 127 signal (end of string) pixel.
							String sr = r + "0";
							r = Integer.parseInt(sr);
							r = r + Integer.parseInt(3+""); //adding 3 to the red pixel makes the reader skip the pixel.
															//3 was chosen because the highest that could be added to any 
															//pixel is 255 and since the first digit wont be higher than
															//a 2 if we add an actual character in the ascii charset, it 
															//will never be used unless we want to skip the pixel.
							Color newC = new Color(r,c.getGreen(),c.getBlue());

							output.setRGB(j,i,newC.getRGB());
						}
					}
				}
			ImageIO.write(output, "png", new File(newImageLoc));
			//System.out.println("Wrote image");
		}
		catch(Exception a){
			a.printStackTrace();
		}
	}
	
	public static String readSteganography(String inputPath)
	{
		String result = "";
		
		try {
			BufferedImage img = ImageIO.read(new File(inputPath));
			for(int i = 0; i < img.getHeight(); i++)
				//for(int i = 0; i < 1; i++)
				{
					for(int j =0; j < img.getWidth(); j++)
					//for(int j =0; j < 1; j++)
					{
						Color c = new Color(img.getRGB(j,i));
						int char1 = c.getRed() - Integer.parseInt((c.getRed() / 10)+"0"); // substracts the value read in the 
																						  //Red color from the value with the
																						  //last digit replace with a 0 
						int char2 = c.getGreen() - Integer.parseInt((c.getGreen() / 10)+"0");
						int char3 = c.getBlue() - Integer.parseInt((c.getBlue() / 10)+"0");
						int allPutTogether = Integer.parseInt(char1+""+char2+""+char3); //if you followed the example in the comments
																						//this creates the ascii value 
																						//in the example, this would be turning 065 
																						//into an int and back to a char
						if(char1 != 3) //if it doesnt contain the skip signal in the red value, continue.
						{
							if(allPutTogether != 127) //If the pixel didnt contain the 127 (end of text) signal, continue.
							{
								result += (char)allPutTogether; //This the pixels character gets turned back into a char and put into
																//a storage string.
							}
						}
						
					}	
				}
		}
		catch(Exception e)
		{
			
		}
		return result;
	}
}