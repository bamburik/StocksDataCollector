package org.bamburov.models;

public enum Consensus {
    STRONG_NO("strong no"),
    NO("no"),
    NEUTRAL("neutral"),
    YES("yes"),
    STRONG_YES("strong yes"),
    LACK_OF_INFO("lack of info");

    public final String value;

    private Consensus(String value) {
        this.value = value;
    }
}
