package com.github.lancethomps.lava.common.expr;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;

import ognl.MemberAccess;

/**
 * The Class OgnlMemberAccess.
 */
public class OgnlMemberAccess implements MemberAccess {

	/** The allow package protected access. */
	private boolean allowPackageProtectedAccess;

	/** The allow private access. */
	private boolean allowPrivateAccess;

	/** The allow protected access. */
	private boolean allowProtectedAccess;

	/**
	 * Instantiates a new ognl member access.
	 *
	 * @param allowAllAccess the allow all access
	 */
	public OgnlMemberAccess(boolean allowAllAccess) {
		this(allowAllAccess, allowAllAccess, allowAllAccess);
	}

	/**
	 * Instantiates a new ognl member access.
	 *
	 * @param allowPrivateAccess the allow private access
	 * @param allowProtectedAccess the allow protected access
	 * @param allowPackageProtectedAccess the allow package protected access
	 */
	public OgnlMemberAccess(boolean allowPrivateAccess, boolean allowProtectedAccess, boolean allowPackageProtectedAccess) {
		super();
		this.allowPrivateAccess = allowPrivateAccess;
		this.allowProtectedAccess = allowProtectedAccess;
		this.allowPackageProtectedAccess = allowPackageProtectedAccess;
	}

	/**
	 *       Returns true if the given member is accessible or can be made accessible
	 *       by this object.
	 *
	 * @param context the context
	 * @param target the target
	 * @param member the member
	 * @param propertyName the property name
	 * @return true, if is accessible
	 */
	@Override
	public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
		int modifiers = member.getModifiers();
		boolean result = Modifier.isPublic(modifiers);

		if (!result) {
			if (Modifier.isPrivate(modifiers)) {
				result = isAllowPrivateAccess();
			} else {
				if (Modifier.isProtected(modifiers)) {
					result = isAllowProtectedAccess();
				} else {
					result = isAllowPackageProtectedAccess();
				}
			}
		}
		return result;
	}

	/**
	 * Checks if is allow package protected access.
	 *
	 * @return the allowPackageProtectedAccess
	 */
	public boolean isAllowPackageProtectedAccess() {
		return allowPackageProtectedAccess;
	}

	/**
	 * Checks if is allow private access.
	 *
	 * @return the allowPrivateAccess
	 */
	public boolean isAllowPrivateAccess() {
		return allowPrivateAccess;
	}

	/**
	 * Checks if is allow protected access.
	 *
	 * @return the allowProtectedAccess
	 */
	public boolean isAllowProtectedAccess() {
		return allowProtectedAccess;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ognl.MemberAccess#restore(java.util.Map, java.lang.Object, java.lang.reflect.Member, java.lang.String, java.lang.Object)
	 */
	@Override
	public void restore(Map context, Object target, Member member, String propertyName, Object state) {
		if (state != null) {
			((AccessibleObject) member).setAccessible(((Boolean) state).booleanValue());
		}
	}

	/**
	 * Sets the allow package protected access.
	 *
	 * @param allowPackageProtectedAccess the allowPackageProtectedAccess to set
	 */
	public void setAllowPackageProtectedAccess(boolean allowPackageProtectedAccess) {
		this.allowPackageProtectedAccess = allowPackageProtectedAccess;
	}

	/**
	 * Sets the allow private access.
	 *
	 * @param allowPrivateAccess the allowPrivateAccess to set
	 */
	public void setAllowPrivateAccess(boolean allowPrivateAccess) {
		this.allowPrivateAccess = allowPrivateAccess;
	}

	/**
	 * Sets the allow protected access.
	 *
	 * @param allowProtectedAccess the allowProtectedAccess to set
	 */
	public void setAllowProtectedAccess(boolean allowProtectedAccess) {
		this.allowProtectedAccess = allowProtectedAccess;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ognl.MemberAccess#setup(java.util.Map, java.lang.Object, java.lang.reflect.Member, java.lang.String)
	 */
	/*
	 * =================================================================== MemberAccess interface ===================================================================
	 */
	@Override
	public Object setup(Map context, Object target, Member member, String propertyName) {
		Object result = null;

		if (isAccessible(context, target, member, propertyName)) {
			AccessibleObject accessible = (AccessibleObject) member;

			if (!accessible.isAccessible()) {
				result = Boolean.TRUE;
				accessible.setAccessible(true);
			}
		}
		return result;
	}
}
