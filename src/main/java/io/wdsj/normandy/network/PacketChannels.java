package io.wdsj.normandy.network;

public final class PacketChannels {
    public static final int PROTOCOL_VERSION = 1;
    public static final class S2C {
        public static final String LOGIN_CHALLENGE = "normandylogin:login_challenge";
        public static final String KEY_GEN_REQUEST = "normandylogin:key_gen_request";
        public static final String HANDSHAKE_ACK = "normandylogin:handshake_ack";
    }
    public static final class C2S {
        public static final String HANDSHAKE = "normandylogin:handshake";
        public static final String CHALLENGE_RESPONSE = "normandylogin:challenge_response";
        public static final String PUBLIC_KEY_SHARE = "normandylogin:public_key_share";
    }
}
