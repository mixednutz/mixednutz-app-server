/**
 * 
 */
package net.mixednutz.app.server.manager.impl;

import java.util.Random;

import org.joda.time.Instant;
import org.springframework.stereotype.Component;

import net.mixednutz.app.server.manager.TokenGenerator;

/**
 * Default class that generates a unique key using given variable
 * length of the key.
 * 
 * @author Andy
 *
 */
@Component
public class DefaultKeyGenerator implements TokenGenerator {
	
	private Random rn;
	
	public static final int MIN_KEY_LENGTH = 10;
	public static final int MAX_KEY_LENGTH = 25;
	
	private int minKeyLength = MIN_KEY_LENGTH;
	private int maxKeyLength = MAX_KEY_LENGTH;
	
	
	/**
	 * Default Constructor that uses the current time as a seed for the 
	 * randomizer.
	 */
	public DefaultKeyGenerator() {
		this(Instant.now().getMillis());
	}
	
	public DefaultKeyGenerator(long seed) {
		rn = new Random(seed);
	}

	/* (non-Javadoc)
	 * @see net.mixednutz.core.manager.InviteKeyGenerator#generate()
	 */
	public String generate() {
		return randomString(minKeyLength, maxKeyLength);
	}
	
	private int rand(int lo, int hi) {
		int n = hi - lo +1;
		int i = rn.nextInt() % n;
		if (i<0) {
			i = -i;
		}
		return lo+i;
	}
	
	protected String randomString(int minLen, int maxLen) {
		int len = rand(minLen, maxLen);
		byte[] b = new byte[len];
		for (int i=0; i<len; i++) {
			int type = rand(0,2);
			switch (type) {
			case 0:
				b[i] = (byte)rand('a','z');
				break;
			case 1:
				b[i] = (byte)rand('A','Z');
				break;
			case 2:
				b[i] = (byte)rand('0','9');
				break;
			}
		}
		return new String(b);
	}

	public int getMinKeyLength() {
		return minKeyLength;
	}

	public void setMinKeyLength(int minKeyLength) {
		this.minKeyLength = minKeyLength;
	}

	public int getMaxKeyLength() {
		return maxKeyLength;
	}

	public void setMaxKeyLength(int maxKeyLength) {
		this.maxKeyLength = maxKeyLength;
	}

}
