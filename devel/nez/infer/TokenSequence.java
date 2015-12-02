package nez.infer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TokenSequence {
	protected Map<String, Token> tokenMap;

	public TokenSequence() {
		this.tokenMap = new HashMap<String, Token>();
	}

	public Map<String, Token> getTokenMap() {
		return tokenMap;
	}

	public final void transaction(String label) {
		if (!tokenMap.containsKey(label)) {
			Token token = new Token(label);
			token.getHistogram().update();
			tokenMap.put(label, token);
		} else {
			tokenMap.get(label).getHistogram().update();
		}
	}

	public final Token[] getTokenList() {
		return (Token[]) this.tokenMap.values().toArray();
	}

	public final void commitAllHistograms() {
		for (Entry<String, Token> token : this.tokenMap.entrySet()) {
			token.getValue().getHistogram().commit();
		}
	}

	public final void normalizeAllHistograms() {
		for (Entry<String, Token> token : this.tokenMap.entrySet()) {
			token.getValue().getHistogram().normalize();
		}
	}

}
