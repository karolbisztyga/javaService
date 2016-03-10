package bb.service.sessionstorage;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CaptchaSessionStorage implements Serializable {
	
	public static final String STORAGE_TITLE = "captcha";
	
	private Long elapseTime;
	private String captcha;
	
	public CaptchaSessionStorage(int secondsDuration, String captcha) {
		this.elapseTime = System.currentTimeMillis()+secondsDuration*1000;
		this.captcha = captcha;
	}
	public Long getElapseTime() {
		return elapseTime;
	}
	public void setElapseTime(Long elapseTime) {
		this.elapseTime = elapseTime;
	}
	public String getCaptcha() {
		return captcha;
	}
	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}
	@Override
	public String toString() {
		return "CaptchaSessionStorage [elapseTime=" + elapseTime + ", captcha=" + captcha + "]";
	}
	
}
