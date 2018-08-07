package com.jbm.game.engine.struct.time;

import com.jbm.game.engine.util.SymbolUtil;

/**
 * 区间表达式
 * @author JiangBangMing
 *
 * 2018年7月13日 下午8:18:11
 */
public class RangeReg {

    public enum Type {
        NULL,//空
        ONLY,//唯一
        OR,// 通过,和/分割表示
        TO,// 通过-分割表示
    }
    private Type type;
    private String range;

    public RangeReg(String range) {
        if (range.indexOf("-") > 0) {//区间划分
            this.type = Type.TO;
        } else if (range.indexOf(",") > 0) {//或划分
            this.type = Type.OR;
        } else if (SymbolUtil.isNullOrEmpty(range)) {
            this.type = Type.NULL;
        } else {
            this.type = Type.ONLY;
        }
        this.range=range.replace("[", "").replace("]", "");
//        List<String> parseArray = JSON.parseArray(range, String.class);
//        ranges = parseArray.toArray(new String[parseArray.size()]);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

}
