package com.alguojian.richeditor;

import java.io.Serializable;

public class RichStatusBean implements Serializable {

    //是否加粗
    public boolean bold;

    //是否斜体
    public boolean italic;

    //在光标插入点开启或关闭下角标
    public boolean subscript;

    //在光标插入点开启或关闭上角标。
    public boolean superscript;

    //在光标插入点开启或关闭删除线
    public boolean strikeThrough;

    //是否是有序列表
    public boolean orderedList;

    //是否是无序列表
    public boolean unorderedList;

    //是否下划线
    public boolean underline;

    //是否水平线
    public boolean horizontalRule;

    //是否文本对齐
    public boolean justifyFull;

    //html标签
    public String formatBlock;

    //字体位置，0-居左，1-居中，2居右
    public int align;

    public int fontSize = 3;

    public String textColor = "#000000";
    public String textBgColor = "#ffffff";

}
