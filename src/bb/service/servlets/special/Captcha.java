package bb.service.servlets.special;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;

import bb.service.sessionstorage.CaptchaSessionStorage;

@WebServlet(urlPatterns={"/captcha"})
public class Captcha extends HttpServlet {
	
	public enum STATUS {
		PASSED,
		INCORRECT,
		TIMED_OUT,
		NO_CAPTCHA
	};

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		Random random = new Random();
		
		int width = 150;
	    int height = 50;
	    int length = 5;

	    BufferedImage bufferedImage = new BufferedImage(width, height, 
	                  BufferedImage.TYPE_INT_RGB);

	    Graphics2D g2d = bufferedImage.createGraphics();

	    Font font = new Font("Courier New", Font.BOLD, 18);
	    g2d.setFont(font);

	    RenderingHints rh = new RenderingHints(
	           RenderingHints.KEY_ANTIALIASING,
	           RenderingHints.VALUE_ANTIALIAS_ON);

	    rh.put(RenderingHints.KEY_RENDERING, 
	           RenderingHints.VALUE_RENDER_QUALITY);

	    g2d.setRenderingHints(rh);
	    
	    g2d.setPaint(Color.white);
	    g2d.fillRect(0, 0, width, height);

	    String captcha = this.randomSigns(length, random);
	    request.getSession().setAttribute(CaptchaSessionStorage.STORAGE_TITLE, new CaptchaSessionStorage(60, captcha));

	    int x = 10; 
	    int y = 0;
	    
	    int linesNumber = random.nextInt(10)+5;
	    
	    for(int i=0 ; i<linesNumber ; ++i) {
	    	g2d.setColor(new Color(0, 0, 0, random.nextInt(100)));
	    	g2d.drawLine(random.nextInt(20), random.nextInt(height), random.nextInt(width-20), random.nextInt(height));
	    }

	    for (int i=0; i<length; i++) {
	    	AffineTransform orig = g2d.getTransform();
	        x += 10 + (int)(Math.random()*15);
	        y = 20 + (int)(Math.random()*10);
	        g2d.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
	        double mod = ((Math.random()*4)/100);
	        if((int)(mod*1000)%2==0) {
	        	mod = -mod;
	        }
	        g2d.rotate(Math.PI*mod);
	        g2d.drawChars(captcha.toCharArray(), i, 1, x, y);
	        g2d.setTransform(orig);
	    }

	    g2d.dispose();

	    response.setContentType("image/png");
	    OutputStream os = response.getOutputStream();
	    ImageIO.write(bufferedImage, "png", os);
	    os.close();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	public static Captcha.STATUS checkCaptcha(HttpServletRequest request, String captchaString) {
		try {
			CaptchaSessionStorage captcha = (CaptchaSessionStorage)request.getSession().getAttribute(CaptchaSessionStorage.STORAGE_TITLE);
			if(captcha.getElapseTime() < System.currentTimeMillis()) {
				return Captcha.STATUS.TIMED_OUT;
			}
			if(!captcha.getCaptcha().equals(captchaString)) {
				return Captcha.STATUS.INCORRECT;
			}
			return Captcha.STATUS.PASSED;
		} catch(NullPointerException e) {
			e.printStackTrace();
			return Captcha.STATUS.NO_CAPTCHA;
		}
	}
	
	public static String getStatusMessage(Captcha.STATUS status) {
		switch(status) {
			case NO_CAPTCHA: {
				return "there is no captcha";
			}
			case INCORRECT: {
				return "captcha is incorrect";
			}
			case TIMED_OUT: {
				return "captcha has timed out";
			}
		}
		return "";
	}
	
	private String randomSigns(int length, Random random) {
		final class Range {
			private final int from;
			private final int to;
			public Range(int from, int to) {
				this.from = from;
				this.to = to;
			}
			public int getFrom() {
				return from;
			}
			public int getTo() {
				return to;
			}
		}
		String s = "";
		List<Range> ranges = new ArrayList<Range>();
		ranges.add(new Range(48, 57));
		ranges.add(new Range(65, 90));
		ranges.add(new Range(97, 122));
		for(int i=0 ; i<length ; ++i) {
			int randomRange = random.nextInt(ranges.size());
			Range range = ranges.get(randomRange);
			int index = (random.nextInt((range.getTo()-range.getFrom()-1))+range.getFrom());
			s+= (char)index;
		}
		return s;
	}

}
