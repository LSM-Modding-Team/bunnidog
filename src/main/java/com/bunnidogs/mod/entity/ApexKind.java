package com.bunnidogs.mod.entity;

/** Combat roles share the original saved genome and bounded body, not duplicate creature implementations. */
public enum ApexKind {
    BRUTE("apex",120), PRISM("prismmaw",160), BROOD("broodcaller",320), BLAST("blastmaw",280), SUPPORT("lifebinder",160);
    public final String id;
    public final int recovery;
    ApexKind(String id,int recovery) { this.id=id; this.recovery=recovery; }
}
