package com.obenproto.obenzh.api.response;

import com.obenproto.obenzh.api.domain.ObenPhrase;

import java.util.ArrayList;

public class GetPhrasesResponse extends ArrayList<ObenPhrase> {
    public ObenPhrase.PhraseObj getPhraseByRecordID(Integer recordID) {
        for (ObenPhrase phrase : this) {
            if (phrase.Phrase.recordId.intValue() == recordID.intValue()) {
                return phrase.Phrase;
            }
        }
        return null;
    }
}
