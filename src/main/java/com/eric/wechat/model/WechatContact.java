package com.eric.wechat.model;

import com.blade.kit.json.JSONArray;

/**
 * Created by Eric Cen on 2016/11/15.
 */
public class WechatContact {

    private JSONArray memberList;
    private JSONArray contactList;
    private JSONArray groupList;

    public WechatContact() {
    }

    public JSONArray getMemberList() {
        return memberList;
    }

    public void setMemberList(JSONArray memberList) {
        this.memberList = memberList;
    }

    public JSONArray getContactList() {
        return contactList;
    }

    public void setContactList(JSONArray contactList) {
        this.contactList = contactList;
    }

    public JSONArray getGroupList() {
        return groupList;
    }

    public void setGroupList(JSONArray groupList) {
        this.groupList = groupList;
    }
}
