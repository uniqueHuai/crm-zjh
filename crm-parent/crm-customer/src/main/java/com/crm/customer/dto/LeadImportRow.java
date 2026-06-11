package com.crm.customer.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class LeadImportRow {

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("公司")
    private String company;

    @ExcelProperty("职位")
    private String position;

    @ExcelProperty("省份")
    private String province;

    @ExcelProperty("城市")
    private String city;

    @ExcelProperty("来源")
    private String sourceChannel;

    @ExcelProperty("备注")
    private String remark;
}
