package com.gerefi.tune.xml;

import com.gerefi.core.gerefiVersion;

import javax.xml.bind.annotation.XmlAttribute;

public class Bibliography {
    private String tuneComment = null;

    @XmlAttribute
    public String getAuthor() {
        return "gerefi " + gerefiVersion.CONSOLE_VERSION;
    }

    @XmlAttribute
    public String getTuneComment() {
        return tuneComment;
    }

    public void setTuneComment(String tuneComment) {
        this.tuneComment = tuneComment;
    }

    @XmlAttribute
    public String getWriteDate() {
        return "date";
    }
}
