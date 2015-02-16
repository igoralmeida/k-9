
package com.fsck.k9.mail;

/**
 * Flags that can be applied to Messages.
 *
 * Update: to support IMAP keywords ( custom flags ) this enum became
 * a class. This class was constructed to resemble enums. Since Java
 * enums are objects internally anyway ( implemented similar to these )
 * this will not be noticably slower.
 *
 * The extra field bCustom denotes custom flags. These get an prefix
 * attached internally. When using the flags with external servers,..
 * one should use the realName() method.
 */
public class Flag {

    /*
     * IMPORTANT WARNING!!
     *
     * DO NOT ADD STATIC FIELDS TO THIS CLASS UNLESS THEY ARE
     * NEW FLAGS THAT GET PREDEFINED. IF YOU DO ADD A NEW PREDEFINED
     * FLAG ADD IT IN THE valueOfByRealName() METHOD BELOW TOO!
     */

    /*
     * IMAP Spec flags.
     */
    public static final Flag DELETED = new Flag("DELETED", "\\Deleted");
    public static final Flag SEEN = new Flag("SEEN", "\\Seen");
    public static final Flag ANSWERED = new Flag("ANSWERED", "\\Answered");
    public static final Flag FLAGGED = new Flag("FLAGGED", "\\Flagged");

    public static final Flag DRAFT = new Flag("DRAFT", "\\Draft");
    public static final Flag RECENT = new Flag("RECENT", "\\Recent");
    public static final Flag FORWARDED = new Flag("FORWARDED", "$forwarded");

    /*
     * The following flags are for internal library use only.
     */
    /**
     * Delete and remove from the LocalStore immediately.
     */
    public static final Flag X_DESTROYED = new Flag("X_DESTROYED");

    /**
     * Sending of an unsent message failed. It will be retried. Used to show status.
     */
    public static final Flag X_SEND_FAILED = new Flag("X_SEND_FAILED");

    /**
     * Sending of an unsent message is in progress.
     */
    public static final Flag X_SEND_IN_PROGRESS = new Flag("X_SEND_IN_PROGRESS");

    /**
     * Indicates that a message is fully downloaded from the server and can be viewed normally.
     * This does not include attachments, which are never downloaded fully.
     */
    public static final Flag X_DOWNLOADED_FULL = new Flag("X_DOWNLOADED_FULL");

    /**
     * Indicates that a message is partially downloaded from the server and can be viewed but
     * more content is available on the server.
     * This does not include attachments, which are never downloaded fully.
     */
    public static final Flag X_DOWNLOADED_PARTIAL = new Flag("X_DOWNLOADED_PARTIAL");

    /**
     * Indicates that the copy of a message to the Sent folder has started.
     */
    public static final Flag X_REMOTE_COPY_STARTED = new Flag("X_REMOTE_COPY_STARTED");

    /**
     * Indicates that all headers of the message have been stored in the
     * database. If this is false, additional headers might be retrieved from
     * the server (if the message is still there).
     */
    public static final Flag X_GOT_ALL_HEADERS = new Flag("X_GOT_ALL_HEADERS");

    /*
     * Predefined Prefixes
     */
    private static final String USER_PREFIX = "USER_";

    // when internal name = name we refer to it as just name
    private final String mName;             // for use towards third party  ex. "\\Deleted"
    private final String mInternalName;     // for internal use in database,...   ex. "DELETED"
    protected final boolean mCustom;

    /**
     * When a Flag is created dynamically we know it's a custom flag.
     *
     * @param mName Internal name of the flag.
     * @return Newly created Flag object.
     */
    public static Flag createFlag(String name) {
        Flag tmpFlag = new Flag(USER_PREFIX + name, name, true);
        return tmpFlag;
    }

    private Flag(String name) {
        this(name, name);
    }

    /**
     * Create a new Flag. This doesn't create a custom flag, it's used to define
     * the predefined flags.
     *
     * @param internalName Name for internal use in database,...   ex. "DELETED"
     * @param name Name for use towards third party ( ex. "\\Deleted" )
     */
    private Flag(String internalName, String name) {
        this(internalName, name, false);
    }

    private Flag(String internalName, String name, boolean isCustom) {
        this.mName = name;
        this.mCustom = isCustom;
        this.mInternalName = internalName;
    }

    /**
     * Returns the predefined static flag object if any. Otherwise a new
     * custom flag is created and returned.
     *
     * When the name starts with the USER_PREFIX we don't add it again to
     * create a new one. This is the case for example when this is called with
     * strings retrieved from the database.
     *
     * IMPORTANT remember the name of the field of predefined flags must equal the
     * internal name!
     *
     * @param mName Name of Flag wanted.
     * @return  Predefined Flag object if any otherwise new custom Flag.
     * @throws IllegalArgumentException Thrown when the field is not accessible.
     */
    public static Flag valueOf(String internalName) throws IllegalArgumentException {
        try {
            // the argument being 'null' implies we look for a static field
            return (Flag)(Flag.class.getField(internalName).get(null));
        } catch (NoSuchFieldException e) {
            // not a predefined flag
            if (internalName.startsWith(USER_PREFIX)) {
                return Flag.createFlag(internalName.substring(USER_PREFIX.length()));
            } else {
                return Flag.createFlag(internalName);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * NOTE
     * This method is intended to be used with Flags which have a different
     * internal and external name! Else use valueOf(String) !!
     *
     *
     * Returns the predefined flag matching the given "real name". For
     * example "\\Deleted" will return the DELETED flag. When no such
     * flag exists we assume it's a custom flag and create it.
     *
     * ! this could be faster this way:
     * http://java.dzone.com/articles/enum-tricks-customized-valueof
     * Since it's only used once I don't see the point.
     *
     * @param real_name Real name to look for.
     * @return The flag that was found or created.
     */
    public static Flag valueOfByRealName(String name) {
        for (Flag f : new Flag[] {DELETED, SEEN, ANSWERED, FLAGGED, DRAFT, RECENT,
                                  X_DESTROYED, X_DOWNLOADED_FULL, X_DOWNLOADED_PARTIAL, X_GOT_ALL_HEADERS,
                                  X_REMOTE_COPY_STARTED, X_SEND_FAILED, X_SEND_IN_PROGRESS
                                 }) {
            if (f.mName.equalsIgnoreCase(name)) {
                return f;
            }
        }

        return Flag.createFlag(name);
    }

    @Override
    public String toString() {
        return mInternalName;
    }

    public String name() {
        return mInternalName;
    }

    /**
     * Returns the real keyword name without user prefix. This is
     * for non-internal use ( syncing with IMAP for example ).
     *
     * @return Real keyword string.
     */
    public String realName() {
        return mName;
    }

    public boolean isCustom() {
        return mCustom;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Flag) {
            Flag f = (Flag)o;
            return ((f) == this ||
                    (f.mCustom == this.mCustom && f.mInternalName.equals(this.mInternalName)
                     && f.mName.equals(this.mName)));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return mInternalName.hashCode();
    }
}
