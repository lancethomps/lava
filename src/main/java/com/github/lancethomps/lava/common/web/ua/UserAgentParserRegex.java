package com.github.lancethomps.lava.common.web.ua;

/**
 * The Class UserAgentRegex.
 *
 * @author lathomps
 */
public class UserAgentParserRegex extends AbstractUserAgentParserRegex {

	/** The family replacement. */
	private String familyReplacement;

	/** The v 1 replacement. */
	private String v1Replacement;

	/** The v 2 replacement. */
	private String v2Replacement;

	/** The v 3 replacement. */
	private String v3Replacement;

	/**
	 * @return the familyReplacement
	 */
	public String getFamilyReplacement() {
		return familyReplacement;
	}

	/**
	 * @return the v1Replacement
	 */
	public String getV1Replacement() {
		return v1Replacement;
	}

	/**
	 * @return the v2Replacement
	 */
	public String getV2Replacement() {
		return v2Replacement;
	}

	/**
	 * @return the v3Replacement
	 */
	public String getV3Replacement() {
		return v3Replacement;
	}

	/**
	 * @param familyReplacement the familyReplacement to set
	 */
	public void setFamilyReplacement(String familyReplacement) {
		this.familyReplacement = familyReplacement;
	}

	/**
	 * @param v1Replacement the v1Replacement to set
	 */
	public void setV1Replacement(String v1Replacement) {
		this.v1Replacement = v1Replacement;
	}

	/**
	 * @param v2Replacement the v2Replacement to set
	 */
	public void setV2Replacement(String v2Replacement) {
		this.v2Replacement = v2Replacement;
	}

	/**
	 * @param v3Replacement the v3Replacement to set
	 */
	public void setV3Replacement(String v3Replacement) {
		this.v3Replacement = v3Replacement;
	}
}
