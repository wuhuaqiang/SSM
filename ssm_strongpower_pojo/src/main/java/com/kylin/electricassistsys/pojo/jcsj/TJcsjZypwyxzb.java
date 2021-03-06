package com.kylin.electricassistsys.pojo.jcsj;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableId;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author 陈文旭
 * @since 2018-04-24
 */

public class TJcsjZypwyxzb extends Model<TJcsjZypwyxzb> {

    private static final long serialVersionUID = 1L;
     @TableId("T_ZYPWYXZB_ID")
    private String tZypwyxzbId;
    private String tZypwyxzbQyid;
    private String tZypwyxzbGqlx;
    private String tZypwyxzbYear;
    private String tZypwyxzbGdkkxrs1;
    private String tZypwyxzbGdkkxrs3;
    private String tZypwyxzbXsl110;
    private String tZypwyxzbXsl10;
    private String tZypwyxzbZhdyhgl;
    private String tZypwyxzbJmddyhgl;
    private String tZypwyxzbCnw;


    public String gettZypwyxzbId() {
        return tZypwyxzbId;
    }

    public void settZypwyxzbId(String tZypwyxzbId) {
        this.tZypwyxzbId = tZypwyxzbId;
    }

    public String gettZypwyxzbQyid() {
        return tZypwyxzbQyid;
    }

    public void settZypwyxzbQyid(String tZypwyxzbQyid) {
        this.tZypwyxzbQyid = tZypwyxzbQyid;
    }

    public String gettZypwyxzbGqlx() {
        return tZypwyxzbGqlx;
    }

    public void settZypwyxzbGqlx(String tZypwyxzbGqlx) {
        this.tZypwyxzbGqlx = tZypwyxzbGqlx;
    }

    public String gettZypwyxzbYear() {
        return tZypwyxzbYear;
    }

    public void settZypwyxzbYear(String tZypwyxzbYear) {
        this.tZypwyxzbYear = tZypwyxzbYear;
    }

    public String gettZypwyxzbGdkkxrs1() {
        return tZypwyxzbGdkkxrs1;
    }

    public void settZypwyxzbGdkkxrs1(String tZypwyxzbGdkkxrs1) {
        this.tZypwyxzbGdkkxrs1 = tZypwyxzbGdkkxrs1;
    }

    public String gettZypwyxzbGdkkxrs3() {
        return tZypwyxzbGdkkxrs3;
    }

    public void settZypwyxzbGdkkxrs3(String tZypwyxzbGdkkxrs3) {
        this.tZypwyxzbGdkkxrs3 = tZypwyxzbGdkkxrs3;
    }

    public String gettZypwyxzbXsl110() {
        return tZypwyxzbXsl110;
    }

    public void settZypwyxzbXsl110(String tZypwyxzbXsl110) {
        this.tZypwyxzbXsl110 = tZypwyxzbXsl110;
    }

    public String gettZypwyxzbXsl10() {
        return tZypwyxzbXsl10;
    }

    public void settZypwyxzbXsl10(String tZypwyxzbXsl10) {
        this.tZypwyxzbXsl10 = tZypwyxzbXsl10;
    }

    public String gettZypwyxzbZhdyhgl() {
        return tZypwyxzbZhdyhgl;
    }

    public void settZypwyxzbZhdyhgl(String tZypwyxzbZhdyhgl) {
        this.tZypwyxzbZhdyhgl = tZypwyxzbZhdyhgl;
    }

    public String gettZypwyxzbJmddyhgl() {
        return tZypwyxzbJmddyhgl;
    }

    public void settZypwyxzbJmddyhgl(String tZypwyxzbJmddyhgl) {
        this.tZypwyxzbJmddyhgl = tZypwyxzbJmddyhgl;
    }

    public String gettZypwyxzbCnw() {
        return tZypwyxzbCnw;
    }

    public void settZypwyxzbCnw(String tZypwyxzbCnw) {
        this.tZypwyxzbCnw = tZypwyxzbCnw;
    }

    @Override
    protected Serializable pkVal() {
        return this.tZypwyxzbId;
    }

    @Override
    public String toString() {
        return "TJcsjZypwyxzb{" +
        "tZypwyxzbId=" + tZypwyxzbId +
        ", tZypwyxzbQyid=" + tZypwyxzbQyid +
        ", tZypwyxzbGqlx=" + tZypwyxzbGqlx +
        ", tZypwyxzbYear=" + tZypwyxzbYear +
        ", tZypwyxzbGdkkxrs1=" + tZypwyxzbGdkkxrs1 +
        ", tZypwyxzbGdkkxrs3=" + tZypwyxzbGdkkxrs3 +
        ", tZypwyxzbXsl110=" + tZypwyxzbXsl110 +
        ", tZypwyxzbXsl10=" + tZypwyxzbXsl10 +
        ", tZypwyxzbZhdyhgl=" + tZypwyxzbZhdyhgl +
        ", tZypwyxzbJmddyhgl=" + tZypwyxzbJmddyhgl +
        ", tZypwyxzbCnw=" + tZypwyxzbCnw +
        "}";
    }
}
