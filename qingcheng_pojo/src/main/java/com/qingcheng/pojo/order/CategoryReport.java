package com.qingcheng.pojo.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_catrgory_report")
public class CategoryReport  implements Serializable {
    @Id
    private Integer categoryId1;
    @Id
    private Integer categoryId2;
    @Id
    private Integer categoryId3;
    @Id
    private Date countDate;

    private Integer num;

    private Integer money;
}
