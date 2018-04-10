package com.lhyone.crypt;

/**
 * Created by Think on 2017/8/27.
 */
public class AuthToken {
    private static class TokenKeys {
//        private static final String[] keys = { "86af68d63bb3c1804de1824afd605", "20542037ab4FZHTsNSnyyLwt51n" };
        private static final String[] keys = { "86af68d63bb3c1804de1824afd666", "20542037ab4FZHTsNSnyyLwt666" };

        public static String[] get() {
            return keys;
        }
    }

    private static final String SEPARATOR = ",";

    private static final TokenEncryption TOKEN_ENCRYPTION = new TokenEncryption(TokenKeys.get());

    public final long userId;

    public final long type;
    public final long active;
    public final long expiry;
    public final String rand;

    /**
     *
     * @param userId
     * @param type
     * @param active
     *            when the token is active (in milliseconds)
     * @param expiry
     *            when the token is expired (in milliseconds)
     */
    public AuthToken(long userId, long type, long active, long expiry, String rand) {
        super();
        this.userId = userId;
        this.type = type;
        this.active = active;
        this.expiry = expiry;
        this.rand = rand;
    }

    /**
     * Create the token string.
     *
     * @return encrypted string
     * @throws EncryptException
     */
    public String token() throws EncryptException {
        String unencrypted = new StringBuilder()
                .append(this.userId).append(SEPARATOR)
                .append(this.type).append(SEPARATOR)
                .append(this.active).append(SEPARATOR)
                .append(this.expiry).append(SEPARATOR)
                .append(this.rand).toString();
        String token = TOKEN_ENCRYPTION.encrypt(unencrypted);
        return token;
    }

    public static boolean isActive(AuthToken authToken) {
        long now = System.currentTimeMillis();
        return authToken != null && now > authToken.active && now < authToken.expiry;
    }

    public static boolean isActive(String token) throws EncryptException {
        return isActive(parse(token));
    }

    public static AuthToken parse(String token) throws EncryptException {
        String decrypt = TOKEN_ENCRYPTION.decrypt(token);
        String[] arr = decrypt.split(SEPARATOR);
        if (arr != null && arr.length == 5) {
            int idx = 0;
            long userId = Long.valueOf(arr[idx++]);
            long type = Long.valueOf(arr[idx++]);
            long active = Long.valueOf(arr[idx++]);
            long expiry = Long.valueOf(arr[idx++]);
            String rand = arr[idx++];

            return new AuthToken(userId, type, active, expiry, rand);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AuthToken [userId=").append(userId).append(", active=").append(active)
                .append(", type=").append(type).append(", expiry=").append(expiry).append(", rand=").append(rand).append("]");
        return builder.toString();
    }

    public static void main(String[] args) {
        long now = System.currentTimeMillis()-900000000;
        long expiry = System.currentTimeMillis()+900000000;
        String unencrypted = new StringBuilder()
                .append(1001L).append(SEPARATOR)
                .append(2L).append(SEPARATOR)
                .append(now).append(SEPARATOR)
                .append(expiry).append(SEPARATOR)
                .append("rand").toString();
        String token = TOKEN_ENCRYPTION.encrypt(unencrypted);
        System.out.println(token);

        AuthToken authToken = parse(token);
        System.out.println(isActive(authToken));
        System.out.println(authToken.active);
        System.out.println(authToken.expiry);
    }
}
